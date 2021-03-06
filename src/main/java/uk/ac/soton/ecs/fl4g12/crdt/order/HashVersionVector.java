/*
 * The MIT License
 *
 * Copyright 2016 Fabrizio Lungo <fl4g12@ecs.soton.ac.uk>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.ac.soton.ecs.fl4g12.crdt.order;

import java.util.HashMap;
import java.util.Set;

/**
 * {@linkplain VersionVector} backed by a {@linkplain HashMap}. Flexible implementation of a
 * {@link VersionVector} allowing the type of identifier and timestamp to be configured. Unlike an
 * {@link ArrayVersionVector}, this implementation works well with non-integer identifiers and
 * sparse integer identifiers.
 *
 * @param <K> the type of the identifier.
 * @param <T> the type of the timestamp.
 */
public final class HashVersionVector<K, T extends Comparable<T>>
    extends AbstractVersionVector<K, T> {

  private final LogicalVersion<T, ?> zero;

  private final HashMap<K, LogicalVersion<T, ?>> vector;

  /**
   * Construct a {@linkplain HashVersionVector}. The {@link LogicalVersion} provided as {@code zero}
   * will be cloned when initialising a new identifier.
   *
   * @param zero a {@link LogicalVersion} representing the zero value of the type wanted for the
   *        timestamps.
   */
  public HashVersionVector(LogicalVersion<T, ?> zero) {
    this(zero, new HashMap<K, LogicalVersion<T, ?>>());
  }

  /**
   * Construct a {@linkplain HashVersionVector} with all fields specified. For internal use only.
   *
   * @param zero a {@link LogicalVersion} representing the zero value of the type wanted for the
   *        timestamps.
   * @param vector the vector to initialise with. This value is not copied.
   */
  private HashVersionVector(LogicalVersion<T, ?> zero, HashMap<K, LogicalVersion<T, ?>> vector) {
    super(zero);
    this.zero = zero.copy();
    this.vector = vector;
  }

  @Override
  public LogicalVersion<T, ?> getLogicalVersion(K id) {
    return vector.get(id);
  }

  @Override
  public Set<K> getIdentifiers() {
    return vector.keySet();
  }

  @Override
  public synchronized LogicalVersion<T, ?> init(K id) {
    if (vector.containsKey(id)) {
      return vector.get(id);
    }
    LogicalVersion<T, ?> version = zero.copy();
    vector.put(id, version);
    return version;
  }

  @Override
  public void sync(K id, T value) {
    if (!vector.containsKey(id)) {
      init(id);
    }
    getLogicalVersion(id).sync(value);
  }

  @Override
  public HashVersionVector<K, T> copy() {
    HashVersionVector<K, T> copy = new HashVersionVector<>(zero);
    copy.sync(this);
    return copy;
  }
}
