package ru.agentlab.rdf4j.ppo.policies;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.agentlab.rdf4j.ppo.policies.model.PPAccessAllowed;
import ru.agentlab.rdf4j.ppo.policies.model.PrivacyPreference;
import ru.agentlab.rdf4j.ppo.policies.model.PrivacyPreferenceFactory;
import ru.agentlab.rdf4j.ppo.policies.model.accesscontrol.PPAccessControl;
import ru.agentlab.rdf4j.ppo.policies.model.accesscontrol.PPAccessControlFactory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PPManagerImpl implements PPManager {
    private static Logger log = LoggerFactory.getLogger(PPManagerImpl.class);

    protected String policiesContext;
    protected List<PrivacyPreference> ppList;
    protected boolean isWhitelisting = true;    // switch to false for blacklisting
    public static String prefixes = "";
    public static final String CUR_USER = "?cur_user";

    public PPManagerImpl() {
        ppList = new ArrayList<>();
    }

    @Override
    public void loadPrivacyPreferences(Repository repo) {
        try {
            RepositoryConnection connection = repo.getConnection();
            /*
             * store prefixes
             */
            List<Namespace> prefixList;

            prefixList = Iterations.asList(connection.getNamespaces());

            for (Namespace p : prefixList) {
                prefixes += "PREFIX ";
                prefixes += p.getPrefix();
                prefixes += ": <";
                prefixes += p.getName();
                prefixes += "> ";
            }

            /*
             * create ppList
             */
            TupleQueryResult statements = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    "SELECT ?s FROM <" + policiesContext + "> " +
                            "WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                            "<http://vocab.deri.ie/ppo#PrivacyPreference>} ")
                    .evaluate();

            while (statements.hasNext()) {
                BindingSet bindingSet = statements.next();
                Value valueOfS = bindingSet.getValue("s");

                IRI ppIRI = connection.getValueFactory().createIRI(valueOfS.stringValue());

                ppList.add(PrivacyPreferenceFactory.createPrivacyPreference(ppIRI, connection, policiesContext));
            }
            log.info("{} privacy preferences have been succesfully mapped", getPrivacyPreferences().size());

        } catch (RepositoryException e) {
            log.error("Error loading policies", e);
            throw new IllegalStateException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserExpertGroupOne(RepositoryConnection connection, IRI webid, boolean presence) {
        IRI adminIri = connection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#expertGroup1");
        editUserRole(connection, webid, adminIri, presence);
    }

    public void setUserExpertUsersTwo(RepositoryConnection connection, IRI webid, boolean presence) {
        IRI adminIri = connection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#expertUsers2");
        editUserRole(connection, webid, adminIri, presence);
    }

    public void setUserAdminGroup(RepositoryConnection connection, IRI webid, boolean presence) {
        IRI adminIri = connection.getValueFactory().createIRI("http://cpgu.kbpm.ru/ns/rm/users#adminUsers");
        editUserRole(connection, webid, adminIri, presence);
    }

    private void editUserRole(RepositoryConnection connection, IRI webid, IRI userRole, boolean presence) {
        String memberOf = "http://xmlns.com/foaf/0.1/member";
        IRI predicateForRole = connection.getValueFactory().createIRI(memberOf);
        Statement statement = connection.getValueFactory().createStatement(
                userRole,
                predicateForRole,
                webid);
        if (presence) {
            connection.add(statement);
        } else {
            connection.remove(statement);
        }
        connection.commit();
    }

    @Override
    public List<PrivacyPreference> getPrivacyPreferences() {
        return ppList;
    }

}
