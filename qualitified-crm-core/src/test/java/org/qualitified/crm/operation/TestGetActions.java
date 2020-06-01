package org.qualitified.crm.operation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestGetActions {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    @Before
    public void createAction(){
        DocumentModel action = coreSession.createDocumentModel("/Admin/Scripts", "Action","Scripts");
        action.setPropertyValue("dc:title", "My custom Action");
        action.setPropertyValue("scripts:documentTypes", "Document1,Document2");
        action.setPropertyValue("scripts:expression", "val=test");
        action.setPropertyValue("scripts:path", "/path/1//2/3");
        action.setPropertyValue("scripts:permission", "Read,Write");
        action.setPropertyValue("scripts:state", "project");
        action.setPropertyValue("scripts:facet", "MyFacet");
        action.setPropertyValue("scripts:icon", "qualitified:icon");
        action.setPropertyValue("scripts:groups", "administrators");
        action.setPropertyValue("scripts:scriptType", "action");
        coreSession.createDocument(action);
        coreSession.save();
    }

    @Test
    public void shouldGetAction() throws OperationException, JSONException {
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        StringBlob actions = (StringBlob)automationService.run(ctx, GetActions.ID);
        JSONArray jsonArray = new JSONObject((String)new JSONObject(actions).get("string")).getJSONArray("actions");
        Assert.assertEquals("My custom Action", ((JSONObject)jsonArray.get(0)).get("title"));
        Assert.assertEquals("Document1,Document2", ((JSONObject)jsonArray.get(0)).get("scripts:documentTypes"));
        Assert.assertEquals("administrators", ((JSONObject)jsonArray.get(0)).get("scripts:groups"));
        Assert.assertEquals("val=test", ((JSONObject)jsonArray.get(0)).get("scripts:expression"));
        Assert.assertEquals("/path/1//2/3", ((JSONObject)jsonArray.get(0)).get("scripts:path"));
        Assert.assertEquals("Read,Write", ((JSONObject)jsonArray.get(0)).get("scripts:permission"));
        Assert.assertEquals("project", ((JSONObject)jsonArray.get(0)).get("scripts:state"));
        Assert.assertEquals("MyFacet", ((JSONObject)jsonArray.get(0)).get("scripts:facet"));
        Assert.assertEquals("qualitified:icon", ((JSONObject)jsonArray.get(0)).get("scripts:icon"));
    }

}
