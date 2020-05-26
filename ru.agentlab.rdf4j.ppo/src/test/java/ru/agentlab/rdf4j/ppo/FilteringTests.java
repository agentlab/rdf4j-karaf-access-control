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
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;
import static org.junit.Assert.*;

public class FilteringTests extends AbstractUnitTest {

    @Test
    public void dimoniaQueryOneTriple() {
        InterceptingRepositoryConnection connection = triplestore.getConnection(dimonia);
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        List<Long> statistics = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                connection.getStatements(subj, pred, obj);
            }
            long end = System.currentTimeMillis();
            statistics.add(end - start);
        }

        double sumTime = statistics.stream()
                .mapToDouble(a -> a)
                .sum();
        System.out.println("Time for 1000 queries is = " + (sumTime / 1000) + " millis.");
        RepositoryResult<Statement> statements = connection.getStatements(subj, pred, obj);
        System.out.println("Available statements for user = " + statements.stream().count());
        logMemoryUsage();
    }

    @Test
    public void dimoniaQueryGroupOfTriples() {
        InterceptingRepositoryConnection connection = triplestore.getConnection(dimonia);
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        IRI obj = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/cpgu#Classifier");


        for (int i = 0; i < 1000; i++) {
            IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///" + UUID.randomUUID().toString() + ".xml");
            Statement statement = unfilteredConnection.getValueFactory().createStatement(subj, pred, obj);
            unfilteredConnection.add(statement);
        }

        List<Long> statistics = new ArrayList<>();

        long start = System.currentTimeMillis();
        for (int j = 0; j < 1; j++) {
            connection.getStatements(null, pred, null);
        }
        long end = System.currentTimeMillis();
        statistics.add(end - start);

        double sumTime = statistics.stream()
                .mapToDouble(a -> a)
                .sum();
        System.out.println("Time for 1 query is = " + (sumTime / 1) + " millis.");
        RepositoryResult<Statement> statements = connection.getStatements(null, pred, null);
        System.out.println("Available statements for user = " + statements.stream().count());
        logMemoryUsage();
    }

    private void logMemoryUsage() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        double total = 0;
        for (MemoryPoolMXBean memoryPoolMXBean : pools) {
            if (memoryPoolMXBean.getType() == MemoryType.HEAP) {
                double peakUsed = memoryPoolMXBean.getPeakUsage().getUsed() / Math.pow(10, 6);
                total = total + peakUsed;
            }
        }
        System.out.println("Total heap peak used: " + total + " MB");
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

    @Test
    public void anonymousShouldNotHaveUpdateAccess() {
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");

        String query = String.format("delete where { <%s> <%s> ?anyObject. }", subj.toString(), pred.toString());

        InterceptingRepositoryConnection conn = getFilteredConnection(anonymous);
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
    public void dimoniaShouldHaveUpdateAccess() {
        IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");

        String query = String.format("delete where { <%s> <%s> ?anyObject. }", subj.toString(), pred.toString());
        RepositoryConnection conn = getFilteredConnection(dimonia);

        long beforeSize = conn.size();

        Update update = conn.prepareUpdate(SPARQL, query);
        update.execute();

        long afterSize = conn.size();

        assertNotEquals(afterSize, beforeSize);
        conn.close();
    }

    @Test
    public void dimoniaShouldNotHaveReadAccessAfterRemovingRights() throws Exception {
        IRI webid = filteredConnection.getValueFactory().createIRI(dimonia);
        IRI subj = filteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
        IRI pred = filteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
        Value obj = filteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

        InterceptingRepositoryConnection userConnection = triplestore.getConnection(webid);

        RepositoryResult<Statement> statementsBefore = userConnection.getStatements(subj, pred, obj);
        System.out.println("Available statements for user = " + Iterations.asList(statementsBefore).size());

        IRI groupSubj = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/policies#classifierTranslatorRole");
        IRI memberPred = unfilteredConnection.getValueFactory().createIRI("https://agentlab.ru/onto/ppo-roles#roleAgent");
        Value dimoniaObj = unfilteredConnection.getValueFactory().createIRI(dimonia);
        Resource context = unfilteredConnection.getValueFactory().createIRI(policiesContext);
        Statement dimoniaMembership = unfilteredConnection.getValueFactory().createStatement(groupSubj, memberPred, dimoniaObj);

        long before = unfilteredConnection.size();
        unfilteredConnection.remove(dimoniaMembership, context);
        long after = unfilteredConnection.size();

        assertEquals(before - 1, after);
        System.out.println("User rights removed!");

        RepositoryResult<Statement> statementsAfter = userConnection.getStatements(subj, pred, obj);
        System.out.println("Available statements for user = " + Iterations.asList(statementsAfter).size());
    }

    @Test
    public void dimoniaShouldHaveReadAccessToClassifiers() throws Exception {
        IRI webid = filteredConnection.getValueFactory().createIRI(dimonia);
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Value obj = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/cpgu#Classifier");

        InterceptingRepositoryConnection userConnection = triplestore.getConnection(webid);

        RepositoryResult<Statement> userConnectionStatements = userConnection.getStatements(null, pred, obj);
        RepositoryResult<Statement> allAvailableStatements = unfilteredConnection.getStatements(null, pred, obj);

        List<Statement> userListOfStatements = Iterations.asList(userConnectionStatements);
        List<Statement> allStatements = Iterations.asList(allAvailableStatements);

        System.out.println("Триплы, полученные из репозитория с незащищенным соединением:");
        userListOfStatements.forEach(System.out::println);
        System.out.println("Триплы, полученные пользователем dimonia:");
        allStatements.forEach(System.out::println);

        assertTrue(userListOfStatements.containsAll(allStatements));

    }

    @Test
    public void dimoniaShouldNotHaveReadAccessToGroups() throws Exception {
        IRI webid = filteredConnection.getValueFactory().createIRI(dimonia);
        IRI pred = unfilteredConnection.getValueFactory().createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Value obj = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/cpgu#Группировка");

        InterceptingRepositoryConnection userConnection = triplestore.getConnection(webid);

        RepositoryResult<Statement> userConnectionStatements = userConnection.getStatements(null, pred, obj);
        RepositoryResult<Statement> allAvailableStatements = unfilteredConnection.getStatements(null, pred, obj);

        List<Statement> userListOfStatements = Iterations.asList(userConnectionStatements);
        List<Statement> allStatements = Iterations.asList(allAvailableStatements);

        System.out.println("Триплы, полученные из репозитория с незащищенным соединением:");
        allStatements.forEach(System.out::println);
        System.out.println("Триплы, полученные пользователем dimonia:");
        userListOfStatements.forEach(System.out::println);

        assertFalse(allStatements.isEmpty());
        assertTrue(userListOfStatements.isEmpty());

    }


    @Test
    public void dimoniaShouldHaveReadAccessWithNoACLQuery() throws IOException {
        File file = new File("src/test/resources/noacl-query.sparql");

        String noAclQuery = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        RepositoryConnection conn = getFilteredConnection(dimonia);
        TupleQueryResult tupleQueryResult = conn.prepareTupleQuery(SPARQL, noAclQuery).evaluate();

        long size = tupleQueryResult.stream().count();
        assertNotEquals(0, size);
        conn.close();
    }

    @Test
    public void dimoniaShouldHaveReadAccessWithACLQuery() throws IOException {
        File file = new File("src/test/resources/acl-query.sparql");

        String aclQuery = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        InterceptingRepositoryConnection conn = getFilteredConnection(dimonia);
        TupleQueryResult tupleQueryResult = conn.prepareTupleQuery(SPARQL, aclQuery).evaluate();
        long size = tupleQueryResult.stream().count();
        assertNotEquals(0, size);
        conn.close();
    }

    @Test
    public void superuserShouldHaveReadAccessWithACLQuery() throws IOException {
        File file = new File("src/test/resources/acl-query.sparql");

        String aclQuery = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        InterceptingRepositoryConnection conn = getFilteredConnection(superUser);
        TupleQueryResult tupleQueryResult = conn.prepareTupleQuery(SPARQL, aclQuery).evaluate();

        long size = tupleQueryResult.stream().count();
        assertNotEquals(0, size);
        conn.close();
    }

    @Test
    public void anonymousShouldNotHaveReadAccessWithACLQuery() throws IOException {
        File file = new File("src/test/resources/acl-query.sparql");

        String aclQuery = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        InterceptingRepositoryConnection conn = getFilteredConnection(anonymous);
        TupleQueryResult tupleQueryResult = conn.prepareTupleQuery(SPARQL, aclQuery).evaluate();

        long size = tupleQueryResult.stream().count();
        assertNotEquals(0, size);
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

    protected InterceptingRepositoryConnection getFilteredConnection(IRI webid) throws RepositoryException {
        return triplestore.getConnection(webid);
    }

    protected InterceptingRepositoryConnection getFilteredConnection(String webid) throws RepositoryException {
        return triplestore.getConnection(webid);
    }
}