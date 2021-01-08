package org.qualitified.crm.operation;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Operation(id = FetchMailHistory.ID, category = Constants.CAT_EXECUTION, label = "FetchMailHistory", description = "FetchMailHistory...")
public class FetchMailHistory {

    public final static String ID = "Qualitified.FetchMailHistory";
    private Log logger = LogFactory.getLog(FetchMailHistory.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    @Param(name = "Status")
    protected String status;


    private String MessageID;


    @OperationMethod
    public void run(DocumentModel interactionDoc) throws OperationException, LoginException, MailjetSocketTimeoutException, JSONException, MailjetException {
        EmailingService emailingService = Framework.getService(EmailingService.class);
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        MessageID = (String) interactionDoc.getPropertyValue("interaction:messageID");
        JSONObject response= emailingService.fetchHistory(Long.parseLong(MessageID));
        Calendar cal = Calendar.getInstance();

            List<Map<String, Object>> mailHistory = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < response.getLong("count"); i++) {
            Map<String, Object> Details = new HashMap<String, Object>();
            JSONObject dataObject =response.getJSONArray("data").getJSONObject(i);
            Details.put("comment",dataObject.getString("Comment"));
            cal.setTimeInMillis(dataObject.getLong("EventAt"));
            Details.put("eventAt",cal.getTime());
            Details.put("eventTypes", dataObject.getString("EventType"));
            Details.put("state", dataObject.getString("State"));
            Details.put("userAgent", dataObject.getString("Useragent"));
            Details.put("userAgentID", Long.toString(dataObject.getLong("UseragentID")));
            mailHistory.add(Details);
        }
        interactionDoc.setPropertyValue("interaction:data", (Serializable) mailHistory);
        interactionDoc.setPropertyValue("interaction:statusMail", (Serializable) mailHistory.get(mailHistory.size()-1).get("eventTypes"));

        documentManager.saveDocument(interactionDoc);

    }

}
