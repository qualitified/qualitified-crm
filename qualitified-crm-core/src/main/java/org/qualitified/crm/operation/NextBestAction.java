package org.qualitified.crm.operation;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPT.CPTPredictor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.helper.NextBestActionHelper;

import javax.security.auth.login.LoginContext;

@Operation(id= NextBestAction.ID, category= Constants.CAT_EXECUTION, label="NextBestAction", description="Predict the Next Best Action based on AI")
public class NextBestAction {
    public static final String ID = "Qualitified.NextBestAction";
    private Log logger = LogFactory.getLog(NextBestAction.class);

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public String run(DocumentModel doc) throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        NextBestActionHelper nbaHelper = new NextBestActionHelper();
        String trainingSequencesSPMF = nbaHelper.generateTrainingSequences(ctx.getCoreSession());
        SequenceDatabase trainingSet = new SequenceDatabase();
        trainingSet.loadFileSPMFFormat(trainingSequencesSPMF,Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        // Train the prediction model
        String optionalParameters = "splitLength:13 recursiveDividerMin:1 recursiveDividerMax:5";
        CPTPredictor predictionModel = new CPTPredictor("CPT", optionalParameters);
        predictionModel.Train(trainingSet.getSequences());

        // Phase 2: Sequence prediction
        // We will predict the next symbol after the sequence
        String currentSequence = nbaHelper.getCurrentSequence(ctx.getCoreSession(),doc.getId());
        String[] sequences = nbaHelper.getAlphaSequenceFromString(currentSequence).split(",");
        Sequence sequence = new Sequence(0);
        for(String sequencePart : sequences){
            sequence.addItem(new Item(nbaHelper.getSequenceNumber(sequencePart)));
        }
        String activityType = "";
        Sequence thePrediction = predictionModel.Predict(sequence);
        if(thePrediction.getItems().size()>0) {
            int prediction = thePrediction.getItems().get(0).val;
            String alpha = nbaHelper.getAlphaFromSequenceNumber(prediction);
            activityType = nbaHelper.getActivityTypeFromAlpha(alpha);
        }
        ctx.getLoginStack().pop();
        return "{\"activityType\":\""+activityType+"\"}";
    }
}
