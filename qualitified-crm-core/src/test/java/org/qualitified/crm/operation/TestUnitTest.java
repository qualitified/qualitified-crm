package org.qualitified.crm.operation;

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
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.api.adapters.TemplateSourceDocument;
import org.nuxeo.template.processors.xdocreport.ZipXmlHelper;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, RuntimeFeature.class, AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestUnitTest {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldRunCorrectly() throws IOException, OperationException {

        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreSave","ScriptNote");
        script.setPropertyValue("dc:title", "FilePreSave");
        script.setPropertyValue("note:mime_type", "text/plain");
        script.setPropertyValue("note:note", "input['dc:description']='value set via script';\ninput['custom:booleanField1']=params.isCreation;");
        coreSession.createDocument(script);
        coreSession.save();

        DocumentModel folder = coreSession.createDocumentModel("/", "MyFolder", "Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModel testScript = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript.setPropertyValue("dc:title", "UnitTest");
        testScript.setPropertyValue("note:mime_type", "text/plain");
        testScript.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertEquals(null,{'expected': 'value set via script', 'result': file['dc:description'], 'message': 'Wrong description'});");
        testScript = coreSession.createDocument(testScript);
        coreSession.save();

        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(testScript);

        Map<String, Object> params = new HashMap();

        automationService.run(ctx, "Qualitified.RunUnitTest", params);

        testScript = coreSession.getDocument(testScript.getRef());
        Assert.assertTrue((Boolean)testScript.getPropertyValue("scriptnote:isValid"));
    }

    @Test
    public void shouldFailCorrectly() throws IOException, OperationException {

        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreSave","ScriptNote");
        script.setPropertyValue("dc:title", "FilePreSave");
        script.setPropertyValue("note:mime_type", "text/plain");
        script.setPropertyValue("note:note", "input['dc:description']='value set via script';\ninput['custom:booleanField1']=params.isCreation;");
        coreSession.createDocument(script);
        coreSession.save();

        DocumentModel folder = coreSession.createDocumentModel("/", "MyFolder", "Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModel testScript = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript.setPropertyValue("dc:title", "UnitTest");
        testScript.setPropertyValue("note:mime_type", "text/plain");
        testScript.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertEquals(null,{'expected': 'random value', 'result': file['dc:description'], 'message': 'Wrong description'});");
        testScript = coreSession.createDocument(testScript);
        coreSession.save();

        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(testScript);

        Map<String, Object> params = new HashMap();

        automationService.run(ctx, "Qualitified.RunUnitTest", params);

        testScript = coreSession.getDocument(testScript.getRef());
        Assert.assertFalse((Boolean)testScript.getPropertyValue("scriptnote:isValid"));
        Assert.assertNotNull(testScript.getPropertyValue("dc:description"));
    }

    @Test
    public void shouldFailCorrectlyBecauseOfScriptError() throws IOException, OperationException {

        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreSave","ScriptNote");
        script.setPropertyValue("dc:title", "FilePreSave");
        script.setPropertyValue("note:mime_type", "text/plain");
        script.setPropertyValue("note:note", "input['dc:desciption']='value set via script';\ninput['custom:booleanField1']=params.isCreation;");
        coreSession.createDocument(script);
        coreSession.save();

        DocumentModel folder = coreSession.createDocumentModel("/", "MyFolder", "Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModel testScript = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript.setPropertyValue("dc:title", "UnitTest");
        testScript.setPropertyValue("note:mime_type", "text/plain");
        testScript.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertEquals(null,{'expected': 'random value', 'result': file['dc:description'], 'message': 'Wrong description'});");
        testScript = coreSession.createDocument(testScript);
        coreSession.save();

        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);
        ctx.setInput(testScript);

        Map<String, Object> params = new HashMap();

        automationService.run(ctx, "Qualitified.RunUnitTest", params);

        testScript = coreSession.getDocument(testScript.getRef());
        Assert.assertFalse((Boolean)testScript.getPropertyValue("scriptnote:isValid"));
        Assert.assertNotNull(testScript.getPropertyValue("dc:description"));
    }
}
