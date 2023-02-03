package dgroomes.sortandsearch;

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BaseValueVector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AlgorithmsTest {

  RootAllocator allocator;
  List<BaseValueVector> vectors = new ArrayList<>();

  @BeforeEach
  void setup() {
    allocator = new RootAllocator();
  }

  @AfterEach
  void tearDown() {
    vectors.forEach(BaseValueVector::close);
    allocator.close();
  }


  @Test
  void binarySearch_int_first() {
    IntVector vector = intVector(1, 2, 3);

    Optional<Integer> found = Algorithms.binarySearch(vector, 1);

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(0);
  }

  @Test
  void binarySearch_int_first_indexedVector() {
    IntVector values = intVector(3, 2, 1);
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 1);

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(0);
  }

  @Test
  void binarySearch_string_first_indexedVector() {
    VarCharVector values = varCharVector("c", "b", "a");
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "a");

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(0);
  }

  @Test
  void binarySearch_int_middle() {
    IntVector vector = intVector(1, 2, 3);

    Optional<Integer> found = Algorithms.binarySearch(vector, 2);

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(1);
  }

  @Test
  void binarySearch_int_middle_indexedVector() {
    IntVector values = intVector(3, 2, 1);
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 2);

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(1);
  }

  @Test
  void binarySearch_string_middle_indexedVector() {
    VarCharVector values = varCharVector("c", "b", "a");
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "b");

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(1);
  }


  @Test
  void binarySearch_int_last() {
    IntVector vector = intVector(1, 2, 3);

    Optional<Integer> found = Algorithms.binarySearch(vector, 3);

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(2);
  }

  @Test
  void binarySearch_int_last_indexedVector() {
    IntVector values = intVector(3, 2, 1);
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 3);

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(2);
  }

  @Test
  void binarySearch_string_last_indexedVector() {
    VarCharVector values = varCharVector("c", "b", "a");
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "c");

    assertThat(found).isPresent();
    var foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(2);
  }

  @Test
  void binarySearch_int_noMatch() {
    IntVector vector = intVector(1, 2, 3);

    Optional<Integer> found = Algorithms.binarySearch(vector, 0);

    assertThat(found).isEmpty();
  }

  @Test
  void binarySearch_int_noMatch_indexedVector() {
    IntVector values = intVector(3, 2, 1);
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 0);

    assertThat(found).isEmpty();
  }

  @Test
  void binarySearch_string_noMatch_indexedVector() {
    VarCharVector values = varCharVector("c", "b", "a");
    IntVector index = intVector(2, 1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "d");

    assertThat(found).isEmpty();
  }

  @Test
  void binarySearch_int_emptyVector() {
    IntVector vector = intVector();

    Optional<Integer> found = Algorithms.binarySearch(vector, 0);

    assertThat(found).isEmpty();
  }

  @Test
  void binarySearch_int_emptyVector_indexedVector() {
    IntVector values = intVector();
    IntVector index = intVector();

    Optional<Integer> found = Algorithms.binarySearch(values, index, 0);

    assertThat(found).isEmpty();
  }

  @Test
  void binarySearch_string_emptyVector_indexedVector() {
    VarCharVector values = varCharVector();
    IntVector index = intVector();

    Optional<Integer> found = Algorithms.binarySearch(values, index, "a");

    assertThat(found).isEmpty();
  }

  @Test
  void binarySearch_int_oneElementVector() {
    IntVector vector = intVector(1);

    Optional<Integer> found = Algorithms.binarySearch(vector, 1);

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(0);
  }

  @Test
  void binarySearch_int_oneElementVector_indexedVector() {
    IntVector values = intVector(1);
    IntVector index = intVector(0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 1);

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(0);
  }

  @Test
  void binarySearch_string_oneElementVector_indexedVector() {
    VarCharVector values = varCharVector("a");
    IntVector index = intVector(0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "a");

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(0);
  }

  @Test
  void binarySearch_int_twoElementVector() {
    IntVector vector = intVector(1, 2);

    Optional<Integer> found = Algorithms.binarySearch(vector, 2);

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(1);
  }

  @Test
  void binarySearch_int_twoElementVector_indexedVector() {
    IntVector values = intVector(2, 1);
    IntVector index = intVector(1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 2);

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(1);
  }

  @Test
  void binarySearch_string_twoElementVector_indexedVector() {
    VarCharVector values = varCharVector("b", "a");
    IntVector index = intVector(1, 0);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "b");

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isEqualTo(1);
  }

  @Test
  void binarySearch_int_variegated() {
    IntVector vector = intVector(2, 2, 3, 3, 3, 5, 5, 5, 5, 5, 7, 7, 7, 7, 7, 7, 7, 7);

    Optional<Integer> found = Algorithms.binarySearch(vector, 3);

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isBetween(2, 4);
  }

  @Test
  void binarySearch_int_variegated_indexedVector() {
    IntVector values = intVector(2, 2, 3, 3, 3, 5, 5, 5, 5, 5, 7, 7, 7, 7, 7, 7, 7, 7);
    IntVector index = intVector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);

    Optional<Integer> found = Algorithms.binarySearch(values, index, 3);

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isBetween(2, 4);
  }

  @Test
  void binarySearch_string_variegated_indexedVector() {
    VarCharVector values = varCharVector("a", "a", "b", "b", "b", "c", "c", "c", "c", "c", "d", "d", "d", "d", "d", "d", "d", "d");
    IntVector index = intVector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);

    Optional<Integer> found = Algorithms.binarySearch(values, index, "b");

    assertThat(found).isPresent();
    Integer foundIndex = found.get();
    assertThat(foundIndex).isBetween(2, 4);
  }

  @Test
  void binaryRangeSearch_runOfSize1_inTheMiddle() {
    IntVector vector = intVector(1, 2, 3);

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 2);

    assertThat(found).isPresent();
    Algorithms.Range range = found.get();
    assertThat(range.low()).isEqualTo(1);
    assertThat(range.high()).isEqualTo(1);
  }

  @Test
  void binaryRangeSearch_runOfSize2_inTheMiddle() {
    IntVector vector = intVector(1, 2, 2, 3);

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 2);

    assertThat(found).isPresent();
    Algorithms.Range range = found.get();
    assertThat(range.low()).isEqualTo(1);
    assertThat(range.high()).isEqualTo(2);
  }

  @Test
  void binaryRangeSearch_runOfSize2_atThelow() {
    IntVector vector = intVector(1, 1, 2);

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 1);

    assertThat(found).isPresent();
    Algorithms.Range range = found.get();
    assertThat(range.low()).isEqualTo(0);
    assertThat(range.high()).isEqualTo(1);
  }

  @Test
  void binaryRangeSearch_runOfSize2_atThehigh() {
    IntVector vector = intVector(1, 2, 2);

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 2);

    assertThat(found).isPresent();
    Algorithms.Range range = found.get();
    assertThat(range.low()).isEqualTo(1);
    assertThat(range.high()).isEqualTo(2);
  }

  @Test
  void binaryRangeSearch_emptyVector() {
    IntVector vector = intVector();

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 1);

    assertThat(found).isEmpty();
  }

  @Test
  void binaryRangeSearch_oneElementVector() {
    IntVector vector = intVector(1);

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 1);

    assertThat(found).isPresent();
    Algorithms.Range range = found.get();
    assertThat(range.low()).isEqualTo(0);
    assertThat(range.high()).isEqualTo(0);
  }

  @Test
  void binaryRangeSearch_large_range() {
    IntVector vector = intVector(1, 1, 1, 1, 1);

    Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 1);
    assertThat(found).isPresent();
    Algorithms.Range range = found.get();
    assertThat(range.low()).isEqualTo(0);
    assertThat(range.high()).isEqualTo(4);
  }


  @Test
  void binaryRangeSearch_variegated() {
    IntVector vector = intVector(2, 2, 3, 3, 3, 5, 5, 5, 5, 5, 7, 7, 7, 7, 7, 7, 7);

    {
      Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 2);
      assertThat(found).isPresent();
      Algorithms.Range range = found.get();
      assertThat(range.low()).isEqualTo(0);
      assertThat(range.high()).isEqualTo(1);
    }

    {
      Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 3);
      assertThat(found).isPresent();
      Algorithms.Range range = found.get();
      assertThat(range.low()).isEqualTo(2);
      assertThat(range.high()).isEqualTo(4);
    }

    {
      Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 5);
      assertThat(found).isPresent();
      Algorithms.Range range = found.get();
      assertThat(range.low()).isEqualTo(5);
      assertThat(range.high()).isEqualTo(9);
    }

    {
      Optional<Algorithms.Range> found = Algorithms.binaryRangeSearch(vector, 7);
      assertThat(found).isPresent();
      Algorithms.Range range = found.get();
      assertThat(range.low()).isEqualTo(10);
      assertThat(range.high()).isEqualTo(16);
    }
  }

  /**
   * Convenience method to create an {@link IntVector}. I haven't been able to find utility methods like this in the
   * Arrow API. I would love to know the idiomatic way to create vectors for testing.
   */
  IntVector intVector(int... values) {
    var vector = new IntVector("", allocator);
    vectors.add(vector);
    vector.allocateNew(values.length);
    vector.setValueCount(values.length);
    for (int i = 0; i < values.length; i++) {
      vector.set(i, values[i]);
    }
    return vector;
  }

  /**
   * Convenience method to create a {@link VarCharVector}.
   */
  VarCharVector varCharVector(String... values) {
    var vector = new VarCharVector("", allocator);
    vectors.add(vector);
    vector.allocateNew(values.length);
    vector.setValueCount(values.length);
    for (int i = 0; i < values.length; i++) {
      vector.set(i, values[i].getBytes());
    }
    return vector;
  }
}
