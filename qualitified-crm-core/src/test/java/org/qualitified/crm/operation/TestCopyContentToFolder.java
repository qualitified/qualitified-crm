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
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestCopyContentToFolder {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    DocumentModel htmlDocument;

    @Before
    public void createContent(){
        htmlDocument = coreSession.createDocumentModel("/Admin", "test.html","ScriptNote");
        htmlDocument.setPropertyValue("dc:title", "test.html");
        htmlDocument.setPropertyValue("note:note", "<h1>Hi!</h1>");
        htmlDocument = coreSession.createDocument(htmlDocument);
        coreSession.save();
    }

    @Test
    public void shouldGetContent() throws OperationException, IOException {
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(htmlDocument);
        Map<String, Object> params = new HashMap();
        String tmpdir = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
        params.put("path", tmpdir);
        automationService.run(ctx,  CopyContentToFolder.ID, params);
        File html = new File(tmpdir+"/test.html");
        String content = new String ( Files.readAllBytes( Paths.get(html.getAbsolutePath()) ) );
        Assert.assertEquals("test.html", html.getName());
        Assert.assertEquals("<h1>Hi!</h1>", content);

    }

}
