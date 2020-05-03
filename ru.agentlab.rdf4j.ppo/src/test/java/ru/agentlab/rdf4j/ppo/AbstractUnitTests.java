package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection;
import org.junit.Assert;
import org.junit.Before;
import ru.agentlab.rdf4j.ppo.policies.PPManager;
import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;
import ru.agentlab.rdf4j.ppo.triplestore.FakeTripleStore;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUnitTests {

    protected PPManager ppManager;
    protected FakeTripleStore triplestore;

    protected String policiesContext = "http://cpgu.kbpm.ru/ns/rm/policies";
    protected String superUser = "http://cpgu.kbpm.ru/ns/rm/users#superuser";
    protected String agentUser = "http://cpgu.kbpm.ru/ns/rm/users#exampleAgent";
    protected String anonymous = "http://cpgu.kbpm.ru/ns/rm/users#anonymous";
    protected String member = "http://xmlns.com/foaf/0.1/member";

    InterceptingRepositoryConnection filteredConnection;
    RepositoryConnection unfilteredConnection;

    @Before
    public void setup() {
        ppManager = new PPManagerImpl();
        ppManager.setPoliciesContext(policiesContext);

        triplestore = new FakeTripleStore(ppManager, superUser, anonymous);
        triplestore.loadData("al-rm-vocab.ttl");
        triplestore.loadData("rm-user-types.ttl");
        triplestore.loadData("users.ttl");
        triplestore.loadData("cpgu.ttl");
        triplestore.loadData("sample.ttl");
        triplestore.loadPolicies("access-management.ttl");
        triplestore.init();

        IRI webid = triplestore.getAnonymousIri();
        filteredConnection = triplestore.getConnection(webid);

        unfilteredConnection = triplestore.getUnfilteredConnection();
    }

    /**
     * match unfiltered response with filtered response
     */
    protected void shouldHaveReadAccess(IRI webid, Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) {
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
