package ca.pfv.spmf;

import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Item;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.Sequence;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequenceprediction.ipredict.predictor.CPT.CPT.CPTPredictor;
import ca.pfv.spmf.tools.dataset_converter.Formats;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;


import java.io.IOException;

public class MainTest {

    public static void main(String args[]) throws IOException {
        // Phase O: Converting dataset
        // Snake Format to SPMF
        SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
        converter.convert("/Users/michaelgena/Dropbox/QUALITIFIED/training_sequences.txt", "/Users/michaelgena/Dropbox/QUALITIFIED/training_sequences_spmf.txt", Formats.Snake, 29, null);

        // Phase 1:  Training
        // Load a file containing the training sequences into memory
        SequenceDatabase trainingSet = new SequenceDatabase();
        trainingSet.loadFileSPMFFormat("/Users/michaelgena/Dropbox/QUALITIFIED/training_sequences_spmf.txt",Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

        // Train the prediction model
        String optionalParameters = "splitLength:13 recursiveDividerMin:1 recursiveDividerMax:5";
        CPTPredictor predictionModel = new CPTPredictor("CPT", optionalParameters);
        predictionModel.Train(trainingSet.getSequences());

        // Phase 2: Sequence prediction
        // We will predict the next symbol after the sequence
        Sequence sequence = new Sequence(0);
        sequence.addItem(new Item(4));
        sequence.addItem(new Item(4));
        sequence.addItem(new Item(4));
        Sequence thePrediction = predictionModel.Predict(sequence);
        System.out.println("The prediction for the next action is: " + thePrediction.getItems().get(0).val);
    }



}
