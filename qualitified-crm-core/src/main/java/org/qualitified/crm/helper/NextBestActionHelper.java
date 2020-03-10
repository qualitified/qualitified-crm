package org.qualitified.crm.helper;

import ca.pfv.spmf.tools.dataset_converter.Formats;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.operations.services.query.DocumentPaginatedQuery;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;

import java.io.*;

public class NextBestActionHelper {
    public String generateTrainingSequences(CoreSession session) throws OperationException, IOException {
        File tempFile = null;
        tempFile = Framework.createTempFile("training_sequences", ".txt");
        Framework.trackFile(tempFile, this);
        Writer output;
        output = new BufferedWriter(new FileWriter(tempFile.getPath(), true));  //clears file every time
        AutomationService automationService = Framework.getService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(session);
        ctx.put("query", "SELECT * FROM Opportunity WHERE opportunity:step = 'Won'");
        DocumentModelList opportunities = (DocumentModelList) automationService.run(ctx, DocumentPaginatedQuery.ID);
        for(DocumentModel opportunity : opportunities){
            ctx.put("query", "SELECT * FROM Interaction WHERE ecm:parentId = '"+opportunity.getId()+"' AND interaction:status = 'DONE' ORDER BY interaction:date ASC");
            DocumentModelList interactions = (DocumentModelList) automationService.run(ctx, DocumentPaginatedQuery.ID);
            String intSequence = "";
            for(DocumentModel interaction : interactions){
                String intActivity = (String)interaction.getPropertyValue("interaction:activity");
                intSequence += intActivity.substring(0,1);
            }
            if(!"".equals(intSequence)){
                output.append(intSequence+"\n");
            }
        }
        output.close();
        File tempSMPFFile = Framework.createTempFile("training_sequences_spmf", ".txt");
        SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
        converter.convert(tempFile.getPath(), tempSMPFFile.getPath(), Formats.Snake, 29, null);
        return tempSMPFFile.getPath();
    }

    public String getCurrentSequence(CoreSession session, String opportunityId) throws OperationException {
        AutomationService automationService = Framework.getService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setCoreSession(session);
        ctx.put("query", "SELECT * FROM Interaction WHERE ecm:parentId = '"+opportunityId+"' AND interaction:status = 'DONE' ORDER BY interaction:date ASC");
        DocumentModelList interactions = (DocumentModelList) automationService.run(ctx, DocumentPaginatedQuery.ID);
        String intSequence = "";
        for(DocumentModel interaction : interactions){
            String intActivity = (String)interaction.getPropertyValue("interaction:activity");
            if(!"".equals(intSequence)){
                intSequence+=",";
            }
            if("Chat".equals(intActivity)){
                intSequence += "S";
            }else {
                intSequence += intActivity.substring(0, 1);
            }
        }
        return intSequence;
    }

    public int getSequenceNumber(String sequencePart){
        if("".equals(sequencePart)){
            return -1;
        }
        int temp = (int)sequencePart.toCharArray()[0];
        int temp_integer = 64;
        if(temp<=90 & temp>=65){
            return (temp-temp_integer) -1;
        }
        return -1;
    }

    public String getAlphaFromSequenceNumber(int sequenceNumber){
        String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","X","Y","Z"};
        return alphabet[sequenceNumber];
    }

    public String getActivityTypeFromAlpha(String alpha){
        String actionType = "";
        switch(alpha){
            case "C":
                actionType = "Call";
                break;
            case "E":
                actionType = "Email";
            break;
            case "M":
                actionType = "Meeting";
                break;
            case "S":
                actionType = "Chat";
                break;
        }
        return actionType;
    }

    public String getAlphaSequenceFromString(String sequence){
        if("".equals(sequence)){
            return "";
        }
        String sequenceAlpha = "";
        String[] sequenceAsTable = sequence.split(",");
        for(String seq : sequenceAsTable){
            if(!"".equals(sequenceAlpha)){
                sequenceAlpha += ",";
            }
            if(seq.equals("Chat")){
                sequenceAlpha += "S";
            }else{
                sequenceAlpha += seq.substring(0,1);
            }
        }
        return sequenceAlpha;
    }
}
