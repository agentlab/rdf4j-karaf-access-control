package ru.agentlab.rdf4j.ppo.policies.model.condition;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

public class PPConditionHasProperty implements PPConditionSingle{

	private Value hasProperty;
	
	public PPConditionHasProperty(Value hasProperty) {
		this.hasProperty = hasProperty;
	}
	
	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		if (statement.getPredicate() != null)
			return statement.getPredicate().equals(hasProperty);
		else
			return false;
	}

}
