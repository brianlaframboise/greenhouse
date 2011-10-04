package kappamaki.index;

import com.google.common.collect.ImmutableList;

public class StepMethod {

    private final String regex;
    private final ImmutableList<String> types;

    public StepMethod(String regex, ImmutableList<String> types) {
        this.regex = regex;
        this.types = types;
    }

    public String getRegex() {
        return regex;
    }

    public ImmutableList<String> getTypes() {
        return types;
    }

}
