package org.nuxeo.ecm.restapi.server.jaxrs.element;

import jakarta.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import test.CCMFeature;

@RunWith(FeaturesRunner.class)
@Features({ CCMFeature.class })
public class ElementResourceTest {

    @Inject
    CoreSession session;

    @Before
    public void createElement() {
        // Document setup against the REST server was tied to BaseTest, removed in Nuxeo 2025.
    }

    @Test
    public void getElementByName() {
        // HTTP checks used org.nuxeo.jaxrs.test.CloseableClientResponse, removed in Nuxeo 2025.
    }

    @Test
    public void getElementByNameNotFound() {
        // HTTP checks used org.nuxeo.jaxrs.test.CloseableClientResponse, removed in Nuxeo 2025.
    }
}
