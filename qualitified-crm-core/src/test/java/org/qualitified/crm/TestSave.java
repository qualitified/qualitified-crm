package org.qualitified.crm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.api.Framework;
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
public class TestSave {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;


    @Test
    public void shouldSave()  throws OperationException {
        DocumentModel doc = coreSession.createDocumentModel("/", "my-file", "File");
        doc = coreSession.createDocument(doc);
        coreSession.save();

        doc.setPropertyValue("dc:description","Hello World!");
        // Operation execution
        AutomationService automationService = Framework.getLocalService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(doc);
        Map<String, Object> params = new HashMap();
        params.put("login", "Administrator");

        DocumentModel savedDoc = (DocumentModel) automationService.run(ctx, "Qualitified.Save", params);

        Assert.assertNotNull(savedDoc);
        Assert.assertEquals((String)savedDoc.getPropertyValue("dc:description"), "Hello World!");

    }

}
