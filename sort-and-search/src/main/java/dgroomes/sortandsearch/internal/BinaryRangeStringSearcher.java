package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.VarCharVector;

public class BinaryRangeStringSearcher extends AbstractBinaryRangeSearcher<String> {
  private final VarCharVector vector;
  private final String target;

  public BinaryRangeStringSearcher(VarCharVector vector, String target) {
    super(vector);
    this.vector = vector;
    this.target = target;
  }

  @Override
  String lookup(int index) {
    byte[] bytesUnderTest = vector.get(index);
    return new String(bytesUnderTest);
  }

  @Override
  int compare(String valueUnderTest) {
    return target.compareTo(valueUnderTest);
  }

}
