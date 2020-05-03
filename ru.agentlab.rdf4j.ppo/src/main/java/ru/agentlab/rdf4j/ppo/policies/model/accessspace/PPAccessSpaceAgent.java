package ru.agentlab.rdf4j.ppo.policies.model.accessspace;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public class PPAccessSpaceAgent implements PPAccessSpace {

    private IRI hasAccessAgent;

    public PPAccessSpaceAgent(IRI hasAccessAgent) {
        this.hasAccessAgent = hasAccessAgent;
    }

    @Override
    public boolean handlesAccess(IRI webid, Statement statement) {
        return webid.equals(hasAccessAgent);
    }
}
