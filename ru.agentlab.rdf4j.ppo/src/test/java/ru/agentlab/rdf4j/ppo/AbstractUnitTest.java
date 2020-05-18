package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection;
import org.junit.Before;
import ru.agentlab.rdf4j.ppo.policies.PPManager;
import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;
import ru.agentlab.rdf4j.ppo.triplestore.FakeTripleStore;

import java.io.IOException;

public abstract class AbstractUnitTest {

    protected PPManager ppManager;
    protected FakeTripleStore triplestore;

    protected String policiesContext = "http://cpgu.kbpm.ru/ns/rm/policies";
    protected String superUser = "http://cpgu.kbpm.ru/ns/rm/users#superuser";
    protected String anonymous = "http://cpgu.kbpm.ru/ns/rm/users#anonymous";
    protected String dimonia = "http://cpgu.kbpm.ru/ns/rm/users#dimonia";
    protected String amivanoff = "http://cpgu.kbpm.ru/ns/rm/users#amivanoff";

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

}
