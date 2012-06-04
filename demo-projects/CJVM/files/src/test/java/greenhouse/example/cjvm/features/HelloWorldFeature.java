package greenhouse.example.cjvm.features;

import static junit.framework.Assert.assertEquals;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

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
