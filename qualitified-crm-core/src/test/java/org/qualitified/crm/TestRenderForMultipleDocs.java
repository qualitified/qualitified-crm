package org.qualitified.crm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.api.adapters.TemplateSourceDocument;
import org.nuxeo.template.processors.xdocreport.ZipXmlHelper;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm", "org.nuxeo.template.manager.api", "org.nuxeo.template.manager", "org.nuxeo.template.manager.xdocreport"})
public class TestRenderForMultipleDocs {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    protected Blob getODTTemplateBlob() throws IOException {
        File file = FileUtils.getResourceFileFromContext("data/DocumentsAttributes.odt");
        Blob blob = Blobs.createBlob(file);
        blob.setFilename("DocumentsAttributes.odt");
        return blob;
    }

    @Test
    public void shouldGenerateMultipleDocs() throws IOException, OperationException {
        DocumentModel template = coreSession.createDocumentModel("/", "MultipleDocs", "TemplateSource");
        template.setPropertyValue("dc:title", "TemplateSource");
        template.setPropertyValue("file:content", (Serializable) getODTTemplateBlob());
        template = coreSession.createDocument(template);
        coreSession.save();
        Assert.assertNotNull(template.getId());

        DocumentModel doc1 = coreSession.createDocumentModel("/", "file1", "TemplateBasedFile");
        doc1.setPropertyValue("dc:title", "file 1");
        doc1 = coreSession.createDocument(doc1);
        coreSession.save();
        DocumentModel doc2 = coreSession.createDocumentModel("/", "file2", "TemplateBasedFile");
        doc2.setPropertyValue("dc:title", "file 2");
        doc2 = coreSession.createDocument(doc2);
        coreSession.save();
        DocumentModel doc3 = coreSession.createDocumentModel("/", "file3", "TemplateBasedFile");
        doc3.setPropertyValue("dc:title", "file 3");
        doc3 = coreSession.createDocument(doc3);
        coreSession.save();

        DocumentModelList docs = new DocumentModelListImpl();
        docs.add(doc1);
        docs.add(doc2);
        docs.add(doc3);

        TemplateBasedDocument adapter = doc1.getAdapter(TemplateBasedDocument.class);
        TemplateSourceDocument templateSource = template.getAdapter(TemplateSourceDocument.class);
        adapter.setTemplate(templateSource.getAdaptedDoc(), true);

        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(docs);
        Map<String, Object> params = new HashMap();
        params.put("templateName", "TemplateSource");

        Blob blob = (Blob) automationService.run(ctx, "Qualitified.RenderForMultipleDocs", params);

        Assert.assertNotNull(blob);

        String xmlContent = ZipXmlHelper.readXMLContent(blob,
                ZipXmlHelper.OOO_MAIN_FILE);

        Assert.assertTrue(xmlContent.contains("file 1"));
        Assert.assertTrue(xmlContent.contains("file 2"));
        Assert.assertTrue(xmlContent.contains("file 3"));

    }
}
