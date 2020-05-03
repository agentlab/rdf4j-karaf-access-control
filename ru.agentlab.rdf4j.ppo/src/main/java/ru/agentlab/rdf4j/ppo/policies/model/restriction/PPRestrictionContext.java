package ru.agentlab.rdf4j.ppo.policies.model.restriction;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;

public class PPRestrictionContext implements PPRestriction {

    private IRI appliesToContext;

    public PPRestrictionContext(IRI appliesToContext) {
        this.appliesToContext = appliesToContext;
    }

    @Override
    public boolean handlesAccess(IRI webid, Statement statement) {
        IRI context = (IRI) statement.getContext();

        if (context != null)
            return context.equals(appliesToContext);
        else
            return false;

    }

}
