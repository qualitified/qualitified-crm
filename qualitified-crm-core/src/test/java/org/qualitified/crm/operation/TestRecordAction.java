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
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm", "org.nuxeo.template.manager.api", "org.nuxeo.template.manager", "org.nuxeo.template.manager.xdocreport"})
public class TestRecordAction {

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
    public void shouldRecordAction() throws OperationException {
        DocumentModel actionGroup = coreSession.getDocument(new PathRef("/Admin/Scripts/ActionGroup1"));
        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(actionGroup);

        Map<String, Object> params = new HashMap();
        params.put("actionName", "Add To VIP Collection");
        params.put("actionContent", "Document.AddToCollection(input,{\"collection\":\"987-654-321\"};");
        DocumentModel action = (DocumentModel)automationService.run(ctx, RecordAction.ID, params);

        Assert.assertEquals("Add To VIP Collection", action.getTitle());
        Assert.assertEquals("Document.AddToCollection(input,{\"collection\":\"987-654-321\"};", (String)action.getPropertyValue("note:note"));
    }

}
