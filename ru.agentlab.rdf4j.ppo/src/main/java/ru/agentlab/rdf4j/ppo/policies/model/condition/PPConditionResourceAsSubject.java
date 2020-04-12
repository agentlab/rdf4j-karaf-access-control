package ru.agentlab.rdf4j.ppo.policies.model.condition;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;

public class PPConditionResourceAsSubject implements PPConditionSingle {

	private Value resourceAsSubject;
	
	public PPConditionResourceAsSubject(Value resourceAsSubject) {
		this.resourceAsSubject = resourceAsSubject;
	}
	
	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		if (statement.getSubject() != null) {
			if(resourceAsSubject.stringValue().equals(PPManagerImpl.CUR_USER))
				return statement.getSubject().equals(webid);
			else
				return statement.getSubject().equals(resourceAsSubject);
		}
		else
			return false;
	}

}
