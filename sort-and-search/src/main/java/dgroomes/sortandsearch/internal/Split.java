package dgroomes.sortandsearch.internal;

import static dgroomes.sortandsearch.internal.Range.*;

/**
 * I'm struggling between calling this a "partition" or a "split" because partition is a strong, well-understood term
 * but I really want to model the "middle point" and its two neighbors (left and right). And that's not really a partition
 * right?
 */
sealed interface Split {

  record SplitPoint(Point point) implements Split {}

  record SplitPointPair(PointPair pointPair) implements Split {}

  record TrueSplit(Range left, int middle, Range right) implements Split {}
}
