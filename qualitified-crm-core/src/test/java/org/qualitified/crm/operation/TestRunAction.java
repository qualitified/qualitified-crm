package org.qualitified.crm.operation;

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
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, RuntimeFeature.class, AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestRunAction {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    @Before
    public void createActionGroup(){
        DocumentModel actionGroup = coreSession.createDocumentModel("/Admin/Scripts", "ActionGroup1","Scripts");
        actionGroup.setPropertyValue("dc:title", "Action Group 1");
        coreSession.createDocument(actionGroup);
        coreSession.save();
    }
    @Test
    public void shouldRunCorrectly() throws IOException, OperationException {

        DocumentModel subAction1 = coreSession.createDocumentModel("/Admin/Scripts/ActionGroup1", "SubAction1","ScriptNote");
        subAction1.setPropertyValue("dc:title", "SubAction1 - Change Title");
        subAction1.setPropertyValue("note:mime_type", "text/plain");
        subAction1.setPropertyValue("note:note", "input['dc:title']='title set via action';");
        coreSession.createDocument(subAction1);

        DocumentModel subAction2 = coreSession.createDocumentModel("/Admin/Scripts/ActionGroup1", "SubAction2","ScriptNote");
        subAction2.setPropertyValue("dc:title", "SubAction2 - Change Description");
        subAction2.setPropertyValue("note:mime_type", "text/plain");
        subAction2.setPropertyValue("note:note", "input['dc:description']='description set via action';");
        coreSession.createDocument(subAction2);

        DocumentModel subAction3 = coreSession.createDocumentModel("/Admin/Scripts/ActionGroup1", "SubAction2","ScriptNote");
        subAction3.setPropertyValue("dc:title", "SubAction2 - Change Description");
        subAction3.setPropertyValue("note:mime_type", "text/plain");
        subAction3.setPropertyValue("note:note", "Document.Save(input,{});");
        coreSession.createDocument(subAction3);

        DocumentModel file = coreSession.createDocumentModel("/", "My File", "File");
        file = coreSession.createDocument(file);
        coreSession.save();

        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(file);

        Map<String, Object> params = new HashMap();
        params.put("actionId",coreSession.getDocument(new PathRef("/Admin/Scripts/ActionGroup1")).getId());
        params.put("actionName",coreSession.getDocument(new PathRef("/Admin/Scripts/ActionGroup1")).getTitle());

        DocumentModel doc = (DocumentModel)automationService.run(ctx, "Qualitified.RunAction", params);

        Assert.assertEquals("title set via action", doc.getTitle());
        Assert.assertEquals("description set via action", (String)doc.getPropertyValue("dc:description"));
    }

}
