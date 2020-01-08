package org.qualitified.crm.operation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, RuntimeFeature.class, AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({"studio.extensions.crm"})
public class TestUnitTests {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    @Test
    public void shouldRunCorrectly() throws IOException, OperationException {

        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FilePreSave","ScriptNote");
        script.setPropertyValue("dc:title", "FilePreSave - Change Description");
        script.setPropertyValue("note:mime_type", "text/plain");
        script.setPropertyValue("note:note", "input['dc:description']='description set via script';\ninput['custom:booleanField1']=params.isCreation;");
        coreSession.createDocument(script);
        coreSession.save();

        DocumentModel folder = coreSession.createDocumentModel("/", "MyFolder", "Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModel testScript = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript.setPropertyValue("dc:title", "UnitTest - Check if descsription is correctly set");
        testScript.setPropertyValue("note:mime_type", "text/plain");
        testScript.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertEquals(null,{'expected': 'description set via script', 'result': file['dc:description'], 'message': 'Wrong description'});");
        testScript = coreSession.createDocument(testScript);
        coreSession.save();

        DocumentModel testScript2 = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript2.setPropertyValue("dc:title", "UnitTest - Check if custom:booleanField1 is correctly set");
        testScript2.setPropertyValue("note:mime_type", "text/plain");
        testScript2.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertTrue(null,{'result': file['custom:booleanField1']+'', 'message': 'Creation mode'});");
        testScript2 = coreSession.createDocument(testScript2);
        coreSession.save();

        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);


        Map<String, Object> params = new HashMap();

        automationService.run(ctx, "Qualitified.RunUnitTests", params);

        testScript = coreSession.getDocument(testScript.getRef());
        Assert.assertTrue((Boolean)testScript.getPropertyValue("scriptnote:isValid"));

        testScript2 = coreSession.getDocument(testScript2.getRef());
        Assert.assertTrue((Boolean)testScript2.getPropertyValue("scriptnote:isValid"));
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
        testScript.setPropertyValue("dc:title", "UnitTest - Check if description is set correctly");
        testScript.setPropertyValue("note:mime_type", "text/plain");
        testScript.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertEquals(null,{'expected': 'value set via script', 'result': file['dc:description'], 'message': 'Wrong description'});");
        testScript = coreSession.createDocument(testScript);
        coreSession.save();

        DocumentModel testScript2 = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript2.setPropertyValue("dc:title", "UnitTest - Check if custom:booleanField1 is correctly set");
        testScript2.setPropertyValue("note:mime_type", "text/plain");
        testScript2.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertFalse(null,{'result': file['custom:booleanField1']+'', 'message': 'Creation mode'});");
        testScript2 = coreSession.createDocument(testScript2);
        coreSession.save();


        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);


        Map<String, Object> params = new HashMap();

        automationService.run(ctx, "Qualitified.RunUnitTests", params);

        testScript = coreSession.getDocument(testScript.getRef());
        Assert.assertTrue((Boolean)testScript.getPropertyValue("scriptnote:isValid"));

        testScript2 = coreSession.getDocument(testScript2.getRef());
        Assert.assertFalse((Boolean)testScript2.getPropertyValue("scriptnote:isValid"));
        Assert.assertNotNull(testScript2.getPropertyValue("dc:description"));
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
        testScript.setPropertyValue("dc:title", "UnitTest - Check if description is set correctly");
        testScript.setPropertyValue("note:mime_type", "text/plain");
        testScript.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertEquals(null,{'expected': 'value set via script', 'result': file['dc:description'], 'message': 'Wrong description'});");
        testScript = coreSession.createDocument(testScript);
        coreSession.save();

        DocumentModel testScript2 = coreSession.createDocumentModel("/Admin/Scripts", "UnitTest","ScriptNote");
        testScript2.setPropertyValue("dc:title", "UnitTest - Check if custom:booleanField1 is correctly set");
        testScript2.setPropertyValue("note:mime_type", "text/plain");
        testScript2.setPropertyValue("note:note", "var folder = Document.Fetch(null,{'value': '"+folder.getId()+"'});\nvar file = Document.Create(folder, {'type':'File', 'name': 'My File'});\nQualitified.AssertTrue(null,{'result': file['custom:booleanField1']+'', 'message': 'Creation mode'});");
        testScript2 = coreSession.createDocument(testScript2);
        coreSession.save();


        // Operation execution
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(coreSession);


        Map<String, Object> params = new HashMap();

        automationService.run(ctx, "Qualitified.RunUnitTests", params);

        testScript = coreSession.getDocument(testScript.getRef());
        Assert.assertFalse((Boolean)testScript.getPropertyValue("scriptnote:isValid"));
        Assert.assertNotNull(testScript.getPropertyValue("dc:description"));

        testScript2 = coreSession.getDocument(testScript2.getRef());
        Assert.assertTrue((Boolean)testScript2.getPropertyValue("scriptnote:isValid"));
    }
}
