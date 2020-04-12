package ru.agentlab.rdf4j.ppo.policies.model.condition;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class PPConditionFactory {

	public static PPCondition createCondition(IRI predicate,Value object, RepositoryConnection connection) {  

		    if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#hasProperty")) {
		    	return new PPConditionHasProperty(object);
		    }
		    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#hasLiteral")) {
		    	return new PPConditionHasLiteral(object);
		    }
		    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#classAsObject")) {
		    	return new PPConditionClassAsObject(object, connection);
		    }
		    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#classAsSubject")) {
		    	return new PPConditionClassAsSubject(object, connection);
		    }
		    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#resourceAsObject")) {
		    	return new PPConditionResourceAsObject(object);
		    }
		    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#resourceAsSubject")) {
		    	return new PPConditionResourceAsSubject(object);
		    }
		    else return null;
	}
	
	public static PPCondition createCondition(List<PPConditionSingle> conditions) throws Exception {
		return new PPConditionComplex(conditions);
	}
}
