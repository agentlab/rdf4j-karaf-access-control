package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;

public interface TripleStore {
	/**
	 * Filtered connection for current user
	 * @param webid url for current user
	 * @return returns connection containing statements for current user
	 */
	InterceptingRepositoryConnection getConnection(IRI webid);

	/**
	 * Filtered connection for anonymous
	 * @return returns connection containing statements for anonymous
	 */
	InterceptingRepositoryConnection getConnection();

	/**
	 * Filtered connection for superuser
	 * @return returns connection containing statements for superuser
	 */
	InterceptingRepositoryConnection getSuperUserConnection();

	long size() throws RepositoryException;
}
