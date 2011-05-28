package com.tinkerpop.rexster.kibbles.sparql;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.extension.*;
import info.aduna.iteration.CloseableIteration;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple extension that just echoes back the string parameter passed in.
 */
@ExtensionNaming(namespace = SparqlExtension.EXTENSION_NAMESPACE, name = SparqlExtension.EXTENSION_NAME)
public class SparqlExtension extends AbstractRexsterExtension {

    public static final String EXTENSION_NAMESPACE = "tp";
    public static final String EXTENSION_NAME = "sparql";

    @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(description = "execute SPARQL queries against a SAIL graph.")
    public ExtensionResponse evaluateSparql(@RexsterContext RexsterResourceContext context,
                                          @RexsterContext Graph graph,
                                          @ExtensionRequestParameter(name="query", description="a SPARQL query") String queryString,
                                          @ExtensionRequestParameter(name="baseuri", description="the base URI for the query") String baseUri) {
        if (queryString == null || queryString.isEmpty()) {
            ExtensionMethod extMethod = context.getExtensionMethod();
            return ExtensionResponse.error(
                    "the query parameter cannot be empty",
                    null,
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    null,
                    generateErrorJson(extMethod.getExtensionApiAsJson()));
        }

        if (baseUri == null || baseUri.isEmpty()) {
            ExtensionMethod extMethod = context.getExtensionMethod();
            return ExtensionResponse.error(
                    "the baseuri parameter cannot be empty",
                    null,
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    null,
                    generateErrorJson(extMethod.getExtensionApiAsJson()));
        }

        if (!(graph instanceof IndexableGraph)) {
            throw new WebApplicationException();
        }

        Sail sail = new GraphSail((IndexableGraph) graph);
        SailConnection sc;

        try {
            sc = sail.getConnection();
        } catch (SailException se) {
            throw new WebApplicationException(se);
        }

        SPARQLParser parser = new SPARQLParser();
        CloseableIteration<? extends BindingSet, QueryEvaluationException> sparqlResults;

        try {
            ParsedQuery query = parser.parseQuery(queryString, baseUri);
            sparqlResults = sc.evaluate(query.getTupleExpr(), query.getDataset(), new EmptyBindingSet(), false);
            JSONArray jsonArray = new JSONArray();

            while (sparqlResults.hasNext()) {
                jsonArray.put(sparqlResults.next().toString());
            }

            HashMap<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put(Tokens.SUCCESS, true);
            resultMap.put(Tokens.RESULTS, jsonArray);

            JSONObject resultObject = new JSONObject(resultMap);
            return ExtensionResponse.ok(resultObject);

        } catch (Exception mqe) {
            throw new WebApplicationException(mqe);
        }

    }
}
