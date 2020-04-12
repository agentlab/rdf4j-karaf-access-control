package ru.agentlab.rdf4j.ppo.policies.model.restriction;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public class PPRestrictionStatement implements PPRestriction {
	
	private Statement appliesToStatement;
	
	PPRestrictionStatement (Statement statement) {
		appliesToStatement = statement;
	}

	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		return statement.equals(appliesToStatement);
	}

}
