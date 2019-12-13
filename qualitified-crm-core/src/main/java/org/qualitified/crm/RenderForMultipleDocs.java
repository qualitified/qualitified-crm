package org.qualitified.crm;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.template.adapters.doc.TemplateBasedDocumentAdapterImpl;
import org.nuxeo.template.api.InputType;
import org.nuxeo.template.api.TemplateInput;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.fm.FMContextBuilder;
import org.nuxeo.template.processors.xdocreport.XDocReportBindingResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by mgena on 11/11/2017.
 */
@Operation(id= RenderForMultipleDocs.ID, category= Constants.CAT_EXECUTION, label="RenderForMultipleDocs", description="Renders a word document for multiple nuxeo documents.")
public class RenderForMultipleDocs {
    public static final String ID = "Qualitified.RenderForMultipleDocs";
    private Log logger = LogFactory.getLog(RenderForMultipleDocs.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "templateName")
    protected String templateName;

    @OperationMethod
    public Blob run(DocumentModelList docs) throws Exception {

        DocumentModelList template = session.query("SELECT * FROM TemplateSource WHERE dc:title = '"+templateName+"'");
        if(template.size() == 0){
            throw new NuxeoException("No template with the name "+ templateName);
        }
        TemplateBasedDocument templateBasedDocument = new TemplateBasedDocumentAdapterImpl(docs.get(0));
        templateBasedDocument.setTemplate(template.get(0), false);

       // Blob sourceTemplateBlob = getSourceTemplateBlob(templateBasedDocument,
         //       templateName);
        Blob sourceTemplateBlob = (Blob) template.get(0).getPropertyValue("file:content");
        // load the template
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
                sourceTemplateBlob.getStream(), TemplateEngineKind.Freemarker,
                false);

        // manage parameters
        List<TemplateInput> params = templateBasedDocument.getParams(templateName);
        FieldsMetadata metadata = new FieldsMetadata();
        for (TemplateInput param : params) {
            if (param.getType() == InputType.PictureProperty) {
                metadata.addFieldAsImage(param.getName());
            }
        }
        report.setFieldsMetadata(metadata);

        // fill Freemarker context
        FMContextBuilder fmContextBuilder = new FMContextBuilder();
        List<Map<String, Object>> docsArray = new ArrayList<Map<String, Object>>();
        for(DocumentModel doc : docs){
            Map<String, Object> docCtx = fmContextBuilder.build(doc,templateName);
            docsArray.add(docCtx);
        }

        Map<String, Object> ctx = fmContextBuilder.build(docs.get(0),templateName);
        ctx.put("docs", docsArray);

        XDocReportBindingResolver resolver = new XDocReportBindingResolver(
                metadata);
        resolver.resolve(params, ctx, templateBasedDocument);

        // add default context vars
        IContext context = report.createContext();
        for (String key : ctx.keySet()) {
            context.put(key, ctx.get(key));
        }

        context.put("docs", docsArray);
        // add automatic loop on audit entries
        /*metadata.addFieldAsList("auditEntries.principalName");
        metadata.addFieldAsList("auditEntries.eventId");
        metadata.addFieldAsList("auditEntries.eventDate");
        metadata.addFieldAsList("auditEntries.docUUID");
        metadata.addFieldAsList("auditEntries.docPath");
        metadata.addFieldAsList("auditEntries.docType");
        metadata.addFieldAsList("auditEntries.category");
        metadata.addFieldAsList("auditEntries.comment");
        metadata.addFieldAsList("auditEntries.docLifeCycle");
        metadata.addFieldAsList("auditEntries.repositoryId");*/

        File workingDir = getWorkingDir();
        File generated = new File(workingDir, "XDOCReportresult-"
                + System.currentTimeMillis());
        generated.createNewFile();

        OutputStream out = new FileOutputStream(generated);

        report.process(context, out);

        Blob newBlob = new FileBlob(generated);

        String templateFileName = sourceTemplateBlob.getFilename();

        // set the output file name
        String targetFileExt = FileUtils.getFileExtension(templateFileName);
        String targetFileName = FileUtils.getFileNameNoExt(templateBasedDocument.getAdaptedDoc().getTitle());
        targetFileName = targetFileName + "." + targetFileExt;
        newBlob.setFilename(targetFileName);

        // mark the file for automatic deletion on GC
        Framework.trackFile(generated, newBlob);
        return newBlob;
    }


    protected File getWorkingDir() {
        String dirPath = System.getProperty("java.io.tmpdir")
                + "/NXTemplateProcessor" + System.currentTimeMillis();
        File workingDir = new File(dirPath);
        if (workingDir.exists()) {
            FileUtils.deleteTree(workingDir);
        }
        workingDir.mkdir();
        return workingDir;
    }

    protected Blob getSourceTemplateBlob(
            TemplateBasedDocument templateBasedDocument, String templateName)
            throws Exception {
        Blob sourceTemplateBlob = templateBasedDocument.getTemplateBlob(templateName);

        return sourceTemplateBlob;
    }



}