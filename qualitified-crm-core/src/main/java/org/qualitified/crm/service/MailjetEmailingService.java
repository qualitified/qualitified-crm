package org.qualitified.crm.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.mailjet.client.resource.Messagehistory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

import java.util.List;
import java.util.Map;

public class MailjetEmailingService extends DefaultComponent implements EmailingService {
    MailjetClient client;
    MailjetRequest request;
    MailjetResponse response;
    String apiKey= Framework.getProperty("mailjetService.apikey");
    String secretKey=Framework.getProperty("mailjetService.secretkey");



    @Override
    public JSONArray send(Map<String, Object> mailDetails) throws JSONException, MailjetSocketTimeoutException, MailjetException {

        client = new MailjetClient(apiKey, secretKey, new ClientOptions("v3.1"));
        request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM,
                                        new JSONObject()
                                                .put("Email", mailDetails.get("fromEmail"))
                                                .put("Name", mailDetails.get("fromName")))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", mailDetails.get("toEmail"))
                                                .put("Name", mailDetails.get("toName"))))
                                .put(Emailv31.Message.SUBJECT, mailDetails.get("subject"))
                                .put(Emailv31.Message.TEXTPART, mailDetails.get("textPart"))
                                .put(Emailv31.Message.HTMLPART, mailDetails.get("htmlPart"))
                                .put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")));
        response = client.post(request);

        return response.getJSONArray("Messages");
    }

    @Override
    public JSONObject fetchHistory(Long MessageID) throws MailjetSocketTimeoutException, MailjetException {
        // return "Hello";
        client = new MailjetClient(apiKey, secretKey);
        request = new MailjetRequest(Messagehistory.resource, MessageID);
        response = client.get(request);
        JSONObject mailHistory = new JSONObject(response);
         return mailHistory;


    }
}
