package ru.agentlab.rdf4j.ppo.policies.model.condition;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class PPConditionClassAsSubject implements PPConditionSingle {

	private Value classAsSubject;
	private RepositoryConnection connection;
	
	public PPConditionClassAsSubject(Value classAsSubject, RepositoryConnection connection) {
		this.classAsSubject = classAsSubject;
		this.connection = connection;
	}
	
	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		
		boolean handlesAccess = false;
		IRI type = connection.getValueFactory().createIRI("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		String query = "ASK {<" + statement.getSubject().stringValue() + "> " + type + " <" + classAsSubject + ">}";
		
		try {
			handlesAccess = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		
		return handlesAccess;
	}

}
