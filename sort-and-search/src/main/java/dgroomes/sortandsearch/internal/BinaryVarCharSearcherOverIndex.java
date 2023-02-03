package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;

public class BinaryVarCharSearcherOverIndex extends AbstractBinarySearcher<String> {
  private final VarCharVector values;
  private final IntVector index;
  private final String target;

  public BinaryVarCharSearcherOverIndex(VarCharVector values, IntVector index, String target) {
    super(values.getValueCount());
    this.values = values;
    this.index = index;
    this.target = target;
  }

  @Override
  String lookup(int indexOfIndex) {
    int indexOfValue = this.index.get(indexOfIndex);
    byte[] valueBytes = values.get(indexOfValue);
    return new String(valueBytes);
  }

  @Override
  int compare(String valueUnderTest) {
    return target.compareTo(valueUnderTest);
  }
}
