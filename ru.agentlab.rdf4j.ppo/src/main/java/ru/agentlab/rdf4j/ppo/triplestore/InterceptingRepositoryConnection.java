package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *
 * @author Franz Brandstätter
 */
public interface InterceptingRepositoryConnection extends org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection {
	
	/**
	 * add update-support to the connection
	 * @param reference statement that has to be updated
	 * @param st statement to replace reference
	 * @throws RepositoryException
	 */
	public void update(Statement reference, Statement st) throws RepositoryException;

}

