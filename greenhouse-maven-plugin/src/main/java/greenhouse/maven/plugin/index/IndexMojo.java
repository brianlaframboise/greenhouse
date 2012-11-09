package greenhouse.maven.plugin.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Indexes step definitions for Greenhouse. Works for both cuke4duke and
 * Cucumber-JVM.
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

        // Cuke4Duke
        list.add(cuke4duke.annotation.I18n.EN.Given.class);
        list.add(cuke4duke.annotation.I18n.EN.When.class);
        list.add(cuke4duke.annotation.I18n.EN.Then.class);
        list.add(cuke4duke.annotation.I18n.EN.And.class);
        list.add(cuke4duke.annotation.I18n.EN.But.class);

        // Cucumber-JVM
        list.add(cucumber.api.java.en.Given.class);
        list.add(cucumber.api.java.en.When.class);
        list.add(cucumber.api.java.en.Then.class);
        list.add(cucumber.api.java.en.And.class);
        list.add(cucumber.api.java.en.But.class);

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
     * @required
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
        if (basePackage == null || "".equals(basePackage)) {
            throw new IllegalArgumentException("A base package must be specified using the basePackage argument (ex: \"com.acme\")");
        }
        getLog().info("Scanning basePackage \"" + basePackage + "\" for annotated step methods...");

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {
                return metadata.getClassName().startsWith(basePackage);
            }
        });
        Set<BeanDefinition> definitions = provider.findCandidateComponents(basePackage);
        getLog().info("Candidate classes found: " + definitions.size());

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
            Multimap<Class<?>, String> enums = HashMultimap.create();
            int i = 1;
            for (StepMethod method : stepMethods) {
                buffer.write(i + ".regex=" + method.regex);
                buffer.newLine();

                List<String> types = new ArrayList<String>(method.params.length);
                for (Class<?> paramClass : method.params) {

                    types.add(paramClass.getCanonicalName());

                    if (paramClass.isEnum() && !enums.containsKey(paramClass)) {
                        Object[] constants = paramClass.getEnumConstants();
                        for (Object constant : constants) {
                            String name = (String) constant.getClass().getMethod("name", (Class<?>[]) null).invoke(constant, (Object[]) null);
                            enums.put(paramClass, name);
                        }
                    }
                }
                buffer.write(i + ".params=" + Joiner.on(',').join(types));
                buffer.newLine();
                buffer.newLine();
                i++;
            }
            for (Class<?> clazz : enums.keySet()) {
                Collection<String> names = enums.get(clazz);
                buffer.write("enum.");
                buffer.write(clazz.getCanonicalName());
                buffer.write("=");
                buffer.write(Joiner.on(',').join(names));
                buffer.newLine();
            }
            buffer.flush();
            buffer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getLog().info("Step index written");
    }

}
