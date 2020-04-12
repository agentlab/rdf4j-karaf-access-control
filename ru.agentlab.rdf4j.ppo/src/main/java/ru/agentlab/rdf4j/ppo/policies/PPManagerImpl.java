package ru.agentlab.rdf4j.ppo.policies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.agentlab.rdf4j.ppo.policies.model.PrivacyPreference;
import ru.agentlab.rdf4j.ppo.policies.model.PrivacyPreferenceFactory;

public class PPManagerImpl implements PPManager{
	private static Logger log = LoggerFactory.getLogger(PPManagerImpl.class);

	protected String policiesContext;
	protected List<PrivacyPreference> ppList;
	protected boolean isWhitelisting = true;	// switch to false for blacklisting
	public static String prefixes = "";
	public static final String CUR_USER = "?cur_user";

	public PPManagerImpl() {
		ppList = new ArrayList<PrivacyPreference>();
	}

	@Override
	public void setWhitelisting(boolean isWhitelisting) {
		this.isWhitelisting = isWhitelisting;
	}

	@Override
	public boolean isWhitelisting() {
		return isWhitelisting;
	}

	@Override
	public void setPoliciesContext(String policiesContext) {
		this.policiesContext = policiesContext;
	}

	@Override
	public String getPoliciesContext() {
		return this.policiesContext;
	}

	@Override
	public void loadPrivacyPreferences(Repository repo) throws IOException {
		try {
			RepositoryConnection connection = repo.getConnection();
			/*
			 * store prefixes
			 */
			List<Namespace> prefixList = null;

			try {
				prefixList = connection.getNamespaces().asList();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}

			for(Namespace p : prefixList){
				prefixes += "PREFIX ";
				prefixes += p.getPrefix();
				prefixes += ": <";
				prefixes += p.getName();
				prefixes += "> ";
			}

			/*
			 * create ppList
			 */
			TupleQueryResult statements = connection.prepareTupleQuery(QueryLanguage.SPARQL, "SELECT ?s FROM <" + policiesContext + "> WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vocab.deri.ie/ppo#PrivacyPreference>} ").evaluate();

			try {
				while(statements.hasNext()) {
					BindingSet bindingSet = statements.next();
					Value valueOfS = bindingSet.getValue("s");

					IRI ppIRI = connection.getValueFactory().createIRI(valueOfS.stringValue());

					ppList.add(PrivacyPreferenceFactory.createPrivacyPreference(ppIRI, connection, policiesContext));
				}
				log.info("{} privacy preferences have been succesfully mapped", getPrivacyPreferences().size());
			} catch (Exception e) {
				e.printStackTrace();
			}

			finally {
				Collections.sort(ppList, Collections.reverseOrder()); // sort by priority
			}

		} catch (RepositoryException e) {
			log.error("Error loading policies", e);
			throw new IllegalStateException(e);
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<PrivacyPreference> getPrivacyPreferences () {
		return ppList;
	}

}
