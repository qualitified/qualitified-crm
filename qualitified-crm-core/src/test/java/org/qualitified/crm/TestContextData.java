package org.qualitified.crm;

import jdk.nashorn.internal.parser.JSONParser;
import net.sf.json.JSONObject;
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
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.*;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import org.nuxeo.ecm.directory.sql.SQLDirectoryFeature;


@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestContextData {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;


    @Test
    public void shouldContainContextData()  throws OperationException {
        DocumentModel doc = coreSession.createDocumentModel("/", "my-file", "File");
        doc = coreSession.createDocument(doc);
        coreSession.save();

        // Operation execution
        AutomationService automationService = Framework.getLocalService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(doc);
        Map<String, Object> params = new HashMap();
        params.put("variable", "myVariable");
        params.put("value", "Hello World!");
        doc = (DocumentModel) automationService.run(ctx, "Qualitified.PutContextData", params);

        Assert.assertEquals((String)doc.getContextData("myVariable"), "Hello World!");

        Map<String, Object> getParams = new HashMap();
        getParams.put("variable", "myVariable");
        StringBlob result = (StringBlob) automationService.run(ctx, "Qualitified.GetContextData", getParams);

        Assert.assertNotNull(result);

        Assert.assertEquals(result.getString(), "Hello World!");

    }

}
