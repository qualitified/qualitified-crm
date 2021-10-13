package org.nuxeo.ecm.restapi.server.jaxrs.element;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.web.common.RequestCleanupHandler;
import org.nuxeo.ecm.platform.web.common.RequestContext;
import org.nuxeo.ecm.platform.web.common.RequestCleanupHandler;
import org.nuxeo.ecm.platform.web.common.RequestContext;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;

public abstract class UnrestrictedRunner {
    private static final Log log = LogFactory.getLog(UnrestrictedRunner.class);

    public Object getLinkDocument(String code) {
        final LoginContext lc;
        try {
            lc = Framework.login();
        } catch (LoginException ex) {
            log.error("Unable to render page", ex);
            return null;
        }
        CloseableCoreSession coreSession = null;
        try {
            coreSession = CoreInstance.openCoreSession(null);

            // Run unrestricted operation
            return getLinkDocument(coreSession, code);

        } finally {
            final CloseableCoreSession session2close = coreSession;
            RequestContext.getActiveContext().addRequestCleanupHandler(new RequestCleanupHandler() {

                @Override
                public void cleanup(HttpServletRequest req) {
                    try {
                        session2close.close();
                        lc.logout();
                    } catch (LoginException e) {
                        log.error("Error during request context cleanup", e);
                    }
                }
            });
        }
    }

    public Object getDocument(String docId, String code) {
        final LoginContext lc;
        try {
            lc = Framework.login();
        } catch (LoginException ex) {
            log.error("Unable to render page", ex);
            return null;
        }
        CloseableCoreSession coreSession = null;
        try {
            coreSession = CoreInstance.openCoreSession(null);

            // Run unrestricted operation
            return getDocument(coreSession, docId, code);

        } finally {
            final CloseableCoreSession session2close = coreSession;
            RequestContext.getActiveContext().addRequestCleanupHandler(new RequestCleanupHandler() {

                @Override
                public void cleanup(HttpServletRequest req) {
                    try {
                        session2close.close();
                        lc.logout();
                    } catch (LoginException e) {
                        log.error("Error during request context cleanup", e);
                    }
                }
            });

        }

    }

    public Object createOrUpdateDocument(String input, String type, String name, String docId, DocumentModel inputDoc) throws IOException {
        final LoginContext lc;
        try {
            lc = Framework.login();
        } catch (LoginException ex) {
            log.error("Unable to render page", ex);
            return null;
        }
        CloseableCoreSession coreSession = null;
        try {
            coreSession = CoreInstance.openCoreSession(null);

            // Run unrestricted operation
            return createOrUpdateDocument(coreSession, input, type, name, docId, inputDoc);

        } finally {
            final CloseableCoreSession session2close = coreSession;
            RequestContext.getActiveContext().addRequestCleanupHandler(new RequestCleanupHandler() {

                @Override
                public void cleanup(HttpServletRequest req) {
                    try {
                        session2close.close();
                        lc.logout();
                    } catch (LoginException e) {
                        log.error("Error during request context cleanup", e);
                    }
                }
            });

        }

    }

    public abstract Object getLinkDocument(CoreSession coreSession, String code);
    public abstract Object getDocument(CoreSession coreSession, String docId, String code);
    public abstract Object createOrUpdateDocument(CoreSession coreSession, String input, String type, String name, String docId, DocumentModel inputDoc) throws IOException;
}