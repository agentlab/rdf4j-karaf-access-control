package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Test;
import ru.agentlab.rdf4j.ppo.triplestore.QueryBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryBuilderTest extends AbstractUnitTest {

    @Test
    public void shouldBuildGetQuery() {
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        String buildedQuery = QueryBuilder.buildGetQuery(subj, pred, obj);
        System.out.println(buildedQuery);
        TupleQueryResult tupleQueryResult = unfilteredConnection.prepareTupleQuery(QueryLanguage.SPARQL, buildedQuery).evaluate();
        List<BindingSet> bindingSets = Iterations.asList(tupleQueryResult);
        assertEquals(1, bindingSets.size());
        System.out.println("Received result: ");
        for (BindingSet bindings : bindingSets) {
            System.out.println(bindings);
        }
    }

}
