package ru.agentlab.rdf4j.ppo.policies.model;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public interface PrivacyPreference extends Comparable<PrivacyPreference> {
	/**
	 * gets the priority of the PP
	 * @return the priority
	 */
	public float getPriority();
	/**
	 * Verifies whether the current PP allows read-access for the submitted
	 * statement and webid
	 * @param webid ID of the current user
	 * @param statement Statement to be verified
	 * @return ALLOWED if the read-access is allowed. DENIED if it is denied. NOT_HANDLED otherwise
	 */
	PPAccessAllowed allowsRead(IRI webid, Statement statement);
	/**
	 * Verifies whether the current PP allows create-access for the submitted
	 * statement and webid
	 * @param webid ID of the current user
	 * @param statement Statement to be verified
	 * @return ALLOWED if the create-access is allowed. DENIED if it is denied. NOT_HANDLED otherwise
	 */
	PPAccessAllowed allowsCreate(IRI webid, Statement statement);
	/**
	 * Verifies whether the current PP allows delete-access for the submitted
	 * statement and webid
	 * @param webid ID of the current user
	 * @param statement Statement to be verified
	 * @return ALLOWED if the delete-access is allowed. DENIED if it is denied. NOT_HANDLED otherwise
	 */
	PPAccessAllowed allowsDelete(IRI webid, Statement statement);
	/**
	 * Verifies whether the current PP allows update-access for the submitted
	 * statement and webid
	 * @param webid ID of the current user
	 * @param statement Statement to be verified
	 * @return ALLOWED if the update-access is allowed. DENIED if it is denied. NOT_HANDLED otherwise
	 */
	PPAccessAllowed allowsUpdate(IRI webid, Statement statement);
	/**
	 * Verifies whether the current PP allows access for the submitted
	 * statement and webid
	 * @param webid ID of the current user
	 * @param statement Statement to be verified
	 * @param right type of access to be verified
	 * @return ALLOWED if the create-access is allowed. DENIED if it is denied. NOT_HANDLED otherwise
	 */
	PPAccessAllowed allowsAccess(IRI webid, Statement statement, String right);
}
