package problem1;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;
import utils.SimpleStack;
import utils.Pair;

/**
 * A simple implementation of binary search trees.
 */
public class SimpleImmutableBST<K, V> implements Iterable<Pair<K, V>> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The root of our tree. Initialized to null for an empty tree.
   */
  ImmutableNode<K, V> root;

  /**
   * The comparator used to determine the ordering in the tree.
   */
  Comparator<K> comparator;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new binary search tree that orders values using the specified comparator.
   */
  public SimpleImmutableBST(Comparator<K> comparator) {
    this.comparator = comparator;
    this.root = null;
  } // SimpleBST(Comparator<K>)

  /**
   * Create a new binary search tree that orders values using a not-very-clever default comparator.
   */
  public SimpleImmutableBST() {
    this((k1, k2) -> k1.toString().compareTo(k2.toString()));
  } // SimpleBST()

  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  /**
   * Associate key with value in the tree, creating a new tree.
   */
  public SimpleImmutableBST<K, V> set(K key, V value) {
    SimpleImmutableBST<K, V> newTree = new SimpleImmutableBST<K, V>(this.comparator);
    newTree.root = this.setHelper(this.root, key, value);
    return newTree;
  } // set(K,V)

  /**
   * Get the value associated with a key.
   */
  public V get(K key) {
    return find(key).value();
  } // get(K,V)

  /**
   * Determine the size of the tree.
   */
  public int size() {
    return size(this.root);
  } // size()

  /**
   * Determine if the tree contains a particular key.
   */
  public boolean containsKey(K key) {
    return find(key) != null;
  } // containsKey(K)

  /**
   * Remove a key from the tree, creating a new tree.
   */
  public SimpleImmutableBST<K, V> remove(K key) {
    SimpleImmutableBST<K, V> newTree = new SimpleImmutableBST<K, V>(this.comparator);
    newTree.root = this.removeHelper(this.root, key);
    return newTree;
  } // remove(K)

  /**
   * Apply action to each key/value pair, visiting them in alphabetical key order.
   */
  public void forEach(BiConsumer<? super K, ? super V> action) {
    forEach(this.root, action);
  } // forEach

  /**
   * Determine the size of a subtree rooted at a particular node.
   */
  public int size(ImmutableNode<K, V> node) {
    if (node == null) {
      return 0;
    } else {
      return 1 + size(node.left()) + size(node.right());
    } // if/else
  }
  // size(ImmutableNode<K,V>)

  // +------------------+--------------------------------------------
  // | Iterable methods |
  // +------------------+

  /**
   * Create an iterator for the key/value pairs.
   */
  public Iterator<Pair<K, V>> iterator() {
    return new Iterator<Pair<K, V>>() {
      // A stack of nodes and pairs
      SimpleStack<Object> remaining = new SimpleStack<Object>(SimpleImmutableBST.this.root);

      public boolean hasNext() {
        return !remaining.isEmpty();
      } // hasNext()

      @SuppressWarnings("unchecked")
      public Pair<K, V> next() {
        Object obj = remaining.get();
        if (obj instanceof ImmutableNode<?, ?>) {
          ImmutableNode<K, V> node = (ImmutableNode<K, V>) obj;
          remaining.put(node.right());
          remaining.put(node.contents());
          remaining.put(node.left());
          return next();
        } else {
          return (Pair<K, V>) obj;
        } // ifelse
      } // next()
    }; // new Iterator
  } // Iterator<Pair<K,V>>

  // +----------------------+----------------------------------------
  // | Other public methods |
  // +----------------------+

  /**
   * Dump the tree to some output location.
   */
  public void dump(PrintWriter pen) {
    dump(pen, root, "");
  } // dump(PrintWriter)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Dump a portion of the tree to some output location.
   */
  void dump(PrintWriter pen, ImmutableNode<K, V> node, String indent) {
    if (node == null) {
      pen.println(indent + "<>");
    } else {
      pen.println(indent + node.key() + ": " + node.value());
      if ((node.left() != null) || (node.right() != null)) {
        dump(pen, node.left(), indent + "  ");
        dump(pen, node.right(), indent + "  ");
      } // if has children
    } // else
  } // dump

  /**
   * Find the node containing key.
   */
  ImmutableNode<K, V> find(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if
    return find(key, this.root);
  } // find(K)

  /**
   * Find the node with a given key in a subtree rooted at node.
   */
  ImmutableNode<K, V> find(K key, ImmutableNode<K, V> node) {
    if (node == null) {
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    }
    int comp = comparator.compare(key, node.key());
    if (comp == 0) {
      return node;
    } else if (comp < 0) {
      return find(key, node.left());
    } else {
      return find(key, node.right());
    } // if/else
  } // find(K, ImmuableNode<K,V>)

  /**
   * Apply action to all the key/value pairs in the subtree rooted at node, visiting them in
   * alphabetical order by key.
   */
  void forEach(ImmutableNode<K, V> node, BiConsumer<? super K, ? super V> action) {
    if (node != null) {
      forEach(node.left(), action);
      action.accept(node.key(), node.value());
      forEach(node.right(), action);;
    } // if
  } // forEach(ImmutableNode<K,V>, BiConsumer<...>)

  /**
   * Set a key/value pair in a subtree.
   */
  ImmutableNode<K, V> setHelper(ImmutableNode<K, V> node, K key, V value) {
    if (node == null)
      return new ImmutableNode<K, V>(key, value, null, null);
    else {
      int compare = this.comparator.compare(key, node.key());

      if (compare < 0)
        return new ImmutableNode<K, V>(node.key(), node.value(), setHelper(node.left(), key, value),
            node.right());
      else if (compare > 0)
        return new ImmutableNode<K, V>(node.key(), node.value(), node.left(),
            setHelper(node.right(), key, value));
      else
        return new ImmutableNode<K, V>(key, value, node.left(), node.right());
    } // else
  } // setHelper(ImmutableNode<K,V>, K, V)

  /**
   * Remove a key from the subtree.
   */
  ImmutableNode<K, V> removeHelper(ImmutableNode<K, V> node, K key) {
    if (node != null) {
      int compare = this.comparator.compare(key, node.key());

      if (compare < 0) {
        return new ImmutableNode<K, V>(node.key(), node.value(), removeHelper(node.left(), key),
            node.right());
      } else if (compare > 0) {
        return new ImmutableNode<K, V>(node.key(), node.value(), node.left(),
            removeHelper(node.right(), key));
      } else {
        if (node.left() == null && node.right() == null)
          return null;
        else if (node.left() == null)
          return node.right();
        else if (node.right() == null)
          return node.left();
        else {
          return helper(node.left(), node.right());
        } // else
      } // else
    } // if
    return null;
  } // removeHelper(ImmutableNode<K,V>, K)

  private ImmutableNode<K, V> helper(ImmutableNode<K, V> left, ImmutableNode<K, V> right) {
    if (right == null)
      return left;

    return new ImmutableNode<K, V>(right.key(), right.value(), helper(left, right.left()),
        right.right());
  }

} // class SimpleBST


/**
 * Nodes in a binary search tree.
 */
class ImmutableNode<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The contents (a key/value pair).
   */
  private Pair<K, V> contents;

  /**
   * The left subtree.
   */
  private ImmutableNode<K, V> left;

  /**
   * The right subtree.
   */
  private ImmutableNode<K, V> right;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  public ImmutableNode(K key, V value, ImmutableNode<K, V> left, ImmutableNode<K, V> right) {
    this.contents = new Pair<K, V>(key, value);
    this.left = left;
    this.right = right;
  } // ImmutableNode(K,V)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  public Pair<K, V> contents() {
    return this.contents;
  } // contents()

  public K key() {
    return this.contents.key();
  } // key()

  public ImmutableNode<K, V> left() {
    return this.left;
  } // left()

  public ImmutableNode<K, V> right() {
    return this.right;
  } // right()

  public V value() {
    return this.contents.value();
  } // value()
} // class ImmutableNode<K,V>

