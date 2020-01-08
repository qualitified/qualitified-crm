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
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by michaelgena on 28/11/2019.
 */
@Operation(id= AssertEquals.ID, category= Constants.CAT_EXECUTION, label="AssertEquals", description="")
public class AssertEquals {
    public static final String ID = "Qualitified.AssertEquals";
    private Log logger = LogFactory.getLog(AssertEquals.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "expected")
    protected String expected;

    @Param(name = "result")
    protected String result;

    @Param(name = "message")
    protected String message;

    @OperationMethod
    public void run() throws Exception {
        String scriptId = (String)ctx.get("scriptId");
        DocumentModel script = session.getDocument(new IdRef(scriptId));
        String log = "";
        if(expected == null){
            logger.error("Test failed: "+message+", expected value is null.");
            log += Calendar.getInstance(TimeZone.getDefault()).getTime()+" Test failed: expected value is null.\n";
        }else if(result == null){
            logger.error("Test failed: "+message+", result value is null.");
            log += Calendar.getInstance(TimeZone.getDefault()).getTime()+" Test failed: expected value is null.\n";
        }else if(!expected.equals(result)) {
            logger.error("Test failed: "+message+", expected ["+expected+"], result ["+result+"]");
            log += Calendar.getInstance(TimeZone.getDefault()).getTime()+" Test failed: "+message+", expected ["+expected+"], result ["+result+"]\n";
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