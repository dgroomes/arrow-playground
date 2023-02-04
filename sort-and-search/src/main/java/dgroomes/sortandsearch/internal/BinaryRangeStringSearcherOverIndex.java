package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;

public final class BinaryRangeStringSearcherOverIndex extends AbstractBinaryRangeSearcher<String> {

  private final VarCharVector values;
  private final IntVector index;

  public BinaryRangeStringSearcherOverIndex(VarCharVector values, IntVector index, String target) {
    super(values, target);
    this.values = values;
    this.index = index;
  }

  @Override
  protected String lookup(int indexOfIndex) {
    int indexOfValue = this.index.get(indexOfIndex);
    byte[] bytes = values.get(indexOfValue);
    return new String(bytes);
  }

  @Override
  protected Comparison compare(String target, String valueUnderTest) {
    return TypedComparator.STRING_COMPARATOR.compare(target, valueUnderTest);
  }
}
