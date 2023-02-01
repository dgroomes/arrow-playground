package dgroomes.sortandsearch.internal;

import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;

import static dgroomes.sortandsearch.internal.Comparison.*;

public class BinaryVarCharSearcherOverIndex extends AbstractBinarySearcher {
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
  protected Comparison targetComparedToElementAt(int index) {
    String valueUnderTest = new String(values.get(this.index.get(index)));
    int comparison = target.compareTo(valueUnderTest);
    if (comparison == 0) {
      return EQUAL_TO;
    } else if (comparison < 0) {
      return LESS_THAN;
    } else {
      return GREATER_THAN;
    }
  }
}
