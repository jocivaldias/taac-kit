package com.jocivaldias.taackit.steps;

import com.jocivaldias.taackit.json.JsonAssertionOptions;
import com.jocivaldias.taackit.support.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonConfigSteps {

    @Dado("que as comparacoes de JSON sejam strict")
    public void queAsComparacoesDeJsonSejamStrict() {
        ScenarioContext.current().getJsonOptions().setStrict(true);
    }

    @Dado("que as comparacoes de JSON sejam lenient")
    public void queAsComparacoesDeJsonSejamLenient() {
        ScenarioContext.current().getJsonOptions().setStrict(false);
    }

    @E("ignorando os campos de JSON:")
    public void ignorandoOsCamposDeJson(String docString) {
        Set<String> fields = Arrays.stream(docString.split("\\r?\\n"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toSet());

        JsonAssertionOptions options = ScenarioContext.current().getJsonOptions();
        options.setIgnoredFields(fields);
    }

    @E("usando o comparador de JSON {string}")
    public void usandoOComparadorDeJson(String comparatorName) {
        ScenarioContext.current().getJsonOptions().setComparatorName(comparatorName);
    }
}
