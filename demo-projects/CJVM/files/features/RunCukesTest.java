package features;

import org.junit.runner.RunWith;

import cucumber.junit.Cucumber;

@RunWith(Cucumber.class)
@Cucumber.Options(format = { "pretty", "html:target/cucumber" }, monochrome = true, glue = { "greenhouse/example" }, features = { "myFeatures" })
public class RunCukesTest {

}
