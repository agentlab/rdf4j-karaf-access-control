package ru.agentlab.rdf4j.ppo.policies.model.condition;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;

public class PPConditionResourceAsObject implements PPConditionSingle {

	private Value resourceAsObject;
	
	public PPConditionResourceAsObject(Value resourceAsObject) {
		this.resourceAsObject = resourceAsObject;
	}
	
	@Override
	public boolean handlesAccess(IRI webid, Statement statement) {
		if (statement.getObject() != null)
			if(resourceAsObject.stringValue().equals(PPManagerImpl.CUR_USER))
				return statement.getObject().equals(webid);
			else
				return statement.getObject().equals(resourceAsObject);
		else 
			return false;
	}

}
