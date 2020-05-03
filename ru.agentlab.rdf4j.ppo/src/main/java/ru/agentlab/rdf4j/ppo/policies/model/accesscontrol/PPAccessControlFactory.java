package ru.agentlab.rdf4j.ppo.policies.model.accesscontrol;

import org.eclipse.rdf4j.model.IRI;

public class PPAccessControlFactory {

    public static PPAccessControl createAccessControl(IRI predicate, IRI object) {

        if (predicate.stringValue().equals("http://vocab.deri.ie/ppo#hasAccess")) {
            return new PPAccessControlImpl(object, true);
        } else if (predicate.stringValue().equals("http://vocab.deri.ie/ppo#hasNoAccess")) {
            return new PPAccessControlImpl(object, false);
        } else return null;
    }
}
