package ru.agentlab.rdf4j.ppo.triplestore;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.Objects;

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
        switch (operation) {
            case READ:
                aclSubQuery = aclSubQuery.replace("{OPERATION}", "acl:Read");
                break;
            case UPDATE:
                aclSubQuery = aclSubQuery.replace("{OPERATION}", "acl:Write");
                break;
        }
        rewritenQuery += ACL_PREFIXES + "\n" + query.substring(0, query.length() - 1) + "\n" + aclSubQuery + "\n" + "}";
        return rewritenQuery;
    }

    public String buildGetQuery(Resource subj, IRI pred, Value obj) {
        String subjStr = Objects.isNull(subj) ? "?subject" : "<" + subj + ">";
        String predStr = Objects.isNull(pred) ? "?predicate" : "<" + pred + ">";
        String objStr = "?object";
        if (obj instanceof Literal) {
            objStr = String.valueOf(obj);
        } else if (obj instanceof IRI) {
            objStr = "<" + obj + ">";
        }
        return "SELECT DISTINCT ?subject ?predicate ?object { ?subject ?predicate ?object " +
                "FILTER (?subject = " + subjStr + ") " +
                "FILTER (?predicate = " + predStr + ") " +
                "FILTER (?object = " + objStr + ")}";
    }

}
