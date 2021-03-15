package org.qualitified.crm.service;

import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface EmailingService {
    public MailjetResponse send(Map<String, Object> mailDetails) throws JSONException, MailjetSocketTimeoutException, MailjetException;
    public JSONObject fetchHistory(Long MessageID) throws MailjetSocketTimeoutException, MailjetException;
}
