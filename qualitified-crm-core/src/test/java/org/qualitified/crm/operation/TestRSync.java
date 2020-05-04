package org.qualitified.crm.operation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, RuntimeFeature.class, AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestRSync {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;


    @Test
    public void testRSync() throws IOException, OperationException {

        /*File file = FileUtils.getResourceFileFromContext("data/qualitified-crm-package-4.1-SNAPSHOT.zip");
        Blob blob = Blobs.createBlob(file);
        blob.setFilename("qualitified-crm-package-4.1-SNAPSHOT.zip");

        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(blob);

        Map<String, Object> params = new HashMap();
        params.put("privateKey","~/.ssh/aws-dev.pem");
        params.put("host","ec2-107-23-199-212.compute-1.amazonaws.com");
        params.put("login","ubuntu");
        params.put("destination","/opt/import");

        StringBlob result = (StringBlob)automationService.run(ctx, "Qualitified.RSync", params);
        Assert.assertNotNull(result);*/

    }

}
