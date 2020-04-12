package ru.agentlab.rdf4j.ppo.policies.model;

import java.util.List;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

import ru.agentlab.rdf4j.ppo.policies.model.accesscontrol.PPAccessControl;
import ru.agentlab.rdf4j.ppo.policies.model.accessspace.PPAccessSpace;
import ru.agentlab.rdf4j.ppo.policies.model.condition.PPCondition;
import ru.agentlab.rdf4j.ppo.policies.model.restriction.PPRestriction;

public class PrivacyPreferenceImpl implements PrivacyPreference {
	
	private List<PPAccessControl> accessControls;
	private List<PPAccessSpace> accessSpaces;
	private PPCondition condition;
	private List<PPRestriction> restrictions;
	private float priority;
	
	public PrivacyPreferenceImpl(List<PPAccessControl> accessControls, 
			List<PPAccessSpace> accessSpaces,
			PPCondition condition,
			List<PPRestriction> restrictions,
			float priority) {
		
		this.accessControls = accessControls;
		this.accessSpaces = accessSpaces;
		this.condition = condition;
		this.restrictions = restrictions;
		this.priority = priority;
	}
	
	@Override
	public float getPriority() {
		return priority;
	}

	@Override
	public PPAccessAllowed allowsRead(IRI webid, Statement statement) {
		return allowsAccess(webid,statement, "http://www.w3.org/ns/auth/acl#Read");
	}

	@Override
	public PPAccessAllowed allowsCreate(IRI webid, Statement statement) {
		return allowsAccess(webid,statement,"http://vocab.deri.ie/ppo#Create");
	}

	@Override
	public PPAccessAllowed allowsDelete(IRI webid, Statement statement) {
		return allowsAccess(webid,statement,"http://vocab.deri.ie/ppo#Delete");
	}

	@Override
	public PPAccessAllowed allowsUpdate(IRI webid, Statement statement) {
		return allowsAccess(webid,statement,"http://vocab.deri.ie/ppo#Update");
	}

	@Override
	public PPAccessAllowed allowsAccess(IRI webid, Statement statement, String right) {
		
		boolean ppIsHandlingAccess = true;
		boolean userHasAccessRight = true;
		// if there is a restriction or condition, then it's not applying for all triples
		// and therefore the restrictions and conditions have to be verified
		if(!restrictions.isEmpty() || condition != null) {
			ppIsHandlingAccess = false; 
		
			for(PPRestriction restriction : restrictions) {
				
				ppIsHandlingAccess = restriction.handlesAccess(webid, statement);
				
				if(ppIsHandlingAccess)
					break;
			}
			
			if((ppIsHandlingAccess || restrictions.isEmpty()) && condition != null)
				ppIsHandlingAccess = condition.handlesAccess(webid, statement);
	
			if(!ppIsHandlingAccess)
				return PPAccessAllowed.NOT_HANDLED;
		}
		
		//if no accessSpace is defined, pp applies for all users
		if(!accessSpaces.isEmpty()) {
			userHasAccessRight = false;
		
			for(PPAccessSpace accessSpace : accessSpaces) {
				userHasAccessRight = accessSpace.handlesAccess(webid, statement);
				if(userHasAccessRight)
					break;
			}
			
			if(!userHasAccessRight)
				return PPAccessAllowed.NOT_HANDLED;
		}
		
		for(PPAccessControl accessControl : accessControls) {
			if(accessControl.getAccessType().stringValue().equals(right))
				return accessControl.grantAccess() ? PPAccessAllowed.ALLOWED : PPAccessAllowed.DENIED;
		}
		
		return PPAccessAllowed.NOT_HANDLED;
	}

	@Override
	public int compareTo(PrivacyPreference o) {
		if(this.getPriority() < o.getPriority())
			return -1;
		else if(this.getPriority() == o.getPriority())
			return 0;
		else
			return 1;
	}
}