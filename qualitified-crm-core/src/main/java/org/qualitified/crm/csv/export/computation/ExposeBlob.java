package org.qualitified.crm.csv.export.computation;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.bulk.BulkCodecs;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.action.computation.AbstractBulkComputation;
import org.nuxeo.ecm.core.bulk.action.computation.AbstractTransientBlobComputation;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.core.bulk.message.DataBucket;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.transientstore.api.TransientStoreService;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 10.3
 */
public class ExposeBlob extends AbstractTransientBlobComputation {

    public static final String NAME = "exposeBlobCustom";

    public ExposeBlob() {
        super(NAME);
    }

    @Override
    public void processRecord(ComputationContext context, String documentIdsStreamName, Record record) {
        Codec<DataBucket> codec = BulkCodecs.getDataBucketCodec();
        DataBucket in = codec.decode(record.getData());
        String commandId = in.getCommandId();
        long documents = in.getCount();

        String storeName = Framework.getService(BulkService.class).getStatus(commandId).getAction();
        Blob blob = getBlob(in.getDataAsString(), storeName);
        // store it in download transient store
        TransientStore store = Framework.getService(TransientStoreService.class)
                .getStore(DownloadService.TRANSIENT_STORE_STORE_NAME);
        store.putBlobs(commandId, Collections.singletonList(blob));
        store.setCompleted(commandId, true);

        // update the command status
        BulkStatus delta = BulkStatus.deltaOf(commandId);
        delta.setProcessed(documents);
        String url = Framework.getService(DownloadService.class).getDownloadUrl(commandId);
        Map<String, Serializable> result = Collections.singletonMap("url", url);
        delta.setResult(result);
        AbstractBulkComputation.updateStatus(context, delta);
        context.askForCheckpoint();
    }

}