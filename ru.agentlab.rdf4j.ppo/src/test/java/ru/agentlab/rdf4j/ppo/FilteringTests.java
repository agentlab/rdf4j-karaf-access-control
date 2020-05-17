package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.agentlab.rdf4j.ppo.policies.PPManager;
import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;
import ru.agentlab.rdf4j.ppo.triplestore.FakeTripleStore;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

public class FilteringTests {
	protected PPManager ppManager;
	protected FakeTripleStore triplestore;

	protected String policiesContext = "http://cpgu.kbpm.ru/ns/rm/policies";
	protected String superUser = "http://cpgu.kbpm.ru/ns/rm/users#superuser";
	protected String anonymous = "http://cpgu.kbpm.ru/ns/rm/users#anonymous";
	protected String dimonia = "http://cpgu.kbpm.ru/ns/rm/users#dimonia";

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
	public void dimoniaQueryOneTriple() {
		InterceptingRepositoryConnection connection = triplestore.getConnection(dimonia);
		IRI subj = unfilteredConnection.getValueFactory().createIRI("file:///urn-s2-iisvvt-infosystems-classifier-45950.xml");
		IRI pred = unfilteredConnection.getValueFactory().createIRI("http://purl.org/dc/terms/title");
		Value obj = unfilteredConnection.getValueFactory().createLiteral("ТН ВЭД ТС");

		List<Long> statistics = new ArrayList<>();

		for (int i = 0; i < 1000; i++) {
			long start = System.currentTimeMillis();
			for (int j = 0; j < 1000; j++) {
				connection.getStatements(subj, pred, obj, false);
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
		IRI subj = unfilteredConnection.getValueFactory().createIRI("http://vocab.deri.ie/ppo");
		List<Long> statistics = new ArrayList<>();

		for (int i = 0; i < 1000; i++) {
			long start = System.currentTimeMillis();
			for (int j = 0; j < 1000; j++) {
				connection.getStatements(subj, null, null);
			}
			long end = System.currentTimeMillis();
			statistics.add(end - start);
		}

		double sumTime = statistics.stream()
				.mapToDouble(a -> a)
				.sum();
		System.out.println("Time for 1000 queries is = " + (sumTime / 1000) + " millis.");
		RepositoryResult<Statement> statements = connection.getStatements(subj, null, null);;
		System.out.println("Available statements for user = " + statements.stream().count());
		logMemoryUsage();
	}

	private void logMemoryUsage() {
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		double total = 0;
		for (MemoryPoolMXBean memoryPoolMXBean : pools)
		{
			if (memoryPoolMXBean.getType() == MemoryType.HEAP)
			{
				double peakUsed = memoryPoolMXBean.getPeakUsage().getUsed() / Math.pow(10, 6);
				total = total + peakUsed;
			}
		}
		System.out.println("Total heap peak used: " + total + " MB");
	}

	@Test
	public void superUserShouldHaveAccessAllTriples() {
		IRI webid = triplestore.getSuperUserIri();

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			 shouldHaveReadAccess(webid, null, null, null, false);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time for 1000 queries is = " + (end - start) + " millis.");
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

	/*@Test
	public void superUserShouldHaveUpdateAccess() {
		RepositoryConnection conn = getFilteredConnection("http://example.org/randomUser");
		Update update = conn.prepareUpdate(SPARQL, "DELETE DATA { <> <> <> }", "cpgu:///");
		update.execute();
		conn.close();
	}*/

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