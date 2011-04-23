package com.tinkerpop.rexster.kibbles.frames;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.rexster.RexsterApplicationGraph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionMethod;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import com.tinkerpop.rexster.extension.ExtensionConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.codehaus.jettison.json.JSONObject;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class FramesExtensionTest {

    private Graph graph;
    private Mockery mockery = new JUnit4Mockery();
    private RexsterResourceContext ctx;
    private FramesExtension framesExtension = new FramesExtension();
    private States test = mockery.states("test");
    private RexsterApplicationGraph rag;

    @Before
    public void beforeTest() {
        this.graph = TinkerGraphFactory.createTinkerGraph();

        // full configuration against test domain olus a dummy
        StringBuffer sb = new StringBuffer();
        sb.append("<extension><namespace>");
        sb.append(FramesExtension.EXTENSION_NAMESPACE);
        sb.append("</namespace><name>");
        sb.append(FramesExtension.EXTENSION_NAME);
        sb.append("</name>");
        sb.append("<configuration>");
        sb.append("<person>com.tinkerpop.frames.domain.classes.Person</person>");
        sb.append("<project>com.tinkerpop.frames.domain.classes.Project</project>");
        sb.append("<created>com.tinkerpop.frames.domain.relations.Created</created>");
        sb.append("<createdby>com.tinkerpop.frames.domain.relations.CreatedBy</createdby>");
        sb.append("<knows>com.tinkerpop.frames.domain.relations.Knows</knows>");
        sb.append("<notreal>com.tinkerpop.frames.domain.relations.ThisFrameIsNotReal</notreal>");
        sb.append("</configuration></extension>");

        XMLConfiguration xmlConfig = new XMLConfiguration();

        try {
            xmlConfig.load(new StringReader(sb.toString()));
        } catch (ConfigurationException ex) {
        }

        this.rag = new RexsterApplicationGraph("tinkergraph", this.graph);

        // allow all namespaces for purpose of testing
        List<String> allowedNamespaces = new ArrayList<String>();
        allowedNamespaces.add("*:*");
        this.rag.loadAllowableExtensions(allowedNamespaces);

        // configure the frames extension
        List<HierarchicalConfiguration> configs = new ArrayList<HierarchicalConfiguration>();
        configs.add(xmlConfig);
        this.rag.loadExtensionsConfigurations(configs);
    }

    @Test
    public void isConfigurationValidNullConfiguration() {
        Assert.assertFalse(this.framesExtension.isConfigurationValid(null));
    }

    @Test
    public void isConfigurationValidEmptyConfiguration() {
        HierarchicalConfiguration xmlConfig = new HierarchicalConfiguration();
        ExtensionConfiguration configuration = new ExtensionConfiguration("ns", "name", xmlConfig);
        Assert.assertFalse(this.framesExtension.isConfigurationValid(configuration));
    }

    @Test
    public void isConfigurationValidNiceConfiguration() {
        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        hc.addProperty("key1", "value1");
        hc.addProperty("key2", "value2");
        ExtensionConfiguration configuration = new ExtensionConfiguration("ns", "name", hc);
        Assert.assertTrue(this.framesExtension.isConfigurationValid(configuration));
    }

    @Test
    public void doFramesWorkOnVertexShortUrl() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment vertexPathSegment = this.mockery.mock(PathSegment.class, "vertexPathSegment");
        final PathSegment vertexIdPathSegment = this.mockery.mock(PathSegment.class, "vertexIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(vertexPathSegment);
        pathSegments.add(vertexIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnVertex(this.ctx, this.graph, this.graph.getVertex(1));

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));

    }

    @Test
    public void doFramesWorkOnVertexBadMapping() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment vertexPathSegment = this.mockery.mock(PathSegment.class, "vertexPathSegment");
        final PathSegment vertexIdPathSegment = this.mockery.mock(PathSegment.class, "vertexIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(vertexPathSegment);
        pathSegments.add(vertexIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("not-a-frame-in-config"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnVertex(this.ctx, this.graph, this.graph.getVertex(1));

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));
    }

    @Test
    public void doFramesWorkOnVertexInvalidFrameRequested() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment vertexPathSegment = this.mockery.mock(PathSegment.class, "vertexPathSegment");
        final PathSegment vertexIdPathSegment = this.mockery.mock(PathSegment.class, "vertexIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(vertexPathSegment);
        pathSegments.add(vertexIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("notreal"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnVertex(this.ctx, this.graph, this.graph.getVertex(1));

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));
    }

    @Test
    public void doFramesWorkOnVertexWrapWithPersonFrame() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment vertexPathSegment = this.mockery.mock(PathSegment.class, "vertexPathSegment");
        final PathSegment vertexIdPathSegment = this.mockery.mock(PathSegment.class, "vertexIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(vertexPathSegment);
        pathSegments.add(vertexIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("person"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnVertex(this.ctx, this.graph, this.graph.getVertex(1));

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("name"));
        Assert.assertEquals("marko", jsonObject.optString("name"));
        Assert.assertTrue(jsonObject.has("age"));
        Assert.assertEquals(29, jsonObject.optInt("age"));
    }

    @Test
    public void doFramesWorkOnEdgeShortUrl() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment edgePathSegment = this.mockery.mock(PathSegment.class, "edgePathSegment");
        final PathSegment edgeIdPathSegment = this.mockery.mock(PathSegment.class, "edgeIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(edgePathSegment);
        pathSegments.add(edgeIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnEdge(this.ctx, this.graph, this.graph.getEdge(11), "standard");

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));

    }

    @Test
    public void doFramesWorkOnEdgeBadMapping() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment edgePathSegment = this.mockery.mock(PathSegment.class, "edgePathSegment");
        final PathSegment edgeIdPathSegment = this.mockery.mock(PathSegment.class, "edgeIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(edgePathSegment);
        pathSegments.add(edgeIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("not-a-frame-in-config"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnEdge(this.ctx, this.graph, this.graph.getEdge(11), "standard");

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));
    }

    @Test
    public void doFramesWorkOnEdgeInvalidFrameRequested() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment edgePathSegment = this.mockery.mock(PathSegment.class, "edgePathSegment");
        final PathSegment edgeIdPathSegment = this.mockery.mock(PathSegment.class, "edgeIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(edgePathSegment);
        pathSegments.add(edgeIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("notreal"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnEdge(this.ctx, this.graph, this.graph.getEdge(11), "standard");

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));
    }

    @Test
    public void doFramesWorkOnEdgeWrapWithCreatedFrameStandardDirection() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment edgePathSegment = this.mockery.mock(PathSegment.class, "edgePathSegment");
        final PathSegment edgeIdPathSegment = this.mockery.mock(PathSegment.class, "edgeIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(edgePathSegment);
        pathSegments.add(edgeIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("created"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnEdge(this.ctx, this.graph, this.graph.getEdge(11), "standard");

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("weight"));
        Assert.assertEquals("0.4", jsonObject.optString("weight"));
    }

    @Test
    public void doFramesWorkOnEdgeNoDirection() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment edgePathSegment = this.mockery.mock(PathSegment.class, "edgePathSegment");
        final PathSegment edgeIdPathSegment = this.mockery.mock(PathSegment.class, "edgeIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(edgePathSegment);
        pathSegments.add(edgeIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("created"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, null);

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnEdge(this.ctx, this.graph, this.graph.getEdge(11), null);

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("weight"));
        Assert.assertEquals("0.4", jsonObject.optString("weight"));
    }

    @Test
    public void doFramesWorkOnEdgeBadDirection() {
        final UriInfo uri = this.mockery.mock(UriInfo.class);
        final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
        final PathSegment graphPathSegment = this.mockery.mock(PathSegment.class, "graphPathSegment");
        final PathSegment edgePathSegment = this.mockery.mock(PathSegment.class, "edgePathSegment");
        final PathSegment edgeIdPathSegment = this.mockery.mock(PathSegment.class, "edgeIdPathSegment");
        final PathSegment namespacePathSegment = this.mockery.mock(PathSegment.class, "namespacePathSegment");
        final PathSegment extensionPathSegment = this.mockery.mock(PathSegment.class, "extensionPathSegment");
        final PathSegment frameNamePathSegment = this.mockery.mock(PathSegment.class, "frameNamePathSegment");

        pathSegments.add(graphPathSegment);
        pathSegments.add(edgePathSegment);
        pathSegments.add(edgeIdPathSegment);
        pathSegments.add(namespacePathSegment);
        pathSegments.add(extensionPathSegment);
        pathSegments.add(frameNamePathSegment);

        this.mockery.checking(new Expectations() {{
            allowing(namespacePathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAMESPACE));
            allowing(extensionPathSegment).getPath();
            will(returnValue(FramesExtension.EXTENSION_NAME));
            allowing(frameNamePathSegment).getPath();
            will(returnValue("notreal"));
            allowing(uri).getPathSegments();
            will(returnValue(pathSegments));
        }});

        // can do a slimmed down RexsterResourceContext
        this.ctx = new RexsterResourceContext(this.rag, uri, null, null, new ExtensionMethod(null, null, null));

        ExtensionResponse extResp = this.framesExtension.doFramesWorkOnEdge(this.ctx, this.graph, this.graph.getEdge(11), "bad-direction");

        Assert.assertNotNull(extResp);
        Assert.assertNotNull(extResp.getJerseyResponse());

        Response response = extResp.getJerseyResponse();
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        JSONObject jsonObject = (JSONObject) response.getEntity();
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.has("success"));
        Assert.assertFalse(jsonObject.optBoolean("success"));
    }
}
