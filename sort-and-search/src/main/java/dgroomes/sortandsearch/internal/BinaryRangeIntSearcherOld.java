//package dgroomes.sortandsearch.internal;
//
//import org.apache.arrow.vector.IntVector;
//
//import java.util.Optional;
//
//import static dgroomes.sortandsearch.internal.Comparison.*;
//
// ONE OF MY ORIGINAL ATTEMPTS! It's to be humbled.
//
//public class BinaryRangeIntSearcherOld {
//  private final IntVector vector;
//  private final int target;
//
//  public BinaryRangeIntSearcherOld(IntVector vector, int target) {
//    this.vector = vector;
//    this.target = target;
//  }
//
//  public Optional<Range> search() {
//    int size = vector.getValueCount();
//    if (size == 0) {
//      return Optional.empty();
//    }
//
//    Range range = Range.of(0, size - 1);
//    Range leftUnsearched;
//    Range rightUnsearched;
//
//    switch (range) {
//      case Range.Point(int index) -> {
//        return switch (targetComparedToElementAt(index)) {
//          case EQUAL_TO -> Optional.of(range);
//          case LESS_THAN, GREATER_THAN -> Optional.empty();
//        };
//      }
//      case Range.PointPair(int left, int right) -> {
//        var leftComparison = targetComparedToElementAt(left);
//        var rightComparison = targetComparedToElementAt(right);
//        return switch (leftComparison) {
//          case EQUAL_TO -> switch (rightComparison) {
//            case EQUAL_TO -> Optional.of(Range.of(left, right));
//            case LESS_THAN, GREATER_THAN -> Optional.of(Range.of(left, left));
//          };
//          case LESS_THAN, GREATER_THAN -> switch (rightComparison) {
//            case EQUAL_TO -> Optional.of(Range.of(right, right));
//            case LESS_THAN, GREATER_THAN -> Optional.empty();
//          };
//        };
//      }
//      case Range.StretchRange(int low, int high) -> {
//        var middle = low + (high - low) / 2;
//        leftUnsearched = Range.of(low, middle - 1);
//        rightUnsearched = Range.of(middle, high);
//      }
//      default ->
//              throw new IllegalStateException("This should not happen. The Java compile 'definite-assignment analysis' should know that this branch is impossible");
//    }
//
//    // Search and expand leftward until the search space is exhausted.
//    Range span = null; // I think I need to support a nil/empty range type... stinks.
//    while (leftUnsearched != null) {
//      BinarySearchStepResult stepResult = BinarySearch.binarySearchStep(leftUnsearched, this::targetComparedToElementAt);
//      switch (stepResult) {
//
//        case BinarySearchStepResult.FoundExhausted(int index) -> {
//          span = Range.extend(span, index);
//          leftUnsearched = null;
//        }
//
//        // Found a match. Because we're extending to the left, we dont need to search the right side.
//        case BinarySearchStepResult.Found(
//                int index, BinarySearchStepResult.Unsearched unsearched
//        ) -> {
//          span = Range.extend(span, index);
//          switch (unsearched) {
//            case BinarySearchStepResult.Unsearched.Left(Range _unsearched) -> leftUnsearched = _unsearched;
//            case BinarySearchStepResult.Unsearched.Right(Range ignore) -> leftUnsearched = null;
//            case BinarySearchStepResult.Unsearched.Both(Range _unsearched, Range ignored) -> leftUnsearched = _unsearched;
//          }
//        }
//
//        case BinarySearchStepResult.TooHigh(var unsearched) -> leftUnsearched = unsearched;
//        case BinarySearchStepResult.TooLow(var unsearched) -> leftUnsearched = unsearched;
//        case BinarySearchStepResult.NotFoundExhausted() -> leftUnsearched = null;
//      }
//    }
//
//    // Search and expand rightward until the search space is exhausted.
//    while (rightUnsearched != null) {
//      BinarySearchStepResult stepResult = BinarySearch.binarySearchStep(rightUnsearched, this::targetComparedToElementAt);
//      switch (stepResult) {
//
//        case BinarySearchStepResult.FoundExhausted(int index) -> {
//          span = Range.extend(span, index);
//          rightUnsearched = null;
//        }
//
//        // Found a match. Because we're extending to the right, we dont need to search the left side.
//        case BinarySearchStepResult.Found(
//                int index, BinarySearchStepResult.Unsearched unsearched
//        ) -> {
//          span = Range.extend(span, index);
//          switch (unsearched) {
//            case BinarySearchStepResult.Unsearched.Left(Range ignore) -> rightUnsearched = null;
//            case BinarySearchStepResult.Unsearched.Right(Range _unsearched) -> rightUnsearched = _unsearched;
//            case BinarySearchStepResult.Unsearched.Both(Range _unsearched, Range ignored) -> rightUnsearched = _unsearched;
//          }
//        }
//
//        case BinarySearchStepResult.TooHigh(var unsearched) -> rightUnsearched = unsearched;
//        case BinarySearchStepResult.TooLow(var unsearched) -> rightUnsearched = unsearched;
//        case BinarySearchStepResult.NotFoundExhausted() -> rightUnsearched = null;
//      }
//    }
//
//    return Optional.ofNullable(span);
//  }
//
//  protected Comparison targetComparedToElementAt(int index) {
//    int valueUnderTest = vector.get(index);
//    int comparison = target - valueUnderTest;
//    if (comparison == 0) {
//      return EQUAL_TO;
//    } else if (comparison < 0) {
//      return LESS_THAN;
//    } else {
//      return GREATER_THAN;
//    }
//  }
//}
