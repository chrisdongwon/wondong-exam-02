package utils;

/**
 * Nodes for doubly-linked structures.
 */
public class Node2<T> {
  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /** 
   * The previous node.
   */
  Node2<T> prev;

  /**
   * The stored value.
   */
  T value;

  /**
   * The next node.
   */
  Node2<T> next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /** 
   * Create a new node.
   */
  public Node2(Node2<T> prev, T value, Node2<T> next) {
    this.prev = prev;
    this.value = value;
    this.next = next;
  } // Node2(Node2<T>, T, Node2<T>)

   /**
    * Create a new node with no links.
    */
   private Node2(T value) {
     this(null, value, null);
   } // Node2(T)

  // +-----------------+---------------------------------------------
  // | Factory methods |
  // +-----------------+

  /**
   * Create a dummy node.
   */
  public static <T> Node2<T> dummyNode() {
    Node2<T> dummy = new Node2<T>(null);
    dummy.prev = dummy;
    dummy.next = dummy;
    return dummy;
  } // dummyNode()

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Insert a new value after this node.  Returns the new node.
   */
  public Node2<T> insertAfter(T value) {
    Node2<T> tmp = new Node2<T>(this, value, this.next);
    this.next.prev = tmp;
    this.next = tmp;
    return tmp;
  } // insertAfter

  /**
   * Insert a new value before this node.  Returns the new node.
   */
  public Node2<T> insertBefore(T value) {
    Node2<T> tmp = new Node2<T>(this.prev, value, this);
    this.prev.next = tmp;
    this.prev = tmp;
    return tmp;
  } // insertBefore
  
  /**
   * Get the next node.
   */
  public Node2<T> next() {
    return this.next;
  } // next()
  
  /**
   * Get the previous node.
   */
  public Node2<T> prev() {
    return this.prev;
  } // prev()

  /**
   * Remove this node.
   */
  public void remove() {
    // Remember the next and prev
    Node2<T> next = this.next;
    Node2<T> prev = this.prev;
    // Clear them out
    this.next = null;
    this.prev = null;
    // Set their pointers
    next.prev = prev;
    prev.next = next;
  } // remove()
  
  /**
   * Set the value in the node.
   */
  public T setValue(T value) {
    T oldValue = this.value;
    this.value = value;
    return oldValue;
  } // setValue(T)
   
  /**
   * Get the value in the node.
   */
  public T value() {
    return this.value;
  } // value()

} // Node2<T>
