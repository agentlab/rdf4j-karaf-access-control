package ru.agentlab.rdf4j.ppo.policies.model.condition;

import java.util.List;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public class PPConditionComplex implements PPCondition{
	List<PPConditionSingle> conditions;
	
	public PPConditionComplex(List<PPConditionSingle> conditions) {
		this.conditions = conditions;
	}
	
	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		boolean handlesAccess = true;
		
		if(conditions.isEmpty())
			return false;
		
		for(PPConditionSingle condition : conditions) {
			if(handlesAccess)
				handlesAccess = condition.handlesAccess(webid, statement);
		}
		return handlesAccess;
	}

}
