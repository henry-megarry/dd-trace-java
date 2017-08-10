package com.datadoghq.trace.writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A bounded queue implementation compatible with the Datadog agent behavior. The class is
 * thread-safe and can be used with concurrency.
 *
 * <p>
 *
 * <p>This class implements a specific behavior when it's full. Each new item added will replace an
 * exisiting one, at a random place/index. The class is backed by an ArrayList in order to perform
 * efficient random remove.
 *
 * @param <T> The element type to store
 */
class WriterQueue<T> {

  private final int capacity;
  private volatile ArrayList<T> list;
  private volatile int elementCount = 0;

  /**
   * Default construct, a capacity must be provided
   *
   * @param capacity the max size of the queue
   */
  WriterQueue(final int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("Capacity couldn't be 0");
    }
    list = new ArrayList<>(capacity);
    this.capacity = capacity;
  }

  /**
   * Return a list containing all elements present in the queue. After the operation, the queue is
   * reset. All action performed on the returned list has no impact to the queue
   *
   * @return a list contain all elements
   */
  public synchronized List<T> getAll() {
    List<T> all = Collections.emptyList();
    all = list;
    list = new ArrayList<>(capacity);
    elementCount = 0;
    return all;
  }

  /**
   * Add an element to the queue. If the queue is full, set the element at a random place in the
   * queue and return the previous one.
   *
   * @param element the element to add to the queue
   * @return null if the queue is not full, otherwise the removed element
   */
  public synchronized T add(final T element) {

    T removed = null;
    if (elementCount < capacity) {
      list.add(element);
      ++elementCount;
    } else {
      final int index = ThreadLocalRandom.current().nextInt(0, elementCount);
      removed = list.set(index, element);
    }
    return removed;
  }

  //  Methods below are essentially used for testing purposes

  /**
   * Return the number of elements set in the queue
   *
   * @return the current size of the queue
   */
  public int size() {
    return elementCount;
  }

  /**
   * Return true if the queue is empty
   *
   * @return true if the queue is empty
   */
  public boolean isEmpty() {
    return elementCount == 0;
  }
}
