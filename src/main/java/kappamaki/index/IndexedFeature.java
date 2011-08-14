package kappamaki.index;

import com.google.common.collect.ImmutableList;

public class IndexedFeature {

    private final String uri;
    private final String name;
    private final ImmutableList<IndexedScenario> scenarios;

    public IndexedFeature(String uri, String name, ImmutableList<IndexedScenario> scenarios) {
        this.uri = uri;
        this.name = name;
        this.scenarios = scenarios;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public ImmutableList<IndexedScenario> getScenarios() {
        return scenarios;
    }

}
