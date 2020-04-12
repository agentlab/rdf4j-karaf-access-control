package ru.agentlab.rdf4j.ppo.policies;

import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.repository.Repository;

import ru.agentlab.rdf4j.ppo.policies.model.PrivacyPreference;

public interface PPManager {
	/**
	 * get all privacyPreferences from the triplestore
	 * @return
	 */
	List<PrivacyPreference> getPrivacyPreferences ();

	/**
	 * All PPs from triplestore are loaded and each
	 * is stored as a PrivacyPreference-object so each PP can
	 * be processed by the PPManager
	 */
	void loadPrivacyPreferences(Repository repo) throws IOException;

	void setPoliciesContext(String policiesContext);

	String getPoliciesContext();
	/**
	 * sets the default behavior for pp-verification
	 * @param isWhitelisting
	 */
	void setWhitelisting(boolean isWhitelisting);

	/**
	 * gets the default behavior for pp-verification
	 * @return true for whitelisting, false otherwise
	 */
	boolean isWhitelisting();
}
