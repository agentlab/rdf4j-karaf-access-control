package ru.agentlab.rdf4j.ppo.policies.model.accesscontrol;

import org.eclipse.rdf4j.model.IRI;

public class PPAccessControlImpl implements PPAccessControl {

    private IRI accessType;
    private boolean grantAccess;

    public PPAccessControlImpl(IRI accessType, boolean grantAccess) {
        this.accessType = accessType;
        this.grantAccess = grantAccess;
    }

    @Override
    public IRI getAccessType() {
        return accessType;
    }

    @Override
    public boolean grantAccess() {
        return grantAccess;
    }
}
