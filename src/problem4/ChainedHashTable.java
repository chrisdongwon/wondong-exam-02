package problem4;

import utils.HashTable;
import utils.MiscUtils;
import utils.Pair;
import utils.Reporter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * A simple implementation of hash tables.
 *
 * @author Samuel A. Rebelsky
 * @author Chris Won
 */
public class ChainedHashTable<K, V> implements HashTable<K, V> {

  // +-------+-----------------------------------------------------------
  // | Notes |
  // +-------+

  /*
   * Our hash table is stored as an array of ArrayLists of key/value pairs. Because of the design of
   * Java arrays, we declare that as type Object[] rather than ArrayList<Pair<K,V>>[] and cast
   * whenever we extract an an element. (SamR needs to find a better way to deal with this issue;
   * using ArrayLists doesn't seem like the best idea.)
   * 
   * We use chaining to handle collisions. (Well, we *will* use chaining once the table is
   * finished.)
   * 
   * We expand the hash table when the load factor is greater than LOAD_FACTOR (see constants
   * below).
   * 
   * Since some combinations of data and hash function may lead to a situation in which we get a
   * surprising relationship between values (e.g., all the hash values are 0 mod 32), when expanding
   * the hash table, we incorporate a random number. (Is this likely to make a big difference? Who
   * knows. But it's likely to be fun.)
   * 
   * For experimentation and such, we allow the client to supply a Reporter that is used to report
   * behind-the-scenes work, such as calls to expand the table.
   * 
   * Other features to add.
   * 
   * [ ] A real implementation of containsKey.
   * 
   * [ ] An iterator.
   */

  // +-----------+-------------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The load factor for expanding the table.
   */
  static final double LOAD_FACTOR = 0.5;

  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of values currently stored in the hash table. We use this to determine when to
   * expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the ArrayList of key/value pairs. (We use an array, rather than
   * an ArrayList, because we want to control expansion an ArrayLists of ArrayLists are just weird.)
   */
  Object[] buckets;

  /**
   * An optional reporter to let us observe what the hash table is doing.
   */
  Reporter reporter;

  /**
   * Do we report basic calls?
   */
  boolean REPORT_BASIC_CALLS = false;

  /**
   * Our helpful random number generator, used primarily when expanding the size of the table..
   */
  Random rand;


  // for keeping track of concurrent modification
  int mutation = 0;

  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new hash table.
   */
  public ChainedHashTable() {
    this.rand = new Random();
    this.clear();
    this.reporter = null;
  } // ChainedHashTable

  /**
   * Create a new hash table that reports activities using a reporter.
   */
  public ChainedHashTable(Reporter reporter) {
    this();
    this.reporter = reporter;
  } // ChainedHashTable(Reporter)

  // +--------------------------+----------------------------------------
  // | ChainedHashTable methods |
  // +--------------------------+

  /**
   * Determine if the hash table contains a particular key.
   */
  @Override
  public boolean containsKey(K key) {
    if (key == null) {
      throw new NullPointerException("Null key");
    } // if
    // STUB/HACK
    try {
      get(key);
      return true;
    } catch (Exception e) {
      return false;
    } // try/catch
  } // containsKey(K)

