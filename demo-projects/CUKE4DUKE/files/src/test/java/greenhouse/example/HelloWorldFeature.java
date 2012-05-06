package greenhouse.example;

import static junit.framework.Assert.assertEquals;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

@StepDefinitions
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
