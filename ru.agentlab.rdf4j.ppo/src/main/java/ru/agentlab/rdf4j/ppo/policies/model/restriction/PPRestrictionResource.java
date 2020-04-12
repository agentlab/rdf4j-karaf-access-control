package ru.agentlab.rdf4j.ppo.policies.model.restriction;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public class PPRestrictionResource implements PPRestriction{
	
	private IRI appliesToResource;
	
	public PPRestrictionResource (IRI appliesToResource) {
		this.appliesToResource = appliesToResource;
	}

	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		boolean handlesAccess = false;

		if(statement.getSubject() != null)
			handlesAccess = statement.getSubject().equals(appliesToResource);
		
		if(statement.getObject() != null && !handlesAccess)
			handlesAccess = statement.getObject().equals(appliesToResource);
		
		return handlesAccess;
	}
}
