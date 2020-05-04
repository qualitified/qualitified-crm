package org.qualitified.crm.operation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
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
import java.util.HashMap;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, RuntimeFeature.class, AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestInstall {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;


    @Test
    public void testInstall() throws IOException, OperationException {

        /*OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);

        Map<String, Object> params = new HashMap();
        params.put("privateKey","~/.ssh/aws-dev.pem");
        params.put("host","ec2-107-23-199-212.compute-1.amazonaws.com");
        params.put("login","ubuntu");
        params.put("packageZipName","qualitified-studioviz-package-1.1.zip");

        StringBlob result = (StringBlob)automationService.run(ctx, "Qualitified.Install", params);
        Assert.assertNotNull(result);*/

    }

}
