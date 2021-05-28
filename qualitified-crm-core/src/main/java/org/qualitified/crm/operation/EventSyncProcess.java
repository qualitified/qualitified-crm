package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.operations.services.bulk.AutomationBulkAction;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Operation(id = EventSyncProcess.ID, category = Constants.CAT_EXECUTION, label = "EventSyncProcess", description = "EventSyncProcess...")
public class EventSyncProcess {

    public final static String ID = "Qualitified.EventSyncProcess";
    private Log logger = LogFactory.getLog(EventSyncProcess.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    Map<String, Serializable> fetchEventParam =new HashMap<>();
    @OperationMethod
    public void run() throws OperationException, LoginException, JSONException, InterruptedException {
        fetchEventParam.put("calendarId","");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String previous2weeks = LocalDateTime.now().minusWeeks(2).format(formatter);
        String next2weeks = LocalDateTime.now().plusWeeks(2).format(formatter);


        BulkCommand EventSynchProcessCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Interaction WHERE interaction:activity IN ('Meeting','Call') " +
                        "AND custom:stringField1 IS NOT NULL AND custom:booleanField1 = 1 " +
                        "AND  interaction:date BETWEEN DATE '"+ previous2weeks +"' AND DATE '"+ next2weeks +"' "+
                        "AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState != 'deleted' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID, "Qualitified.FetchFromCalendar")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) fetchEventParam)
                .build();

        // run command
        BulkService EventSynchProcessBulkService = Framework.getService(BulkService.class);
        String EventSynchProcessCommandId = EventSynchProcessBulkService.submit(EventSynchProcessCommand);
        logger.warn("EventSynchProcess running with commandId '"+EventSynchProcessCommandId+"'");
        // await end of computation
        //fetchMailHistoryBulkService.await(fetchMailHistoryCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus fetchMailHistoryBulkStatus = EventSynchProcessBulkService.getStatus(EventSynchProcessCommandId);

    }

}
