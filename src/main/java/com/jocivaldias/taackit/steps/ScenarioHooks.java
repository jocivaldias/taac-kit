package com.jocivaldias.taackit.steps;

import com.jocivaldias.taackit.support.ScenarioContext;
import io.cucumber.java.Before;

public class ScenarioHooks {

    @Before
    public void resetContext() {
        ScenarioContext.reset();
    }
}
