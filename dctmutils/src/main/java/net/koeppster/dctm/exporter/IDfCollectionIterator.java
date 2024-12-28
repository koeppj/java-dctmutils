package net.koeppster.dctm.exporter;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.client.IDfCollection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IDfCollectionIterator implements Iterator<ExportQueueItem> {
    private final IDfCollection collection;
    private boolean hasNextCalled = false;
    private boolean hasNext = false;
    private ExportQueueItem lastItem = null;

    public IDfCollectionIterator(IDfCollection collection) {
        this.collection = collection;
    }

    @Override
    public boolean hasNext() {
        if (!hasNextCalled) {
            try {
                hasNext = collection.next();
                if (hasNext) 
                    lastItem = new ExportQueueItem(collection.getId("i_chronicle_id").getId(), 
                                                   collection.getId("r_object_id").getId(),
                                                   collection.getTime("r_modify_date").getDate(),
                                                   ExportQueueItem.ItemStatus.READY);
                
            } catch (DfException e) {
                DfLogger.error(this, "Error while checking for next element in IDfCollection", null, e);
                throw new RuntimeException("Error while checking for next element in IDfCollection", e);
            }
            hasNextCalled = true;
        }
        return hasNext;
    }

    @Override
    public ExportQueueItem next() {
        if (!hasNext() || !hasNext) {
            throw new NoSuchElementException("No more elements in IDfCollection");
        }
        hasNextCalled = false; // Reset for the next call to `hasNext`
        return lastItem;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported for IDfCollection");
    }

    /**
     * Close the underlying collection when done.
     */
    public void close() throws DfException {
        if (collection != null) {
            collection.close();
        }
    }
}