  /**
   * Apply a function to each key/value pair.
   */
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (Pair<K, V> pair : this) {
      action.accept(pair.key(), pair.value());
    } // for
  } // forEach(BiConsumer)

  /**
   * Get the value for a particular key.
   */
  @Override
  public V get(K key) {
    int index = find(key);
    @SuppressWarnings("unchecked")
    ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[index];
    if (alist == null) {
      if (REPORT_BASIC_CALLS && (reporter != null)) {
        reporter.report("get(" + key + ") failed with empty list");
      } // if reporter != null
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } else {
      for (Pair<K, V> pair : alist) {
        if (key.equals(pair.key())) {
          if (REPORT_BASIC_CALLS && (reporter != null)) {
            reporter.report("get(" + key + ") => " + pair.value());
          } // if reporter != null
          return pair.value();
        } // if
      } // for
      if (REPORT_BASIC_CALLS && (reporter != null)) {
        reporter.report("get(" + key + ") failed");
      } // if reporter != null
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } // get
  } // get(K)

  /**
   * Iterate the keys in some order.
   */
  public Iterator<K> keys() {
    return MiscUtils.transform(this.iterator(), (pair) -> pair.key());
  } // keys()

  /**
   * Remove a key/value pair.
   */
  @Override
  public V remove(K key) {
    this.mutation++;

    int index = find(key);
    @SuppressWarnings("unchecked")
    ArrayList<Pair<K, V>> bucket = (ArrayList<Pair<K, V>>) this.buckets[index];
    if (bucket != null) {
      for (int i = 0; i < bucket.size(); i++) {
        Pair<K, V> pair = bucket.get(i);
        if (key.equals(pair.key())) {
          bucket.remove(i);
          --this.size;

          return pair.value();
        } // if
      } // for
    } // if

    return null;
  } // remove(K)

  /**
   * Set a value.
   */
  @SuppressWarnings("unchecked")
  public V set(K key, V value) {
    this.mutation++;

    V result = null;
    // If there are too many entries, expand the table.
    if (this.size > (this.buckets.length * LOAD_FACTOR)) {
      expand();
    } // if there are too many entries

    // Find out where the key belongs and put the pair there.
    int index = find(key);
    ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) this.buckets[index];
    // Special case: Nothing there yet
    if (alist == null) {
      alist = new ArrayList<Pair<K, V>>();
      this.buckets[index] = alist;
    }
    // Look for something with a matching key.
    for (int i = 0; i < alist.size(); i++) {
      Pair<K, V> pair = alist.get(i);
      if (key.equals(pair.key())) {
        if (REPORT_BASIC_CALLS && (reporter != null)) {
          reporter.report("replacing " + pair + " in bucket " + index);
        } // if reporter != null
        V temp = pair.value();
        alist.set(i, new Pair<K, V>(key, value));
        return temp;
      } // if
    } // for
    // If we found nothing with a matching key, add to the end.
    alist.add(new Pair<K, V>(key, value));
    ++this.size;

    // Report activity, if appropriate
    if (REPORT_BASIC_CALLS && (reporter != null)) {
      reporter.report("adding '" + key + ":" + value + "' to bucket " + index);
    } // if reporter != null

    // And we're done
    return result;
  } // set(K,V)

  /**
   * Get the size of the dictionary - the number of values stored.
   */
  @Override
  public int size() {
    return this.size;
  } // size()

  /**
   * Iterate the values in some order.
   */
  public Iterator<V> values() {
    return MiscUtils.transform(this.iterator(), (pair) -> pair.value());
  } // values()

  // +------------------+--------------------------------------------
  // | Iterator methods |
  // +------------------+

  /**
   * Iterate the key/value pairs in some order.
   */
  public Iterator<Pair<K, V>> iterator() {
    return new Iterator<Pair<K, V>>() {
      int index = findFirst();
      int subIndex = 0;

      // -1 if not updated
      int updateIndex = -1;
      int updateSub = -1;

      int initialMutation = mutation;

      public boolean hasNext() {
        if (initialMutation != mutation)
          throw new ConcurrentModificationException();

        if (index >= 0 && index < buckets.length) {
          @SuppressWarnings("unchecked")
          ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[index];
          return (alist != null) && (subIndex < alist.size());
        } // if

        return false;
      } // hasNext()

      public Pair<K, V> next() {
        if (initialMutation != mutation)
          throw new ConcurrentModificationException();

        @SuppressWarnings("unchecked")
        ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[index];

        if (alist == null || alist.isEmpty()) {
          buckets[index] = null;
          index = findNext(index);
        } // if

        @SuppressWarnings("unchecked")
        ArrayList<Pair<K, V>> temp = (ArrayList<Pair<K, V>>) buckets[index];
        alist = temp;

        Pair<K, V> result = alist.get(subIndex);

        updateIndex = index;
        updateSub = subIndex++;

        if (subIndex >= alist.size()) {
          subIndex = 0;
          index = findNext(index);
        } // if

        return result;
      } // next()

      public void remove() {
        if (initialMutation != mutation)
          throw new ConcurrentModificationException();

        if (updateIndex == -1 || updateSub == -1)
          throw new IllegalStateException();

        @SuppressWarnings("unchecked")
        ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[updateIndex];

        alist.remove(updateSub);
        buckets[updateIndex] = alist;

        if (alist.isEmpty()) {
          buckets[updateIndex] = null;
        } // if

        updateIndex = -1;
        updateSub = -1;
        size--;
        mutation++;
      } // remove()

      private int findFirst() {
        for (int i = 0; i < buckets.length; i++) {
          @SuppressWarnings("unchecked")
          ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[i];

          if (alist != null)
            return i;
        } // for

        return -1;
      } // findFirst

      private int findNext(int start) {
        for (int i = start + 1; i < buckets.length; i++) {
          @SuppressWarnings("unchecked")
          ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[i];

          if (alist != null)
            return i;
        } // for

        return -1;
      } // findNext

    }; // new Iterator
  } // iterator()

  // +-------------------+-------------------------------------------
  // | HashTable methods |
  // +-------------------+

  /**
   * Clear the whole table.
   */
  @Override
  public void clear() {
    this.buckets = new Object[41];
    this.size = 0;
  } // clear()

  /**
   * Dump the hash table.
   */
  @Override
  public void dump(PrintWriter pen) {
    pen.println("Capacity: " + this.buckets.length + ", Size: " + this.size);
    for (int i = 0; i < this.buckets.length; i++) {
      @SuppressWarnings("unchecked")
      ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) this.buckets[i];
      if (alist != null) {
        for (Pair<K, V> pair : alist) {
          pen.println("  " + i + ": <" + pair.key() + "(" + pair.key().hashCode() + "):"
              + pair.value() + ">");
        } // for each pair in the bucket
      } // if the current bucket is not null
    } // for each bucket
  } // dump(PrintWriter)

  // +------+------------------------------------------------------------
  // | Misc |
  // +------+

  /**
   * Should we report basic calls? Intended mostly for tracing.
   */
  public void reportBasicCalls(boolean report) {
    REPORT_BASIC_CALLS = report;
  } // reportBasicCalls

  // +---------+---------------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Expand the size of the table.
   */
  void expand() {
    // Figure out the size of the new table
    int newSize = 2 * this.buckets.length + rand.nextInt(10);
    if (REPORT_BASIC_CALLS && (reporter != null)) {
      reporter.report("Expanding to " + newSize + " elements.");
    } // if reporter != null
    // Remember the old table
    Object[] oldBuckets = this.buckets;
    // Create a new table of that size.
    this.buckets = new Object[newSize];
    // Reset the size because we're about to call set too many times.
    this.size = 0;
    // Move all values from the old table to their appropriate
    // location in the new table.
    for (int i = 0; i < oldBuckets.length; i++) {
      if (oldBuckets[i] != null) {
        @SuppressWarnings("unchecked")
        ArrayList<Pair<K, V>> bucket = (ArrayList<Pair<K, V>>) oldBuckets[i];
        for (Pair<K, V> pair : bucket) {
          // reporter.report("Replacing " + pair);
          this.set(pair.key(), pair.value());
        } // for
      } // if
    } // for
  } // expand()

  /**
   * Find the index of the entry with a given key. If there is no such entry, return the index of an
   * entry we can use to store that key.
   */
  int find(K key) {
    if (key == null) {
      throw new NullPointerException("null key");
    } // if

    return Math.abs(key.hashCode()) % this.buckets.length;
  } // find(K)

} // class ChainedHashTable<K,V>

