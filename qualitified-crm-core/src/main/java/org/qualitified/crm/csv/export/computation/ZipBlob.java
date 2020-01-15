package org.qualitified.crm.csv.export.computation;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.bulk.BulkCodecs;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.action.computation.AbstractTransientBlobComputation;
import org.nuxeo.ecm.core.bulk.message.DataBucket;
import org.nuxeo.ecm.core.utils.BlobUtils;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 10.3
 */
public class ZipBlob extends AbstractTransientBlobComputation {

    private static final Logger log = LogManager.getLogger(ZipBlob.class);

    public static final String NAME = "zipBlobCustom";

    public static final String ZIP_PARAMETER = "zip";

    public ZipBlob() {
        super(NAME);
    }

    @Override
    public void processRecord(ComputationContext context, String inputStreamName, Record record) {
        Codec<DataBucket> codec = BulkCodecs.getDataBucketCodec();
        DataBucket in = codec.decode(record.getData());

        String storeName = Framework.getService(BulkService.class).getStatus(in.getCommandId()).getAction();
        Blob blob = getBlob(in.getDataAsString(), storeName);
        try {
            blob = BlobUtils.zip(blob, blob.getFilename() + ".zip");
        } catch (IOException e) {
            log.error("Unable to zip blob", e);
        }
        storeBlob(blob, in.getCommandId(), storeName);

        DataBucket out = new DataBucket(in.getCommandId(), in.getCount(), getTransientStoreKey(in.getCommandId()));
        context.produceRecord(OUTPUT_1, Record.of(in.getCommandId(), codec.encode(out)));
        context.askForCheckpoint();
    }

}