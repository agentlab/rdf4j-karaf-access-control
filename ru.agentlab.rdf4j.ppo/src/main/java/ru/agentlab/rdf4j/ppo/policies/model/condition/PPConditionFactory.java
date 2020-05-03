package ru.agentlab.rdf4j.ppo.policies.model.condition;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PPConditionFactory {

    public static PPCondition createCondition(IRI predicate, Value object, RepositoryConnection connection) {

        switch (predicate.stringValue()) {
            case "http://vocab.deri.ie/ppo#hasProperty":
                return new PPConditionHasProperty(object);
            case "http://vocab.deri.ie/ppo#hasLiteral":
                return new PPConditionHasLiteral(object);
            case "http://vocab.deri.ie/ppo#classAsObject":
                return new PPConditionClassAsObject(object, connection);
            case "http://vocab.deri.ie/ppo#classAsSubject":
                return new PPConditionClassAsSubject(object, connection);
            case "http://vocab.deri.ie/ppo#resourceAsObject":
                return new PPConditionResourceAsObject(object);
            case "http://vocab.deri.ie/ppo#resourceAsSubject":
                return new PPConditionResourceAsSubject(object);
            default:
                return null;
        }
    }

    public static PPCondition createCondition(List<PPConditionSingle> conditions) {
        return new PPConditionComplex(conditions);
    }
}
