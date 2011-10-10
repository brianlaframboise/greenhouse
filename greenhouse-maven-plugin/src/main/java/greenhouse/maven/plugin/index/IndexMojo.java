package greenhouse.maven.plugin.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.google.common.base.Joiner;

import cuke4duke.annotation.I18n.EN.And;
import cuke4duke.annotation.I18n.EN.But;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

/**
 * Indexes step definitions for Greenhouse.
 * 
 * @goal index
 * @execute phase=test-compile
 * @requiresDependencyResolution test
 * @requiresProject true
 */
public class IndexMojo extends AbstractMojo {

    private static final List<Class<? extends Annotation>> ANNOTATIONS;
    static {
        List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
        list.add(Given.class);
        list.add(When.class);
        list.add(Then.class);
        list.add(And.class);
        list.add(But.class);
        ANNOTATIONS = list;
    }

    /**
     * Output directory into whick greenhouse-step-index.properties is written.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * The base package in which to look for @StepDefinitions classes (ex:
     * "com.acme.steps")
     * 
     * @parameter expression="${basePackage}" default-value=""
     */
    protected String basePackage;

    private static class StepMethod {
        public String regex;
        public Class<?>[] params;

        public StepMethod(String regex, Class<?>[] params) {
            this.regex = regex;
            this.params = params;
        }
    }

    public void execute() throws MojoExecutionException {
        ClassLoader classLoader = loadProjectTestClassloader();
        List<Class<?>> classes = loadStepClasses(classLoader);
        List<StepMethod> stepMethods = extractStepMethods(classes);
        serialize(stepMethods);
    }

    protected ClassLoader loadProjectTestClassloader() {
        try {
            List<URL> urls = new ArrayList<URL>();
            for (String path : project.getTestClasspathElements()) {
                urls.add(new File(path).toURI().toURL());
            }
            URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}));
            Thread.currentThread().setContextClassLoader(classLoader);
            return classLoader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Class<?>> loadStepClasses(ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(StepDefinitions.class));
        if (basePackage == null) {
            basePackage = "";
        }
        getLog().info("Scanning package \"" + basePackage + "\" for @StepDefinitions classes...");
        Set<BeanDefinition> definitions = provider.findCandidateComponents(basePackage);

        List<Class<?>> classes = new ArrayList<Class<?>>(definitions.size());
        try {
            for (BeanDefinition definition : definitions) {
                Class<?> clazz = Class.forName(definition.getBeanClassName(), true, classLoader);
                classes.add(clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getLog().info("Step classes found: " + classes.size());
        return classes;
    }

    private List<StepMethod> extractStepMethods(List<Class<?>> classes) {
        List<StepMethod> stepMethods = new ArrayList<StepMethod>();
        for (Class<?> clazz : classes) {
            for (Method method : clazz.getMethods()) {
                getLog().debug("Method: " + method.toString());
                for (Annotation ann : method.getAnnotations()) {
                    for (Class<? extends Annotation> cukeAnnotation : ANNOTATIONS) {
                        if (ann.annotationType().getCanonicalName().equals(cukeAnnotation.getCanonicalName())) {
                            getLog().debug("Adding: " + ann.annotationType().getCanonicalName());
                            String regex = (String) AnnotationUtils.getValue(ann);
                            stepMethods.add(new StepMethod(regex, method.getParameterTypes()));
                        }
                    }
                }
            }
        }
        getLog().info("Step annotations found: " + stepMethods.size());
        return stepMethods;
    }

    private void serialize(List<StepMethod> stepMethods) {
        File stepIndex = new File(outputDirectory.getPath() + File.separator + "greenhouse-step-index.properties");
        getLog().info("Writing step index to: " + stepIndex.getPath());
        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(stepIndex));

            int i = 1;
            for (StepMethod method : stepMethods) {
                buffer.write(i + ".regex=" + method.regex);
                buffer.newLine();

                List<String> types = new ArrayList<String>(method.params.length);
                for (Class<?> paramClass : method.params) {
                    types.add(paramClass.getCanonicalName());
                }
                buffer.write(i + ".params=" + Joiner.on(',').join(types));
                buffer.newLine();
                buffer.newLine();
                i++;
            }
            buffer.flush();
            buffer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLog().info("Step index written");
    }

}
