package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.Objects;

public class QueryBuilder {

    private QueryBuilder() {
    }

    public static String buildGetQuery(Resource subj, IRI pred, Value obj) {
        String subjStr = Objects.isNull(subj) ? "?subject" : "<" + subj + ">";
        String predStr = Objects.isNull(pred) ? "?predicate" : "<" + pred + ">";
        String objStr = "?object";
        if (obj instanceof Literal) {
            objStr = String.valueOf(obj);
        } else if (obj instanceof IRI) {
            objStr = "<" + obj + ">";
        }
        return "SELECT DISTINCT ?subject ?predicate ?object { ?subject ?predicate ?object " +
                "FILTER (?subject = " + subjStr + ") " +
                "FILTER (?predicate = " + predStr + ") " +
                "FILTER (?object = " + objStr + ")}";
    }

}
