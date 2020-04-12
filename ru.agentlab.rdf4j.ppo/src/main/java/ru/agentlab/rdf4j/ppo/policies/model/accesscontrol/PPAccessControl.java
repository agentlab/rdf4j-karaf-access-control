package ru.agentlab.rdf4j.ppo.policies.model.accesscontrol;

import org.eclipse.rdf4j.model.IRI;

public interface PPAccessControl {
	/**
	 * gets the type of accessControl
	 * @return create,read,update or delete
	 */
	IRI getAccessType();

	/**
	 * gets whether access is granted or denied
	 * @return true if granted, false otherwise
	 */
	boolean grantAccess();
}