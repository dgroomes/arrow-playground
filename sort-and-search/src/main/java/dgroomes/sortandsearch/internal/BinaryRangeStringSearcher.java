package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.VarCharVector;

public class BinaryRangeStringSearcher extends AbstractBinaryRangeSearcher<String> {
  private final VarCharVector vector;

  public BinaryRangeStringSearcher(VarCharVector vector, String target) {
    super(vector, target);
    this.vector = vector;
  }

  @Override
  protected String lookup(int index) {
    byte[] bytesUnderTest = vector.get(index);
    return new String(bytesUnderTest);
  }

  @Override
  protected Comparison compare(String target, String valueUnderTest) {
    return TypedComparator.STRING_COMPARATOR.compare(target, valueUnderTest);
  }
}
