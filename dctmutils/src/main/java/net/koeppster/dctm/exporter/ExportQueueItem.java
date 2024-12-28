package net.koeppster.dctm.exporter;

import java.io.Serializable;
import java.util.Date;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

@Entity(
    indices = {
      @Index(
          type = IndexType.NON_UNIQUE,
          fields = {"status"}),
      @Index(
          fields = {"chronicleId"},
          type = IndexType.UNIQUE)
    })
public class ExportQueueItem implements Serializable {

  public static enum ItemStatus {
    READY,
    IN_PROESS,
    COMPLETE
  }

  @Id private String chronicleId = null;
  private String objectId = null;
  private Date modifiedDate = null;
  private ItemStatus status = null;

  public String getChronicleId() {
    return chronicleId;
  }

  public void setChronicleId(String chronicleId) {
    this.chronicleId = chronicleId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public ItemStatus getStatus() {
    return status;
  }

  public void setStatus(ItemStatus status) {
    this.status = status;
  }

  public ExportQueueItem(
      String chroninalId, String objectId, Date modifiedDate, ItemStatus status) {
    this.chronicleId = chroninalId;
    this.objectId = objectId;
    this.modifiedDate = modifiedDate;
    this.status = status;
  }

  public ExportQueueItem() {}

  public String toString() {
    return "(chronicleId="
        .concat(chronicleId)
        .concat(",objectId=")
        .concat(objectId)
        .concat(",modifyDate=")
        .concat(modifiedDate.toString().concat(",status=").concat(status.name()))
        .concat(")");
  }
}
