package ru.agentlab.rdf4j.ppo.policies.model.condition;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

public class PPConditionHasLiteral implements PPConditionSingle {

    private Value hasLiteral;

    public PPConditionHasLiteral(Value hasLiteral) {
        this.hasLiteral = hasLiteral;
    }

    @Override
    public boolean handlesAccess(IRI webid, Statement statement) {
        if (statement.getObject() != null)
            return statement.getObject().equals(hasLiteral);
        else
            return false;
    }

}
