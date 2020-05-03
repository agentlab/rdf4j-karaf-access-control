package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.junit.Test;

public class FilteringTests extends AbstractUnitTests {

    @Test
    public void superUserShouldHaveAccess() {
        IRI webid = triplestore.getSuperUserIri();//unfilteredConnection.getValueFactory().createIRI("http://example.org/emma");
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        shouldHaveReadAccess(webid, subj, pred, obj, false);
    }

    @Test
    public void anonymousShouldNotHaveAccess() {
        IRI webid = triplestore.getAnonymousIri();//unfilteredConnection.getValueFactory().createIRI("http://example.org/randomUser");
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        shouldNotHaveReadAccess(webid, subj, pred, obj, false);
    }

    @Test
    public void blackWhitelisting() {
        IRI webid = unfilteredConnection.getValueFactory().createIRI("http://example.org/randomUser");
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        shouldNotHaveReadAccess(webid, subj, pred, obj, false);
        ppManager.setWhitelisting(false);
        shouldHaveReadAccess(webid, subj, pred, obj, false);
        ppManager.setWhitelisting(true);
    }

//	@Test
//	public void superUserShouldHaveUpdateAccess() {
//		RepositoryConnection conn = getFilteredConnection("http://example.org/randomUser");
//		Update update = conn.prepareUpdate(SPARQL, "DELETE DATA { <> <> <> }", "cpgu:///");
//		update.execute();
//		conn.close();
//	}

}