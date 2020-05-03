package ru.agentlab.rdf4j.ppo.triplestore;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;

import ru.agentlab.rdf4j.ppo.policies.PPManager;


public class AccessControlSailImpl extends AbstractSail implements AccessControlSail {

    private Sail base;
    private PPManager ppManager;

    public AccessControlSailImpl(Sail parent, Repository repo, PPManager ppManager) {
        this.base = parent;
        this.ppManager = ppManager;
        try {
            ppManager.loadPrivacyPreferences(repo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PPManager getPPManager() {
        return ppManager;
    }

    @Override
    public SailConnection getConnection() {
        return base.getConnection();
    }

    @Override
    public SailConnection getConnection(IRI webid) {
        return getConnectionInternal();
    }

    @Override
    protected SailConnection getConnectionInternal() {
        return this.base.getConnection();
    }

    @Override
    public File getDataDir() {
        return this.base.getDataDir();
    }

    @Override
    public ValueFactory getValueFactory() {
        return this.base.getValueFactory();
    }

    @Override
    public void initialize() {
        this.base.init();
    }

    @Override
    public boolean isWritable() {
        return this.base.isWritable();
    }

    @Override
    public void setDataDir(File dataDir) {
        this.base.setDataDir(dataDir);
    }

    @Override
    public void shutDown() {
        this.base.shutDown();
    }


    @Override
    protected void shutDownInternal() {

    }

    @Override
    public Sail getBaseSail() {
        return base;
    }

    @Override
    public void setBaseSail(Sail base) {
        this.base = base;

    }

}
