# Greenhouse

A web UI for searching and executing scenarios in maven-powered cuke4duke projects.

## Requirements

### Installation

TODO: Document greenhouse-maven-plugin installation

### Integration With Your Project

Your properties:

Greenhouse requires that you expose 3 Maven properties:

- **cucumber.tagsArg**: A property for passing through to cuke4duke the "--tags" parameter.
- **cucumber.format**: cuke4duke's output format
- **cucumber.out**: cuke4duke's output directory/file

You are free to make the default values anything you'd like.

Example pom.xml properties configuration:

    	<properties>
    		<cucumber.tagsArg>--tags=~@ignore</cucumber.tagsArg>
    		<cucumber.format>junit</cucumber.format>
    		<cucumber.out>${project.build.directory}/cucumber-reports</cucumber.out>
    	</properties>

Example pom.xml cuke4duke plugin configuration:

    	<plugins>
    		<plugin>
    			<groupId>cuke4duke</groupId>
    			<artifactId>cuke4duke-maven-plugin</artifactId>
    			<version>0.4.4</version>
    			<configuration>
    				<jvmArgs>
    					<jvmArg>
    						-Dcuke4duke.objectFactory=cuke4duke.internal.jvmclass.SpringFactory
    					</jvmArg>
    					<jvmArg>-Dfile.encoding=UTF-8</jvmArg>
    				</jvmArgs>
    				<cucumberArgs>
    					<cucumberArg>--backtrace</cucumberArg>
    					<cucumberArg>--format</cucumberArg>
    					<cucumberArg>pretty</cucumberArg>
    					<cucumberArg>--format</cucumberArg>
    					<cucumberArg>${cucumber.format}</cucumberArg>
    					<cucumberArg>--out</cucumberArg>
    					<cucumberArg>${cucumber.out}</cucumberArg>
    					<cucumberArg>--require</cucumberArg>
    					<cucumberArg>${basedir}/target/test-classes</cucumberArg>
    					<cucumberArg>${cucumber.tagsArg}</cucumberArg>
    				</cucumberArgs>
    				<gems>
    					<gem>install cuke4duke --version 0.4.4</gem>
    				</gems>
    			</configuration>
    			<executions>
    				<execution>
    					<id>run-features</id>
    					<phase>integration-test</phase>
    					<goals>
    						<goal>cucumber</goal>
    					</goals>
    				</execution>
    			</executions>
    		</plugin>
    	</plugins>

## Features for 1.0:

- Search
    - Single tag
    - Combination of tags
    - Scenario name
    - Feature file name
    - Full-text
- Scenario Execution
    - Execute a single scenario
    - Execute a single example in a single scenario
    - Execute a tag
    - Execute a set of tags 
    - Modify and execute a single scenario
- Tags
    - Display ordered list of tag counts
    - Display tag documentation

## And perhaps in the far-flung future:

- VCS integration to automatically update the cuke4duke project and re-index the feature files
- Execute multiple maven process to run scenario sets in parallel