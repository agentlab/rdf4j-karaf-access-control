package ru.agentlab.rdf4j.ppo.policies.model.condition;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.memory.model.MemLiteral;

public class PPConditionClassAsObject implements PPConditionSingle {

    private Value classAsObject;
    private RepositoryConnection connection;

    public PPConditionClassAsObject(Value classAsObject, RepositoryConnection connection) {
        this.classAsObject = classAsObject;
        this.connection = connection;
    }

    @Override
    public boolean handlesAccess(IRI webid, Statement statement) {

        //object must not be Literal as it should be a class
        if (statement.getObject().getClass().equals(MemLiteral.class))
            return false;

        boolean handlesAccess = false;
        IRI type = connection.getValueFactory().createIRI("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
        String query = "ASK {<" + statement.getObject().stringValue() + "> " + type + " <" + classAsObject + ">}";

        try {
            handlesAccess = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate();
        } catch (QueryEvaluationException | MalformedQueryException | RepositoryException e) {
            e.printStackTrace();
        }

        return handlesAccess;
    }
}