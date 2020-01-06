package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by michaelgena on 29/12/2019.
 */
@Operation(id= AssertTrue.ID, category= Constants.CAT_EXECUTION, label="AsserTrue", description="")
public class AssertTrue {
    public static final String ID = "Qualitified.AssertTrue";
    private Log logger = LogFactory.getLog(AssertTrue.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "result")
    protected boolean result;

    @OperationMethod
    public void run() throws Exception {
        String scriptId = (String)ctx.get("scriptId");
        DocumentModel script = session.getDocument(new IdRef(scriptId));
        String log = "";
        if(!result){
            logger.warn("Test failed: expected [true], result [false].");
            log = Calendar.getInstance(TimeZone.getDefault()).getTime()+" Test failed: expected [true], result [false]\n";
        }
        if(!log.equals("")){
            String existingLog = script.getPropertyValue("dc:description") != null ? (String)script.getPropertyValue("dc:description") : "";
            script.setPropertyValue("dc:description", log+existingLog);
            script.setPropertyValue("scriptnote:isValid", false);
            session.saveDocument(script);
            session.save();
        }
    }

}