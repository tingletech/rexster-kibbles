package com.tinkerpop.rexster.kibbles.sparql;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.oupls.sail.GraphSail;
import com.tinkerpop.rexster.ElementJSONObject;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.Tokens;
import com.tinkerpop.rexster.extension.*;
import com.tinkerpop.rexster.util.ElementJSONHelper;
import com.tinkerpop.rexster.util.RequestObjectHelper;
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
import java.util.List;
import java.util.Map;

/**
 * A simple extension that just echoes back the string parameter passed in.
 */
@ExtensionNaming(namespace = SparqlExtension.EXTENSION_NAMESPACE, name = SparqlExtension.EXTENSION_NAME)
public class SparqlExtension extends AbstractRexsterExtension {

    public static final String EXTENSION_NAMESPACE = "tp";
    public static final String EXTENSION_NAME = "sparql";
    private static final String WILDCARD = "*";

    private static final String API_SHOW_TYPES = "displays the properties of the elements with their native data type (default is false)";
    private static final String API_QUERY = "the SPARQL query to be evaluated";
    private static final String API_RETURN_KEYS = "an array of element property keys to return (default is to return all element properties)";

    @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(description = "execute SPARQL queries against a SAIL graph.",
                         api = {
                             @ExtensionApi(parameterName = Tokens.REXSTER + "." + Tokens.SHOW_TYPES, description = API_SHOW_TYPES),
                             @ExtensionApi(parameterName = Tokens.REXSTER + "." + Tokens.RETURN_KEYS, description = API_RETURN_KEYS)
                         })
    public ExtensionResponse evaluateSparql(@RexsterContext RexsterResourceContext context,
                                          @RexsterContext Graph graph,
                                          @ExtensionRequestParameter(name="query", description=API_QUERY) String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            ExtensionMethod extMethod = context.getExtensionMethod();
            return ExtensionResponse.error(
                    "the query parameter cannot be empty",
                    null,
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    null,
                    generateErrorJson(extMethod.getExtensionApiAsJson()));
        }

        JSONObject requestObject = context.getRequestObject();
        boolean showDataTypes = RequestObjectHelper.getShowTypes(requestObject);
        List<String> returnKeys = RequestObjectHelper.getReturnKeys(requestObject, WILDCARD);

        if (!(graph instanceof SailGraph)) {
            throw new WebApplicationException();
        }

        try {

            SailGraph sailGraph = (SailGraph) graph;
            List<Map<String, Vertex>> sparqlResults = sailGraph.executeSparql(queryString);

            JSONArray jsonArray = new JSONArray();

            for (Map<String, Vertex> map : sparqlResults) {
                Map<String, ElementJSONObject> mapOfJson = new HashMap<String, ElementJSONObject>();
                for (String key : map.keySet()) {
                    mapOfJson.put(key, new ElementJSONObject(map.get(key), returnKeys, showDataTypes));
                }

                jsonArray.put(new JSONObject(mapOfJson));
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
