package org.qualitified.crm.operation;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
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
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestNextBestAction {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    @Before
    public void createOpportunitiesAndInteractions(){
        DocumentModel opportunity1 = coreSession.createDocumentModel("/", "Opportunity1","Opportunity");
        opportunity1.setPropertyValue("dc:title", "Opportunity 1");
        opportunity1.setPropertyValue("opportunity:step", "Won");
        coreSession.createDocument(opportunity1);

        DocumentModel opportunity2 = coreSession.createDocumentModel("/", "Opportunity2","Opportunity");
        opportunity2.setPropertyValue("dc:title", "Opportunity 2");
        opportunity2.setPropertyValue("opportunity:step", "Won");
        coreSession.createDocument(opportunity2);

        DocumentModel opportunity3 = coreSession.createDocumentModel("/", "Opportunity3","Opportunity");
        opportunity3.setPropertyValue("dc:title", "Opportunity 3");
        opportunity3.setPropertyValue("opportunity:step", "Won");
        coreSession.createDocument(opportunity3);

        DocumentModel interaction11 = coreSession.createDocumentModel("/Opportunity1", "Interaction1","Interaction");
        interaction11.setPropertyValue("dc:title", "Interaction 1");
        interaction11.setPropertyValue("interaction:activity", "Call");
        interaction11.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction11);

        DocumentModel interaction12 = coreSession.createDocumentModel("/Opportunity1", "Interaction12","Interaction");
        interaction12.setPropertyValue("dc:title", "Interaction 1.2");
        interaction12.setPropertyValue("interaction:activity", "Email");
        interaction12.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction12);

        DocumentModel interaction13 = coreSession.createDocumentModel("/Opportunity1", "Interaction13","Interaction");
        interaction13.setPropertyValue("dc:title", "Interaction 1.3");
        interaction13.setPropertyValue("interaction:activity", "Meeting");
        interaction13.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction13);

        DocumentModel interaction21 = coreSession.createDocumentModel("/Opportunity2", "Interaction21","Interaction");
        interaction21.setPropertyValue("dc:title", "Interaction 2.1");
        interaction21.setPropertyValue("interaction:activity", "Meeting");
        interaction21.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction21);

        DocumentModel interaction22 = coreSession.createDocumentModel("/Opportunity2", "Interaction22","Interaction");
        interaction22.setPropertyValue("dc:title", "Interaction 2.2");
        interaction22.setPropertyValue("interaction:activity", "Email");
        interaction22.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction22);

        DocumentModel interaction23 = coreSession.createDocumentModel("/Opportunity2", "Interaction23","Interaction");
        interaction23.setPropertyValue("dc:title", "Interaction 2.3");
        interaction23.setPropertyValue("interaction:activity", "Call");
        interaction23.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction23);

        DocumentModel interaction31 = coreSession.createDocumentModel("/Opportunity3", "Interaction31","Interaction");
        interaction31.setPropertyValue("dc:title", "Interaction 3.1");
        interaction31.setPropertyValue("interaction:activity", "Call");
        interaction31.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction31);

        DocumentModel interaction32 = coreSession.createDocumentModel("/Opportunity3", "Interaction32","Interaction");
        interaction32.setPropertyValue("dc:title", "Interaction 3.2");
        interaction32.setPropertyValue("interaction:activity", "Email");
        interaction32.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction32);

        DocumentModel interaction33 = coreSession.createDocumentModel("/Opportunity3", "Interaction33","Interaction");
        interaction33.setPropertyValue("dc:title", "Interaction 3.3");
        interaction33.setPropertyValue("interaction:activity", "Call");
        interaction33.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction33);

        coreSession.save();
    }

    @Test
    public void shouldGetNextBestAction() throws OperationException, LoginException, IOException, JSONException {
        /*DocumentModel opportunity = coreSession.createDocumentModel("/", "Opportunity","Opportunity");
        opportunity.setPropertyValue("dc:title", "New Opportunity");
        opportunity.setPropertyValue("opportunity:step", "New");
        opportunity = coreSession.createDocument(opportunity);
        coreSession.save();

        DocumentModel interaction1 = coreSession.createDocumentModel("/Opportunity", "Interaction1","Interaction");
        interaction1.setPropertyValue("dc:title", "Interaction 1");
        interaction1.setPropertyValue("interaction:activity", "Call");
        interaction1.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction1);

        DocumentModel interaction2 = coreSession.createDocumentModel("/Opportunity", "Interaction2","Interaction");
        interaction2.setPropertyValue("dc:title", "Interaction 2");
        interaction2.setPropertyValue("interaction:activity", "Email");
        interaction2.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction2);

        DocumentModel interaction3 = coreSession.createDocumentModel("/Opportunity", "Interaction3","Interaction");
        interaction3.setPropertyValue("dc:title", "Interaction 3");
        interaction3.setPropertyValue("interaction:activity", "Meeting");
        interaction3.setPropertyValue("interaction:status", "DONE");
        coreSession.createDocument(interaction3);
        coreSession.save();
        // Operation execution

        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(opportunity);

        Map<String, Object> params = new HashMap();
        //Blob nextAction = (Blob)automationService.run(ctx, NextBestAction.ID, params);
        String nextAction = (String)automationService.run(ctx, NextBestAction.ID, params);
        //String jsonString =  IOUtils.toUTF8String(nextAction.getByteArray());
        JSONObject json = new JSONObject(nextAction);
        Assert.assertNotNull("", json.get("activityType"));*/

    }

}
