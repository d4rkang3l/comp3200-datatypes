/*
 * The MIT License
 *
 * Copyright 2017 Fabrizio Lungo <fl4g12@ecs.soton.ac.uk>
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

package uk.ac.soton.ecs.fl4g12.crdt.datatypes.convergent;

import java.util.Collection;
import java.util.Set;
import static org.junit.Assert.assertTrue;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import uk.ac.soton.ecs.fl4g12.crdt.datatypes.GrowableUpdatableSetAbstractTest;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.DeliveryChannel;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.StateDeliveryChannel;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.StatefulUpdatable;
import uk.ac.soton.ecs.fl4g12.crdt.order.VersionVector;

/**
 * Tests for growable convergent {@link StatefulUpdatable} based {@link Set} implementations.
 *
 * @param <E> the type of values stored in the {@link Set}.
 * @param <K> the type of identifier used to identify nodes.
 * @param <T> the type of the timestamp within the {@link VersionVector}
 * @param <M> the type of {@link SetState} made from the {@link StatefulUpdatable} {@link Set}.
 * @param <S> the type of {@link StatefulUpdatable} based {@link Set} being tested.
 */
public abstract class GrowableConvergentSetAbstractTest<E, K, T extends Comparable<T>, M extends SetState<E, K, VersionVector<K, T>>, S extends Set<E> & StatefulUpdatable<K, VersionVector<K, T>, M>>
    extends GrowableUpdatableSetAbstractTest<E, K, T, M, S> {

  public static <E, K, T extends Comparable<T>, U extends SetState<E, K, VersionVector<K, T>>, S extends Set<E> & StatefulUpdatable<K, VersionVector<K, T>, U>> void assertSetStateContains(
      U setState, E element) {
    assertTrue("The set state should contain the element that was added",
        setState.getState().contains(element));
  }

  public static <E, K, T extends Comparable<T>, U extends SetState<E, K, VersionVector<K, T>>, S extends Set<E> & StatefulUpdatable<K, VersionVector<K, T>, U>> void assertSetStateContainsAll(
      U setState, Collection<E> elements) {
    assertTrue("The set state should contain the elements that were added",
        setState.getState().containsAll(elements));
  }

  @Override
  protected M assertPublish(DeliveryChannel<K, M, ?> channel, VerificationMode mode) {
    StateDeliveryChannel<K, M> stateDeliveryChannel = (StateDeliveryChannel<K, M>) channel;
    Mockito.verify(stateDeliveryChannel, mode).publish();
    return stateDeliveryChannel.getUpdatable().snapshot();
  }

  @Override
  protected void assertAdd(S set, E element, M updateMessage) {
    assertSetStateContains(updateMessage, element);
  }

  @Override
  protected void assertAddAll_Single(S set, E element, M updateMessage) {
    assertSetStateContains(updateMessage, element);
  }

  @Override
  protected void assertAddAll_Multiple(S set, Set<E> elements, M updateMessage) {
    assertSetStateContainsAll(updateMessage, elements);
  }

  @Override
  protected void assertAddAll_Overlap(S set, Set<E> elements, Set<E> newElements, M updateMessage) {
    assertSetStateContainsAll(updateMessage, elements);
  }

  @Override
  protected boolean precedes(S updatable, M message) {
    return updatable.getVersion().precedes(message.getVersion());
  }

  @Override
  protected boolean precedes(M message1, M message2) {
    return message1.getVersion().precedes(message2.getVersion());
  }

  @Override
  protected int compare(M message1, M message2) {
    return message1.compareTo(message2);
  }

}
