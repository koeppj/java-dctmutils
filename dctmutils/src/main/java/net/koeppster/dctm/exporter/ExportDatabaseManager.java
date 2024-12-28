package net.koeppster.dctm.exporter;

import com.documentum.fc.common.DfLogger;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.Cursor;
import org.dizitart.no2.repository.ObjectRepository;

public class ExportDatabaseManager {

  public class QueueItemCursor {
    private long numItems = 0;
    private Iterator<ExportQueueItem> items;

    public long getNumItems() {
      return numItems;
    }

    public Iterator<ExportQueueItem> getItems() {
      return items;
    }

    public QueueItemCursor(long numItems, Iterator<ExportQueueItem> items) {
      this.numItems = numItems;
      this.items = items;
    }
  }

  private Nitrite db = null;
  private ObjectRepository<ExportQueueItem> queueItems = null;

  public ExportDatabaseManager(File arg0, boolean arg1) {
    MVStoreModule module = MVStoreModule.withConfig().filePath(arg0).compress(true).build();
    db = Nitrite.builder().loadModule(module).loadModule(new JacksonMapperModule()).openOrCreate();
    queueItems = db.getRepository(ExportQueueItem.class);
    if (arg1) {
        queueItems.clear();
    }
  }

  public void putItem(String arg0, String arg1, Date arg3) {
    DfLogger.debug(this, "putItem(arg0={0})", new String[] {arg0}, null);
    ExportQueueItem item = queueItems.getById(arg0);
    if (null == item) {
      DfLogger.debug(this, "Creating new Item for {0}", new String[] {arg0}, null);
      queueItems.insert(new ExportQueueItem(arg0, arg1, arg3, ExportQueueItem.ItemStatus.READY));
    } else if (!arg1.equals(item.getObjectId())) {
      DfLogger.debug(this, "Setting to READY for New Version {0}", new String[] {arg0}, null);
      item.setObjectId(arg1);
      item.setModifiedDate(arg3);
      item.setStatus(ExportQueueItem.ItemStatus.READY);
      queueItems.update(item);
    } else if (!arg3.equals(item.getModifiedDate())) {
      DfLogger.debug(this, "Setting to READY for Updated Object {0}", new String[] {arg0}, null);
      item.setModifiedDate(arg3);
      item.setStatus(ExportQueueItem.ItemStatus.READY);
      queueItems.update(item);
    }
  }

  public void putItem(ExportQueueItem arg0) {
    DfLogger.debug(this, "putItem(arg0={0})", new String[] {arg0.toString()}, null);
    putItem(arg0.getChronicleId(), arg0.getObjectId(), arg0.getModifiedDate());
  }

  public QueueItemCursor getOpenItems() {
    Cursor<ExportQueueItem> openItems =
        queueItems.find(FluentFilter.where("status").eq(ExportQueueItem.ItemStatus.READY));
    DfLogger.debug(
        this,
        "Returning cursor with size of {0}",
        new String[] {Long.toString(openItems.size())},
        null);
    return new QueueItemCursor(openItems.size(), openItems.iterator());
  }

  public Set<ExportQueueItem> getOpenItemList() {
    Cursor<ExportQueueItem> openItems =
        queueItems.find(FluentFilter.where("status").eq(ExportQueueItem.ItemStatus.READY));
    return openItems.toSet();
  }

  public void markItemComplete(String arg0) {
    DfLogger.debug(this, "Marking Complete: {0}", new String[] {arg0.toString()}, null);
    ExportQueueItem item = queueItems.getById(arg0);
    item.setStatus(ExportQueueItem.ItemStatus.COMPLETE);
    WriteResult result = queueItems.update(item);
    DfLogger.debug(
        this,
        "Result of write: {0}",
        new String[] {Integer.toString(result.getAffectedCount())},
        null);
  }

  public void markItemInprogress(String arg0) {
    DfLogger.debug(this, "Marking In-Progress: {0}", new String[] {arg0.toString()}, null);
    ExportQueueItem item = queueItems.getById(arg0);
    item.setStatus(ExportQueueItem.ItemStatus.IN_PROESS);
    WriteResult result = queueItems.update(item);
    DfLogger.debug(
        this,
        "Result of write: {0}",
        new String[] {Integer.toString(result.getAffectedCount())},
        null);
  }

  public void shutdown() {
    if (null != queueItems) {
        queueItems.close();
    }
    if (null != db) {
        db.close();
    }
  }
}
