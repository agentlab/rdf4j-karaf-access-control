/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.event.RepositoryConnectionInterceptor;
import org.eclipse.rdf4j.repository.event.base.InterceptingRepositoryWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Wrapper that notifies interceptors of events on RepositoryConnections before
 * they happen. Any interceptor can block the operation by returning true from
 * the relevant notification method. To do so will also cause the notification
 * process to stop, i.e. no other interceptors will be notified. The order in
 * which interceptors are notified is unspecified.
 *
 * @author Herko ter Horst
 * @see InterceptingRepositoryWrapper
 */
public class InterceptingRepositoryConnectionWrapper extends org.eclipse.rdf4j.repository.base.RepositoryConnectionWrapper
        implements InterceptingRepositoryConnection {

    /*-----------*
     * Variables *
     *-----------*/

    private boolean activated;

    private Set<RepositoryConnectionInterceptor> interceptors = new CopyOnWriteArraySet<>();

    public void setQueryRewriter(QueryRewriter queryRewriter) {
        this.queryRewriter = queryRewriter;
    }

    private QueryRewriter queryRewriter;

    /*--------------*
     * Construcotrs *
     *--------------*/

    public InterceptingRepositoryConnectionWrapper(Repository repository, RepositoryConnection connection) {
        super(repository, connection);
    }

    /*---------*
     * Methods *
     *---------*/

    /**
     * Registers a <tt>RepositoryConnectionInterceptor</tt> that will receive notifications of operations that are
     * performed on this connection.
     */
    @Override
    public void addRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor) {
        interceptors.add(interceptor);
        activated = true;
    }

    /**
     * Removes a registered <tt>RepositoryConnectionInterceptor</tt> from this connection.
     */
    @Override
    public void removeRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor) {
        interceptors.remove(interceptor);
        activated = !interceptors.isEmpty();
    }

    @Override
    protected boolean isDelegatingAdd() {
        return !activated;
    }

    @Override
    protected boolean isDelegatingRemove() {
        return !activated;
    }

    @Override
    public void addWithoutCommit(Resource subject, IRI predicate, Value object, Resource... contexts)
            throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.add(getDelegate(), subject, predicate, object, contexts);
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().add(subject, predicate, object, contexts);
        }
    }

    @Override
    public void clear(Resource... contexts) throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.clear(getDelegate(), contexts);
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().clear(contexts);
        }
    }

    @Override
    public void begin() throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.begin(getDelegate());
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            super.begin();
        }
    }

    @Override
    public void close() throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.close(getDelegate());
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            super.close();
        }
    }

    @Override
    public void commit() throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.commit(getDelegate());
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().commit();
        }
    }

    @Override
    public void removeWithoutCommit(Resource subject, IRI predicate, Value object, Resource... contexts)
            throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.remove(getDelegate(), subject, predicate, object, contexts);
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().remove(subject, predicate, object, contexts);

        }
    }

    @Override
    public void removeNamespace(String prefix) throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.removeNamespace(getDelegate(), prefix);
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().removeNamespace(prefix);
        }
    }

    @Override
    public void clearNamespaces() throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.clearNamespaces(getDelegate());
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().clearNamespaces();
        }
    }

    @Override
    public void rollback() throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.rollback(getDelegate());
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().rollback();
        }
    }

    @Override
    @Deprecated
    public void setAutoCommit(boolean autoCommit) throws RepositoryException {
        boolean denied = false;
        boolean wasAutoCommit = isAutoCommit();
        if (activated && wasAutoCommit != autoCommit) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.setAutoCommit(getDelegate(), autoCommit);
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().setAutoCommit(autoCommit);

        }
    }

    @Override
    public void setNamespace(String prefix, String name) throws RepositoryException {
        boolean denied = false;
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                denied = interceptor.setNamespace(getDelegate(), prefix, name);
                if (denied) {
                    break;
                }
            }
        }
        if (!denied) {
            getDelegate().setNamespace(prefix, name);
        }
    }

    @Override
    public Update prepareUpdate(final QueryLanguage ql, final String update, final String baseURI)
            throws MalformedQueryException, RepositoryException {
        if (activated) {
            return new Update() {

                private String rewritenQuery = queryRewriter.addUserRightsParams(update, TripleStoreAction.UPDATE);

                private final RepositoryConnection conn = getDelegate();

                private final Update delegate = conn.prepareUpdate(ql, rewritenQuery, baseURI);

                @Override
                public void execute() throws UpdateExecutionException {
                    boolean denied = false;
                    if (activated) {
                        for (RepositoryConnectionInterceptor interceptor : interceptors) {
                            denied = interceptor.execute(conn, ql, rewritenQuery, baseURI, delegate);
                            if (denied) {
                                break;
                            }
                        }
                    }
                    if (!denied) {
                        delegate.execute();
                    }
                }

                @Override
                public void setBinding(String name, Value value) {
                    delegate.setBinding(name, value);
                }

                @Override
                public void removeBinding(String name) {
                    delegate.removeBinding(name);
                }

                @Override
                public void clearBindings() {
                    delegate.clearBindings();
                }

                @Override
                public BindingSet getBindings() {
                    return delegate.getBindings();
                }

                @Override
                public void setDataset(Dataset dataset) {
                    delegate.setDataset(dataset);
                }

                @Override
                public Dataset getDataset() {
                    return delegate.getDataset();
                }

                @Override
                public void setIncludeInferred(boolean includeInferred) {
                    delegate.setIncludeInferred(includeInferred);
                }

                @Override
                public boolean getIncludeInferred() {
                    return delegate.getIncludeInferred();
                }

                @Override
                public void setMaxExecutionTime(int maxExecTime) {
                    delegate.setMaxExecutionTime(maxExecTime);
                }

                @Override
                public int getMaxExecutionTime() {
                    return delegate.getMaxExecutionTime();
                }
            };
        } else {
            return getDelegate().prepareUpdate(ql, update, baseURI);
        }
    }

    /**
     * Extensions
     */

    @Override
    protected boolean isDelegatingRead() throws RepositoryException {
        return !activated;
    }

    @Override
    public RepositoryResult<Statement> getStatements(Resource subj, IRI pred,
                                                     Value obj, boolean includeInferred, Resource... contexts)
            throws RepositoryException {

        String query = QueryBuilder.buildGetQuery(subj, pred, obj);
        String queryWithAcl = queryRewriter.addUserRightsParams(query, TripleStoreAction.READ);
        // RepositoryResult<Statement> unfilteredStatements = getDelegate().getStatements(subj, pred, obj, includeInferred, contexts);
        TupleQueryResult tupleQueryResult = getDelegate().prepareTupleQuery(queryWithAcl).evaluate();
        List<BindingSet> bindingSets = tupleQueryResult.stream().collect(Collectors.toList());
        List<Statement> statements = new ArrayList<>();
        for (BindingSet bindings : bindingSets) {
            IRI subject = (IRI) bindings.getBinding("subject").getValue();
            IRI predicate = (IRI) bindings.getBinding("predicate").getValue();
            Value object = bindings.getBinding("object").getValue();
            Statement statement = getDelegate().getValueFactory().createStatement(subject, predicate, object);
            statements.add(statement);
        }
        return new RepositoryResult<>(new CloseableIteratorIteration<>(statements.iterator()));
    }

    @Override
    public void remove(Resource subject, IRI predicate, Value object,
                       Resource... contexts) throws RepositoryException {

        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                TripleFilterInterceptor filterInterceptor = (TripleFilterInterceptor) interceptor;

                if (contexts.length > 0)
                    for (Resource c : contexts) {
                        Statement st = getValueFactory().createStatement(subject, predicate, object, c);

                        if (filterInterceptor.verifyDeleteStatement(st))
                            getDelegate().remove(st, contexts);
                    }
                else {

                    Statement st = getValueFactory().createStatement(subject, predicate, object);
                    if (filterInterceptor.verifyDeleteStatement(st))
                        getDelegate().remove(st, contexts);
                }
            }
        }
    }

    @Override
    public void update(Statement reference, Statement st) throws RepositoryException {
        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                TripleFilterInterceptor filterInterceptor = (TripleFilterInterceptor) interceptor;

                if (filterInterceptor.verifyUpdateStatement(reference, st)) {
                    getDelegate().remove(reference);
                    getDelegate().add(st);
                }
            }
        }
    }

    @Override
    public void add(Resource subject, IRI predicate, Value object,
                    Resource... contexts) throws RepositoryException {

        if (activated) {
            for (RepositoryConnectionInterceptor interceptor : interceptors) {
                TripleFilterInterceptor filterInterceptor = (TripleFilterInterceptor) interceptor;

                if (contexts.length > 0)
                    for (Resource c : contexts) {
                        Statement st = getValueFactory().createStatement(subject, predicate, object, c);

                        if (filterInterceptor.verifyCreateStatement(st))
                            getDelegate().add(st, contexts);
                    }
                else {
                    Statement st = getValueFactory().createStatement(subject, predicate, object);

                    if (filterInterceptor.verifyCreateStatement(st))
                        getDelegate().add(st, contexts);
                }
            }
        }
    }


}
