package problem1;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import utils.MiscUtils;
import java.util.Random;

/**
 * Some tests of our simple immutable BSTs.
 *
 * Note that it is comparatively easy to write something that meets these tests but does not
 * correctly implement immutable BSTs. The tests assume good faith on the part of the tree
 * implementation.
 */
class SimpleImmutableBSTTests {

  // +--------------+------------------------------------------------
  // | Useful lists |
  // +--------------+

  /**
   * A word list stolen from some tests that SamR wrote in the distant past.
   */
  static final String[] words = {"aardvark", "anteater", "antelope", "bear", "bison", "buffalo",
      "chinchilla", "cat", "dingo", "elephant", "eel", "flying squirrel", "fox", "goat", "gnu",
      "goose", "hippo", "horse", "iguana", "jackalope", "kestrel", "llama", "moose", "mongoose",
      "nilgai", "orangutan", "opossum", "red fox", "snake", "tarantula", "tiger", "vicuna",
      "vulture", "wombat", "yak", "zebra", "zorilla"};

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * Keys for the current test.
   */
  String[] keys;

  /**
   * Values for the current test.
   */
  Integer[] values;

  /**
   * Trees for the current test. (Objects for the normal reason.)
   */
  Object[] trees;

  // +-------+-------------------------------------------------------
  // | Setup |
  // +-------+

  /**
   * Prepare the key and value arrays.
   */
  @BeforeEach
  void setup() {
    Random rand = new Random();
    // Set up the array of keys
    keys = words.clone();
    MiscUtils.randomlyPermute(keys);
    // Set up the array of values
    values = new Integer[keys.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = rand.nextInt(100);
    } // for
  } // setup()


  // +-------+-------------------------------------------------------
  // | Tests |
  // +-------+

  /**
   * Test that setting works correctly.
   */
  @Test
  void testSet() {
    // Set up an array of trees of gradually increasing size
    trees = new Object[keys.length + 1];
    trees[0] = new SimpleImmutableBST<String, Integer>();
    for (int i = 0; i < keys.length; i++) {
      trees[i + 1] = tree(i).set(keys[i], values[i]);
    } // for

    checkTrees();
  } // testSet()

  /**
   * Test that setting works correctly.
   */
  @Test
  void testRemove() {
    // Build a tree with all of the keys.
    SimpleImmutableBST<String, Integer> bst = new SimpleImmutableBST<String, Integer>();
    for (int i = 0; i < keys.length; i++) {
      bst = bst.set(keys[i], values[i]);
    } // for

    // Fill in the trees array by gradually removing elements from
    // the tree.
    trees = new Object[keys.length + 1];
    trees[keys.length] = bst;
    for (int i = keys.length - 1; i >= 0; i--) {
      trees[i] = tree(i + 1).remove(keys[i]);
    } // for

    checkTrees();
  } // testRemove()

  // +-------+-------------------------------------------------------
  // | Utils |
  // +-------+

  /**
   * Make sure that all of our trees match expectations (that is, that trees[n] has size n, and
   * contains the key/value pairs from indices [0..n) of keys and values.
   */
  void checkTrees() {
    // Verify that the sizes are correct
    for (int size = 0; size < trees.length; size++) {
      assertEquals(size, tree(size).size());
    } // for
    
    // Verify that all of the values that belong in each tree are there,
    // and all those that do not belong are not there.
    for (int size = 0; size < trees.length; size++) {
      SimpleImmutableBST<String, Integer> bst = tree(size);
      for (int i = 0; i < size; i++) {
        assertEquals(bst.get(keys[i]), values[i]);
      } // for
      for (int i = size; i < trees.length; i++) {
        try {
          bst.get(keys[i]);
          fail(keys[i] + " should not be in the tree");
        } catch (Exception e) {
          // Success!
        } // try/catch
      } // for
    } // for
  } // checkTrees()

  /**
   * Get the ith tree in the correct type
   */
  @SuppressWarnings("unchecked")
  SimpleImmutableBST<String, Integer> tree(int i) {
    return (SimpleImmutableBST<String, Integer>) trees[i];
  } // tree(int)
} // SimpleImmutableBSTTests
