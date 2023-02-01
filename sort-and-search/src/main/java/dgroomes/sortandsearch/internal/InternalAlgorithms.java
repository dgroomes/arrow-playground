package dgroomes.sortandsearch.internal;

import static dgroomes.sortandsearch.internal.Range.*;
import static dgroomes.sortandsearch.internal.Split.*;

class InternalAlgorithms {

  /**
   * Split a range on a "middle" point. The result is three parts: a left side, a middle, and a right side.
   * <p>
   * Usefully, this method accommodates ranges without a middle (i.e. a point or a point pair).
   */
  static Split split(Range range) {
    return switch (range) {
      // A point is not splittable.
      case Point point -> new SplitPoint(point);
      // A pair of points are not splittable.
      case PointPair pointPair -> new SplitPointPair(pointPair);

      case StretchRange(int low, int high) -> {
        int middle = low + (high - low) / 2;
        Range left = of(low, middle - 1);
        Range right = of(middle + 1, high);
        yield new TrueSplit(left, middle, right);
      }
    };
  }
}
