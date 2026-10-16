package org.nuxeo.ecm.restapi.server.jaxrs.element;

import jakarta.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.http.test.HttpClientTestRule;
import org.nuxeo.http.test.handler.VoidHandler;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import test.CCMFeature;

@RunWith(FeaturesRunner.class)
@Features({ CCMFeature.class })
public class ElementResourceTest {

    @Inject
    protected CoreSession session;

    @Inject
    protected RestServerFeature restServerFeature;

    @Rule
    public final HttpClientTestRule httpClient = HttpClientTestRule.defaultClient(
            () -> restServerFeature.getRestApiUrl());

    @Test
    public void getElementByName() {
        httpClient.buildGetRequest("/element/nuxeo-account-edit-layout").execute(new VoidHandler());
    }

    @Test
    public void getElementByNameNotFound() {
        httpClient.buildGetRequest("/element/my-unexisting-element").execute(new VoidHandler());
    }
}
