package ru.agentlab.rdf4j.ppo.policies.model.accessspace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class PPAccessSpaceFactory {

	public static PPAccessSpace createAccessSpace(IRI predicate,Value object, RepositoryConnection connection) {
		
	    if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#hasAccessQuery")) {
	    	return new PPAccessSpaceQuery(object.stringValue(), connection);
	    }
	    else if(predicate.stringValue().equals("http://vocab.deri.ie/ppo#hasAccessAgent")) {
	    	return new PPAccessSpaceAgent((IRI) object);
	    }  
		    
		return null;
	}
}
