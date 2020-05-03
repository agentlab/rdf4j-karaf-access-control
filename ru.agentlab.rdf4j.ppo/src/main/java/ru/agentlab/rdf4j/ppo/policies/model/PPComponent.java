package ru.agentlab.rdf4j.ppo.policies.model;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public interface PPComponent {
    /**
     * checks whether the statement is handled by the
     * current PrivacyPreference
     *
     * @param webid     ID of the current user
     * @param statement Statement which has to be checked
     * @return true if the current PP handles the statement, false otherwise
     */
    boolean handlesAccess(IRI webid, Statement statement);
}
