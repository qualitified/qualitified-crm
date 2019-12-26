package org.qualitified.crm.listener;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, AutomationFeature.class})
@Deploy({"studio.extensions.crm"})
public class TestPostSave {

    protected final List<String> events = Arrays.asList("documentCreated", "documentModified");

    @Inject
    protected EventService s;

    @javax.inject.Inject
    CoreSession coreSession;

    //@Test
    public void listenerRegistration() {
        EventListenerDescriptor listener = s.getEventListener("postsave");
        assertNotNull(listener);
        assertTrue(events.stream().allMatch(listener::acceptEvent));
    }

    @Test
    public void runPostSave() throws OperationException{

        DocumentModel folder = coreSession.createDocumentModel("/", "folder1", "Folder");
        folder.setPropertyValue("dc:title", "My Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModel file = coreSession.createDocumentModel("/folder1", "file1", "File");
        file = coreSession.createDocument(file);
        coreSession.save();

        DocumentModel script = coreSession.createDocumentModel("/Admin/Scripts", "FolderPostSave","ScriptNote");
        script.setPropertyValue("dc:title", "FolderPostSave");
        script.setPropertyValue("note:mime_type", "text/plain");
        //script.setPropertyValue("note:note","");
        script.setPropertyValue("note:note", "var file = Document.Fetch(null, {'value':'/folder1/file1'});\nif(file != null){file['dc:description']=input.title;\nfile['custom:booleanField1']=params.isCreation;\nDocument.Save(file,{});}\n");
        coreSession.createDocument(script);
        coreSession.save();

        folder.setPropertyValue("dc:title", "My New Folder Title");
        coreSession.saveDocument(folder);

        assertNotNull(file.getId());
        assertEquals(folder.getTitle(), file.getPropertyValue("dc:description"));
        assertEquals(false, file.getPropertyValue("custom:booleanField1"));
    }

    @Test
    public void templateStructureWorks(){
        DocumentModel templateFolder = coreSession.createDocumentModel("/Admin/Templates", "Folder", "Folder");
        coreSession.createDocument(templateFolder);
        coreSession.save();

        DocumentModel subFolder = coreSession.createDocumentModel("/Admin/Templates/Folder", "subfolder1", "Folder");
        coreSession.createDocument(subFolder);
        coreSession.save();

        DocumentModel subFolder2 = coreSession.createDocumentModel("/Admin/Templates/Folder", "subfolder2", "Folder");
        coreSession.createDocument(subFolder2);
        coreSession.save();

        DocumentModel folder = coreSession.createDocumentModel("/", "folder", "Folder");
        folder = coreSession.createDocument(folder);
        coreSession.save();

        DocumentModelList list = coreSession.getChildren(new IdRef(folder.getId()));

        assertEquals(2,list.totalSize());
    }
}
