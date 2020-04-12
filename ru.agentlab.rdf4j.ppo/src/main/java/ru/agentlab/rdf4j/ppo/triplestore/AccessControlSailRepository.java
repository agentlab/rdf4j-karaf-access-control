package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.SailException;

import ru.agentlab.rdf4j.ppo.policies.PPManager;

public class AccessControlSailRepository extends SailRepository {

	private PPManager ppManager;

	public AccessControlSailRepository(AccessControlSail sail) {
		super(sail);
		ppManager = sail.getPPManager();
	}

	/**
	 * gets a filtered connection based on the webid
	 * @param webid an IRI which authenticates the requesting user
	 * @throws RepositoryException
	 * @throws SailException
	 * @return filtered connection based on the webid
	 */
	public InterceptingRepositoryConnection getConnection(IRI webid) {

		InterceptingRepositoryConnectionWrapper connection = null;
		try {
			 connection = new InterceptingRepositoryConnectionWrapper(this, super.getConnection());

			try {
				connection.begin();
				connection.addRepositoryConnectionInterceptor(new TripleFilterInterceptor(webid, ppManager));
			} finally {

				connection.commit();
			}

    	} catch (RepositoryException e) {
    		e.printStackTrace();
    	}

		return connection;
	}
}
