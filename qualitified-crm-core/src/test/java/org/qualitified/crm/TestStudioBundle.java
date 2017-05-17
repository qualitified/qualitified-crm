package org.qualitified.crm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.*;
import org.nuxeo.runtime.RuntimeService;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.directory.sql.SQLDirectoryFeature;
import org.nuxeo.ecm.core.api.DocumentModel;

@RunWith(FeaturesRunner.class)
@Features({SQLDirectoryFeature.class, CoreFeature.class, RuntimeFeature.class, AutomationFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@LocalDeploy("org.nuxeo.ecm.directory.sql.tests:test-sql-directories-bundle.xml")
@Deploy({"studio.extensions.cgisweden"})
public class TestStudioBundle {

    @Inject
    CoreSession coreSession;

    @Inject
    protected AutomationService automationService;


    @Test
    public void shouldDeployNuxeoRuntime() {
        RuntimeService runtime = Framework.getRuntime();
        assertNotNull(runtime);
    }

    @Test
    public void shouldHaveAPublicationDate() {
        DocumentModel doc = coreSession.createDocumentModel("/", "myDoc", "bp-participant");
        assertNotNull(doc);
        //assertNotNull(doc.getPropertyValue("book:publicationDate"));
    }
}
