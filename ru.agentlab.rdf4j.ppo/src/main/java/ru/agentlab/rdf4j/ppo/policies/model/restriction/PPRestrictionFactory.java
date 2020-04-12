package ru.agentlab.rdf4j.ppo.policies.model.restriction;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public class PPRestrictionFactory {
	public static PPRestriction createRestriction(Statement statement) {
		
		return new PPRestrictionStatement(statement);
	}

	public static PPRestriction createRestriction(IRI predicate, IRI object) {
		if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#appliesToResource")) {
	    	return new PPRestrictionResource(object);
	    }
	    
	    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#appliesToContext")) {
	    	return new PPRestrictionContext(object);
	    }
	 
	    else 
	    	return null;
	}
}
