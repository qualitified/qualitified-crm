package org.qualitified.crm.csv.export.action;

import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.PRODUCE_IMMEDIATE_OPTION;
import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.STATUS_STREAM;
import static org.nuxeo.lib.stream.computation.AbstractComputation.INPUT_1;
import static org.nuxeo.lib.stream.computation.AbstractComputation.OUTPUT_1;
import static org.nuxeo.lib.stream.computation.AbstractComputation.OUTPUT_2;
import static org.nuxeo.lib.stream.computation.AbstractComputation.OUTPUT_3;

import java.util.Arrays;
import java.util.Map;

import org.qualitified.crm.csv.export.computation.ExposeBlob;
import org.qualitified.crm.csv.export.computation.MakeBlob;
import org.qualitified.crm.csv.export.computation.SortBlob;
import org.qualitified.crm.csv.export.computation.ZipBlob;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.stream.StreamProcessorTopology;
import org.qualitified.crm.csv.export.computation.QualitifiedCSVProjectionComputation;

/**
 * @since 10.3
 */
public class QualitifiedCSVExportAction implements StreamProcessorTopology {

    public static final String ACTION_NAME = "csvExportCustom";

    @Override
    public Topology getTopology(Map<String, String> options) {
        boolean produceImmediate = getOptionAsBoolean(options, PRODUCE_IMMEDIATE_OPTION, false);
        return Topology.builder()
                .addComputation(QualitifiedCSVProjectionComputation::new, //
                        Arrays.asList(INPUT_1 + ":" + ACTION_NAME))
                .addComputation(() -> new MakeBlob(produceImmediate), //
                        Arrays.asList(INPUT_1 + ":" + MakeBlob.NAME, //
                                OUTPUT_1 + ":" + SortBlob.NAME, //
                                OUTPUT_2 + ":" + ZipBlob.NAME, //
                                OUTPUT_3 + ":" + ExposeBlob.NAME))
                .addComputation(SortBlob::new, //
                        Arrays.asList(INPUT_1 + ":" + SortBlob.NAME, //
                                OUTPUT_1 + ":" + ZipBlob.NAME, //
                                OUTPUT_2 + ":" + ExposeBlob.NAME))
                .addComputation(ZipBlob::new, //
                        Arrays.asList(INPUT_1 + ":" + ZipBlob.NAME, //
                                OUTPUT_1 + ":" + ExposeBlob.NAME))
                .addComputation(ExposeBlob::new, //
                        Arrays.asList(INPUT_1 + ":" + ExposeBlob.NAME, //
                                OUTPUT_1 + ":" + STATUS_STREAM))
                .build();
    }

    public static boolean getOptionAsBoolean(Map<String, String> options, String option, boolean defaultValue) {
        String value = (String)options.get(option);
        return value == null ? defaultValue : Boolean.valueOf(value);
    }
}