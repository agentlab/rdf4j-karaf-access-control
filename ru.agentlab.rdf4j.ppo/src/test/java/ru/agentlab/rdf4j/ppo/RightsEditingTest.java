package ru.agentlab.rdf4j.ppo;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Test;
import ru.agentlab.rdf4j.ppo.policies.PPManagerImpl;

import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class RightsEditingTest extends AbstractUnitTests {

    @Test
    public void addUserToAdminGroup() {
        IRI exampleAgent = unfilteredConnection.getValueFactory().createIRI(agentUser);
        IRI predicateForRole = unfilteredConnection.getValueFactory().createIRI(member);
        IRI adminGroup = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#adminUsers");

        PPManagerImpl ppManagerImpl = new PPManagerImpl();
        ppManagerImpl.setUserAdminRights(unfilteredConnection, exampleAgent);

        Statement expected = unfilteredConnection.getValueFactory().createStatement(adminGroup, predicateForRole, exampleAgent);

        RepositoryResult<Statement> resultSet = unfilteredConnection.getStatements(adminGroup, predicateForRole, null);
        assertTrue(resultSet.stream().collect(Collectors.toList()).contains(expected));
    }

    @Test
    public void addUserToExpertGroupOne() {
        IRI exampleAgent = unfilteredConnection.getValueFactory().createIRI(agentUser);
        IRI predicateForRole = unfilteredConnection.getValueFactory().createIRI(member);
        IRI expertGroupOne = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#expertGroup1");

        PPManagerImpl ppManagerImpl = new PPManagerImpl();
        ppManagerImpl.setUserExpertGroupOne(unfilteredConnection, exampleAgent);

        Statement expected = unfilteredConnection.getValueFactory().createStatement(expertGroupOne, predicateForRole, exampleAgent);

        RepositoryResult<Statement> resultSet = unfilteredConnection.getStatements(expertGroupOne, predicateForRole, null);
        assertTrue(resultSet.stream().collect(Collectors.toList()).contains(expected));
    }

    @Test
    public void addUserToExpertUsersTwo() {
        IRI exampleAgent = unfilteredConnection.getValueFactory().createIRI(agentUser);
        IRI predicateForRole = unfilteredConnection.getValueFactory().createIRI(member);
        IRI expertUsersTwo = unfilteredConnection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#expertUsers2");

        PPManagerImpl ppManagerImpl = new PPManagerImpl();
        ppManagerImpl.setUserExpertUsersTwo(unfilteredConnection, exampleAgent);

        Statement expected = unfilteredConnection.getValueFactory().createStatement(expertUsersTwo, predicateForRole, exampleAgent);

        RepositoryResult<Statement> resultSet = unfilteredConnection.getStatements(expertUsersTwo, predicateForRole, null);
        assertTrue(resultSet.stream().collect(Collectors.toList()).contains(expected));
    }

}
