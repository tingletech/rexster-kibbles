package com.tinkerpop.rexster.kibbles.gremlin

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.rexster.extension.*;
import com.tinkerpop.rexster.RexsterResourceContext;
import org.codehaus.jettison.json.JSONObject;
import com.tinkerpop.gremlin.Gremlin;

/* from https://github.com/tinkerpop/rexster/wiki/Extensions-and-Gremlin */
@ExtensionNaming(namespace = "ex", name = "gremlinExample")
public class GremlinBasedExtension extends AbstractRexsterExtension {

    static {
        Gremlin.load();
    }

    @ExtensionDefinition(extensionPoint = ExtensionPoint.VERTEX)
    @ExtensionDescriptor(description = "Traversal with native Gremlin")
    public ExtensionResponse evaluate(@RexsterContext RexsterResourceContext context,
                                      @RexsterContext Vertex v) {

        JSONObject results = new JSONObject();
        results.put("foaf count", v.out('knows').out('knows').count())
        return ExtensionResponse.ok(results);
    }
}
