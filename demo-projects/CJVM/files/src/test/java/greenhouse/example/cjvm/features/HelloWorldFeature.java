package greenhouse.example.cjvm.features;

import static org.junit.Assert.assertEquals;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class HelloWorldFeature {

    private String action;
    private String subject;

    @Given("^the Action is (\\w+)$")
    public void the_action_is(String action) {
        this.action = action;
    }

    @When("^the Subject is (\\w+)$")
    public void the_subject_is_(String subject) {
        this.subject = subject;
    }

    @Then("^the Greeting is (.*)$")
    public void the_greeting_is(String expected) {
        assertEquals(expected, action + ", " + subject);
    }
}
