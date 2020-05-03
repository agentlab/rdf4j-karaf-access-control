package ru.agentlab.rdf4j.ppo.triplestore;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.event.base.RepositoryConnectionInterceptorAdapter;

import ru.agentlab.rdf4j.ppo.policies.PPManager;
import ru.agentlab.rdf4j.ppo.policies.model.PPAccessAllowed;
import ru.agentlab.rdf4j.ppo.policies.model.PrivacyPreference;

public class TripleFilterInterceptor extends RepositoryConnectionInterceptorAdapter {

    private IRI webid;
    private PPManager ppManager;

    public TripleFilterInterceptor(IRI webid, PPManager ppManager) {
        this.webid = webid;
        this.ppManager = ppManager;
    }

    /**
     * verifies the submitted statement for read-access
     *
     * @param st statement to be verified
     * @return true if read-access is granted, false otherwise
     */
    public boolean verifyReadStatement(Statement st) {
        return verifyStatement(st, TripleStoreAction.READ);
    }

    /**
     * verifies the submitted statement for create-access
     *
     * @param st statement to be verified
     * @return true if create-access is granted, false otherwise
     */
    public boolean verifyCreateStatement(Statement st) {
        return verifyStatement(st, TripleStoreAction.CREATE);
    }

    /**
     * verifies the submitted statement for delete-access
     *
     * @param st statement to be verified
     * @return true if delete-access is granted, false otherwise
     */
    public boolean verifyDeleteStatement(Statement st) {
        return verifyStatement(st, TripleStoreAction.DELETE);
    }

    /**
     * verifies the submitted statement for update-access
     *
     * @param st statement to be verified
     * @return true if update-access is granted, false otherwise
     */
    public boolean verifyUpdateStatement(Statement reference, Statement st) {
        boolean refMayUpdate = verifyStatement(reference, TripleStoreAction.UPDATE);

        if (refMayUpdate && reference.getSubject().equals(st.getSubject()) &&
                reference.getPredicate().equals(st.getPredicate()))
            if (reference.getContext() == null & st.getContext() == null || reference.getContext().equals(st.getContext()))
                return true;
            else
                return false;
        else
            return false;
    }

    /**
     * verifies the submitted statement for access
     *
     * @param st     statement to be verified
     * @param action action type to be verified
     * @return true if access for the action is granted, false otherwise
     */
    public boolean verifyStatement(Statement st, TripleStoreAction action) {
        List<PrivacyPreference> ppList = ppManager.getPrivacyPreferences();

        for (PrivacyPreference preference : ppList) {
            PPAccessAllowed ppAccess = PPAccessAllowed.NOT_HANDLED;

            switch (action) {
                case READ:
                    ppAccess = preference.allowsRead(webid, st);
                    break;
                case CREATE:
                    ppAccess = preference.allowsCreate(webid, st);
                    break;
                case DELETE:
                    ppAccess = preference.allowsDelete(webid, st);
                    break;
                case UPDATE:
                    ppAccess = preference.allowsUpdate(webid, st);
                    break;
            }

            if (ppAccess != PPAccessAllowed.NOT_HANDLED) {
                switch (ppAccess) {
                    case ALLOWED:
                        return true;
                    case DENIED:
                        return false;
                    default:
                        break;
                }
            }
        }
        return !ppManager.isWhitelisting();
    }
}