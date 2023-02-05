package dgroomes.sortandsearch.algorithms.internal;

import dgroomes.sortandsearch.algorithms.Comparison;
import dgroomes.sortandsearch.algorithms.TypedComparator;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;

public final class BinaryVarCharSearcherOverIndex extends AbstractBinarySearcher<String> {
  private final VarCharVector values;
  private final IntVector index;

  public BinaryVarCharSearcherOverIndex(VarCharVector values, IntVector index, String target) {
    super(values.getValueCount(), target);
    this.values = values;
    this.index = index;
  }

  @Override
  protected String lookup(int indexOfIndex) {
    int indexOfValue = this.index.get(indexOfIndex);
    byte[] valueBytes = values.get(indexOfValue);
    return new String(valueBytes);
  }

  @Override
  protected Comparison compare(String target, String valueUnderTest) {
    return TypedComparator.STRING_COMPARATOR.compare(target, valueUnderTest);
  }
}