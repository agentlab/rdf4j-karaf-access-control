package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.StackableSail;

import ru.agentlab.rdf4j.ppo.policies.PPManager;

public interface AccessControlSail extends StackableSail {

    SailConnection getConnection(IRI webid) throws SailException;

    PPManager getPPManager();

}
