package ru.agentlab.rdf4j.ppo.policies.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

import ru.agentlab.rdf4j.ppo.policies.model.accesscontrol.PPAccessControl;
import ru.agentlab.rdf4j.ppo.policies.model.accesscontrol.PPAccessControlFactory;
import ru.agentlab.rdf4j.ppo.policies.model.accessspace.PPAccessSpace;
import ru.agentlab.rdf4j.ppo.policies.model.accessspace.PPAccessSpaceFactory;
import ru.agentlab.rdf4j.ppo.policies.model.condition.PPCondition;
import ru.agentlab.rdf4j.ppo.policies.model.condition.PPConditionFactory;
import ru.agentlab.rdf4j.ppo.policies.model.condition.PPConditionSingle;
import ru.agentlab.rdf4j.ppo.policies.model.restriction.PPRestriction;
import ru.agentlab.rdf4j.ppo.policies.model.restriction.PPRestrictionFactory;
import ru.agentlab.rdf4j.ppo.triplestore.TripleStore;

/**
 *
 * Gets the statements of the current policyPreference and
 * map them to a PrivacyPreference
 *
 */
public class PrivacyPreferenceFactory {

	/**
	 * create a new PrivacyPreference based on an IRI
	 * @param pp IRI of the privacyPreference which has to be mapped
	 * @param connection the active repository-connection
	 * @return new PrivacyPreference
	 * @throws Exception Syntax Error
	 */
	public static PrivacyPreference createPrivacyPreference(IRI pp, RepositoryConnection connection, String policiesContext) throws Exception {

		IRI context = connection.getValueFactory().createIRI(policiesContext);

		List<PPRestriction> restrictions = new ArrayList<PPRestriction>();
		List<PPAccessControl> accessControls = new ArrayList<PPAccessControl>();
		List<PPAccessSpace> accessSpaces = new ArrayList<PPAccessSpace>();
		PPCondition condition = null;
		float priority = 0f;

		/**
		 * iterate over all statements of the current policy and process them
		 */
		try {
			RepositoryResult<Statement> statements = connection.getStatements(pp, null, null, false, context);

			while (statements.hasNext()) {
				Statement st = statements.next();

				if(st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#appliesToResource") ||
						st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#appliesToContext")) {
					restrictions.add(PPRestrictionFactory.createRestriction(st.getPredicate(), (IRI)st.getObject()));
				}

				else if(st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#appliesToStatement")) {
					List<Value> statementElements = new ArrayList<Value>();

					try {
						RepositoryResult<Statement> stStatements = connection.getStatements((BNode)st.getObject(), null, null, false, context);

						while (stStatements.hasNext()) {
							Statement s = stStatements.next();
							if(s.getPredicate().stringValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"))
								statementElements.add(0, s.getObject());
							else if(s.getPredicate().stringValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"))
								statementElements.add(1, s.getObject());
							else if(s.getPredicate().stringValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#object"))
								statementElements.add(2, s.getObject());
						}

						if(statementElements.size() == 3) {
							Statement statement = connection.getValueFactory().createStatement((IRI)statementElements.get(0),
									(IRI)statementElements.get(1),
									statementElements.get(2));
							restrictions.add(PPRestrictionFactory.createRestriction(statement));

						}
						else
							throw new Exception("SyntaxError appliesToStatement - subject, predicate and object expected in " + pp.stringValue());

					} catch (RepositoryException e) {
						e.printStackTrace();
					}
				}

				else if(st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#hasAccess") ||
			    		st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#hasNoAccess")) {
			    	accessControls.add(PPAccessControlFactory.createAccessControl(st.getPredicate(), (IRI)st.getObject()));
				}

		    	else if(st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#hasCondition")) {
			    	try {
						RepositoryResult<Statement> conditionStatements = connection.getStatements((BNode)st.getObject(),
								null, null, false, context);

						List<PPConditionSingle> conditionList = new ArrayList<PPConditionSingle>();

						while (conditionStatements.hasNext()) {
							Statement conditionStatement = conditionStatements.next();

							conditionList.add((PPConditionSingle) PPConditionFactory.createCondition(conditionStatement.getPredicate(),
									conditionStatement.getObject(),
									connection));
						}

						if(conditionList.size() == 1)
							condition = conditionList.get(0);

						else if(conditionList.size() > 1) {
							condition = PPConditionFactory.createCondition(conditionList);
						}

					} catch (RepositoryException e) {
						e.printStackTrace();
					}
			    }

		    	else if(st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#hasAccessSpace")) {

			    	try {
						RepositoryResult<Statement> accessSpaceStatements = connection.getStatements((BNode)st.getObject(),
								null, null, false, context);

						while (accessSpaceStatements.hasNext()) {
							Statement accessSpaceStatement = accessSpaceStatements.next();
							accessSpaces.add(PPAccessSpaceFactory.createAccessSpace(accessSpaceStatement.getPredicate(),
									accessSpaceStatement.getObject(),
									connection));
						}

					} catch (RepositoryException e) {
						e.printStackTrace();
					}
			    }

		    	else if(st.getPredicate().stringValue().equals("http://vocab.deri.ie/ppo#hasPriority")) {
		    		RepositoryResult<Statement> hasPriorityStatements = connection.getStatements((BNode)st.getObject(),null,null,false,context);

		    		//should there be more priorities defined, the first one is used
	    			Statement hasPriorityStatement = hasPriorityStatements.next();
	    			if(hasPriorityStatement.getPredicate().stringValue().equals("http://purl.org/ontology/wo/weightingontology.html#weight_value"))
	    				priority = Float.parseFloat(hasPriorityStatement.getObject().stringValue());
	    			else
	    				throw new Exception("SyntaxError predicate of priority in " + pp.stringValue());
			    }
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		if (accessControls.isEmpty())
			throw new Exception("AccessControl missing in " + pp);

		return new PrivacyPreferenceImpl(accessControls, accessSpaces, condition, restrictions, priority);
	}
}