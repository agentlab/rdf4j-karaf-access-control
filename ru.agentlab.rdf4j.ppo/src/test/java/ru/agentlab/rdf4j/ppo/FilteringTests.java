package ru.agentlab.rdf4j.ppo;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.agentlab.rdf4j.ppo.policies.PPManager;
import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;
import ru.agentlab.rdf4j.ppo.triplestore.FakeTripleStore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FilteringTests {
    protected PPManager ppManager;
    protected FakeTripleStore triplestore;

    protected String policiesContext = "http://cpgu.kbpm.ru/ns/rm/policies";
    protected String superUser = "http://cpgu.kbpm.ru/ns/rm/users#superuser";
    protected String anonymous = "http://cpgu.kbpm.ru/ns/rm/users#anonymous";

    InterceptingRepositoryConnection filteredConnection;
    RepositoryConnection unfilteredConnection;

    @Before
    public void setup() throws IOException {
        ppManager = new PPManagerImpl();
        ppManager.setPoliciesContext(policiesContext);

        triplestore = new FakeTripleStore(ppManager, superUser, anonymous);
        triplestore.loadData("vocabs-ext/acl.ttl");
        triplestore.loadData("vocabs-ext/ppo.ttl");
        triplestore.loadData("vocabs-shapes-our/ppo-roles-vocab.ttl");
        triplestore.loadData("vocabs-shapes-our/rm-min-vocab.ttl");
        triplestore.loadData("data-our/users.ttl");
        triplestore.loadData("data-our/data-sample.ttl");
        triplestore.loadPolicies("data-our/access-management-settings.ttl");
        triplestore.init();

        IRI webid = triplestore.getAnonymousIri();
        filteredConnection = triplestore.getConnection(webid);

        unfilteredConnection = triplestore.getUnfilteredConnection();
    }

    @Test
    public void superUserShouldHaveAccess() {
        IRI webid = triplestore.getSuperUserIri();//unfilteredConnection.getValueFactory().createIRI("http://example.org/emma");
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        shouldHaveReadAccess(webid, subj, pred, obj, false);
        System.out.println("Huuuu");
    }

    @Test
    public void anonumousShouldNotHaveAccess() {
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

    @Test
    public void anonymousShouldNotHaveUpdateAccess() {
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");

        String query = String.format("delete where { <%s> <%s> ?anyObject }", subj.toString(), pred.toString());

        RepositoryConnection conn = getFilteredConnection(anonymous);
        long beforeSize = conn.size();
        System.out.println(beforeSize);
        Update update = conn.prepareUpdate(SPARQL, query);
        update.execute();
        long afterSize = conn.size();
        System.out.println(afterSize);
        assertEquals(beforeSize, afterSize);
        conn.close();
    }

    @Test
    public void dimoniaShouldHaveReadAccessWithNoACLQuery() throws IOException {
        File file = new File("src/test/resources/noacl-query.sparql");

        String aclQuery = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        RepositoryConnection conn = getFilteredConnection(anonymous);
        TupleQueryResult tupleQueryResult = conn.prepareTupleQuery(SPARQL, aclQuery).evaluate();
//        List<String> bindingSets = tupleQueryResult.getBindingNames();
//        BindingSet bindingSet;
//
//        List<String> result = new ArrayList<>();
//        while (tupleQueryResult.hasNext()) {
//            bindingSet = tupleQueryResult.next();
//            for (String binding : bindingSets) {
//                Value value = bindingSet.getValue(binding);
//
//                if (Objects.nonNull(value)) {
//                    result.add(String.valueOf(value));
//                }
//            }
//        }
        long size = tupleQueryResult.stream().count();
        assertNotEquals(0, size);
        conn.close();
    }

    @Test
    public void dimoniaShouldNotHaveReadAccessWithACLQuery() throws IOException {
        File file = new File("src/test/resources/acl-query.sparql");

        String aclQuery = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        RepositoryConnection conn = getFilteredConnection(anonymous);
        TupleQueryResult tupleQueryResult = conn.prepareTupleQuery(SPARQL, aclQuery).evaluate();
        long size = tupleQueryResult.stream().count();
        assertEquals(0, size);
        conn.close();
    }


    @Test
    public void superUserShouldHaveUpdateAccess() {
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");

        String query = String.format("delete where { <%s> <%s> ?anyObject }", subj.toString(), pred.toString());

        RepositoryConnection conn = getFilteredConnection(superUser);
        long beforeSize = conn.size();
        System.out.println(beforeSize);
        Update update = conn.prepareUpdate(SPARQL, query);
        update.execute();
        long afterSize = conn.size();
        System.out.println(afterSize);
        assertNotEquals(afterSize, beforeSize);
        conn.close();
    }

    /**
     * match unfiltered response with filtered response
     */
    private void shouldHaveReadAccess(IRI webid, Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) {
        List<Statement> expected = Iterations.asList(unfilteredConnection.getStatements(subj, pred, obj, includeInferred, contexts));
        List<Statement> actual = Iterations.asList(getFilteredConnection(webid).getStatements(subj, pred, obj, includeInferred, contexts));

        if (expected.isEmpty())
            Assert.fail("statement does not exist");

        Assert.assertEquals(expected, actual);
    }

    /**
     * match filtered response with empty list
     */
    protected void shouldNotHaveReadAccess(IRI webid, Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) {
        List<Statement> expected = Iterations.asList(unfilteredConnection.getStatements(subj, pred, obj, includeInferred, contexts));
        List<Statement> actual = Iterations.asList(getFilteredConnection(webid).getStatements(subj, pred, obj, includeInferred, contexts));

        if (expected.isEmpty())
            Assert.fail("statement does not exist");

        Assert.assertEquals("should not return statements", new ArrayList<String>(), actual);
    }

    protected RepositoryConnection getFilteredConnection(IRI webid) throws RepositoryException {
        return triplestore.getConnection(webid);
    }

    protected RepositoryConnection getFilteredConnection(String webid) throws RepositoryException {
        return triplestore.getConnection(webid);
    }
}