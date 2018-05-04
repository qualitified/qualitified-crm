package org.qualitified.crm.listener;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({"studio.extensions.crm"})
public class TestPreSave {

    protected final List<String> events = Arrays.asList("aboutToCreate", "beforeDocumentModification");

    @Inject
    protected EventService s;

    @Test
    public void listenerRegistration() {
        EventListenerDescriptor listener = s.getEventListener("presave");
        assertNotNull(listener);
        assertTrue(events.stream().allMatch(listener::acceptEvent));
    }
}
