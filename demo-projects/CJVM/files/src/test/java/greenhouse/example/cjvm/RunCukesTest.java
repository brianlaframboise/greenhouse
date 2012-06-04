package greenhouse.example.cjvm;

import org.junit.runner.RunWith;

import cucumber.junit.Cucumber;

@RunWith(Cucumber.class)
@Cucumber.Options(format = { "pretty", "html:target/cucumber" }, monochrome = true)
public class RunCukesTest {

}
