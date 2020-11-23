package org.qualitified.crm.service;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public interface EmailingService {
    public JSONArray send(List<String> mailDetails) throws JSONException, MailjetSocketTimeoutException, MailjetException;
    public JSONObject fetchHistory(Long MessageID) throws MailjetSocketTimeoutException, MailjetException;
}
