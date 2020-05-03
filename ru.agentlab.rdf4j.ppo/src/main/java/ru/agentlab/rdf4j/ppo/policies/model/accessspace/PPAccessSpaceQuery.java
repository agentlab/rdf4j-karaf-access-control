package ru.agentlab.rdf4j.ppo.policies.model.accessspace;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;

public class PPAccessSpaceQuery implements PPAccessSpace {

	private String hasAccessQuery;
	private RepositoryConnection connection;
	
	public PPAccessSpaceQuery(String hasAccessQuery, RepositoryConnection connection) {
		this.hasAccessQuery = hasAccessQuery;
		this.connection = connection;	
	}
	
	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		boolean handlesAccess = false;
		
		try {
			String tempHasAccessQuery = hasAccessQuery.replace("?cur_user", "<" + webid.stringValue() + ">");

			handlesAccess = connection.prepareBooleanQuery(QueryLanguage.SPARQL, PPManagerImpl.prefixes + tempHasAccessQuery).evaluate();

		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			e.printStackTrace();
		}

		return handlesAccess;
	}
}
