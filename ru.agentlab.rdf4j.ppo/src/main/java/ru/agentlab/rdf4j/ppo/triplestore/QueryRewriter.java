package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.IRI;

public class QueryRewriter {

    private static final String ACL_PREFIXES =
            "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX pporoles:<https://agentlab.ru/onto/ppo-roles#>\n" +
                    "PREFIX ppo:     <http://vocab.deri.ie/ppo#>\n" +
                    "PREFIX acl: <http://www.w3.org/ns/auth/acl#>\n";

    private static final String ACL_ADDITION =
            "  ?eIri0 rdf:type ?type.\n" +
                    "  ?role pporoles:roleAgent <{USER}>.\n" +
                    "  ?role pporoles:rolePolicy ?policies.\n" +
                    "  ?policies ppo:hasCondition ?conditions.\n" +
                    "  ?conditions ?classAsSubjectOrObject ?type.\n" +
                    "  ?policies ppo:hasAccess {OPERATION}";

    private IRI webId;

    public QueryRewriter(IRI webId) {
        this.webId = webId;
    }

    public String addUserRightsParams(String query, TripleStoreAction operation) {
        String rewritenQuery = "";
        String aclSubQuery = ACL_ADDITION.replace("{USER}", String.valueOf(webId));
        if (operation == TripleStoreAction.READ) {
            aclSubQuery = aclSubQuery.replace("{OPERATION}", "acl:Read");
        } else if (operation == TripleStoreAction.UPDATE) {
            aclSubQuery = aclSubQuery.replace("{OPERATION}", "acl:Write");
        }
        rewritenQuery += ACL_PREFIXES + "\n" + query.substring(0, query.length() - 1) + "\n" + aclSubQuery + "\n" + "}";
        return rewritenQuery;
    }

}
