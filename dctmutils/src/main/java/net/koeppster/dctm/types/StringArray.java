package net.koeppster.dctm.types;

import java.util.ArrayList;

public class StringArray {
  private String[] array;

  public StringArray(String array) {
    this.array = array.split(",");
  }

  public String[] getArray() {
    return this.array;
  }

  public ArrayList<String> toArrayList() {
    ArrayList<String> list = new ArrayList<>();
    for (String s : this.array) {
      list.add(s);
    }
    return list;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String s : this.array) {
      sb.append(s);
      sb.append(",");
    }
    return sb.toString();
  }

  public static StringArray valueOf(String array) {
    return new StringArray(array);
  }
}
