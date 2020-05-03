package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Test;
import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;

import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RightsEditingTest extends AbstractUnitTests {

    @Test
    public void addUserToAdminGroupAndRemoveThen() {
        IRI exampleAgent = unfilteredConnection.getValueFactory().createIRI(agentUser);
        IRI predicateForRole = unfilteredConnection.getValueFactory().createIRI(member);
        IRI adminGroup = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#adminUsers");

        PPManagerImpl ppManagerImpl = new PPManagerImpl();
        ppManagerImpl.setUserAdminGroup(unfilteredConnection, exampleAgent, true);
        Statement expectedAfterAdding = unfilteredConnection.getValueFactory().createStatement(adminGroup, predicateForRole, exampleAgent);
        RepositoryResult<Statement> resultSetAfterAdding = unfilteredConnection.getStatements(adminGroup, predicateForRole, null);
        assertTrue(resultSetAfterAdding.stream().collect(Collectors.toList()).contains(expectedAfterAdding));

        ppManagerImpl.setUserAdminGroup(unfilteredConnection, exampleAgent, false);
        RepositoryResult<Statement> resultSetAfterRemoving = unfilteredConnection.getStatements(adminGroup, predicateForRole, null);
        assertFalse(resultSetAfterRemoving.stream().collect(Collectors.toList()).contains(expectedAfterAdding));
    }

    @Test
    public void addUserToExpertGroupOneAndRemoveThen() {
        IRI exampleAgent = unfilteredConnection.getValueFactory().createIRI(agentUser);
        IRI predicateForRole = unfilteredConnection.getValueFactory().createIRI(member);
        IRI expertGroupOne = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#expertGroup1");

        PPManagerImpl ppManagerImpl = new PPManagerImpl();
        ppManagerImpl.setUserExpertGroupOne(unfilteredConnection, exampleAgent, true);
        Statement expectedAfterAdding = unfilteredConnection.getValueFactory().createStatement(expertGroupOne, predicateForRole, exampleAgent);
        RepositoryResult<Statement> resultSetAfterAdding = unfilteredConnection.getStatements(expertGroupOne, predicateForRole, null);
        assertTrue(resultSetAfterAdding.stream().collect(Collectors.toList()).contains(expectedAfterAdding));

        ppManagerImpl.setUserExpertGroupOne(unfilteredConnection, exampleAgent, false);
        RepositoryResult<Statement> resultSetAfterRemoving = unfilteredConnection.getStatements(expertGroupOne, predicateForRole, null);
        assertFalse(resultSetAfterRemoving.stream().collect(Collectors.toList()).contains(expectedAfterAdding));
    }

    @Test
    public void addUserToExpertUsersTwoAndRemoveThen() {
        IRI exampleAgent = unfilteredConnection.getValueFactory().createIRI(agentUser);
        IRI predicateForRole = unfilteredConnection.getValueFactory().createIRI(member);
        IRI expertUsersTwo = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#expertUsers2");

        PPManagerImpl ppManagerImpl = new PPManagerImpl();
        ppManagerImpl.setUserExpertUsersTwo(unfilteredConnection, exampleAgent, true);
        Statement expectedAfterAdding = unfilteredConnection.getValueFactory().createStatement(expertUsersTwo, predicateForRole, exampleAgent);
        RepositoryResult<Statement> resultSetAfterAdding = unfilteredConnection.getStatements(expertUsersTwo, predicateForRole, null);
        assertTrue(resultSetAfterAdding.stream().collect(Collectors.toList()).contains(expectedAfterAdding));

        ppManagerImpl.setUserExpertUsersTwo(unfilteredConnection, exampleAgent, false);
        RepositoryResult<Statement> resultSetAfterRemoving = unfilteredConnection.getStatements(expertUsersTwo, predicateForRole, null);
        assertFalse(resultSetAfterRemoving.stream().collect(Collectors.toList()).contains(expectedAfterAdding));
    }
}
