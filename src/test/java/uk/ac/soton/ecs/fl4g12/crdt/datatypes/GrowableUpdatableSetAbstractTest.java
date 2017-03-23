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

package uk.ac.soton.ecs.fl4g12.crdt.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.DeliveryChannel;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.UpdateMessage;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.VersionedUpdatable;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.VersionedUpdateMessage;
import uk.ac.soton.ecs.fl4g12.crdt.order.VersionVector;

/**
 * Tests for growable {@link VersionedUpdatable} {@link Set} implementations.
 *
 * @param <E> the type of values stored in the {@link Set}.
 * @param <K> the type of identifier used to identify nodes.
 * @param <T> the type of the timestamp within the {@link VersionVector}
 * @param <U> the type of snapshot made from this state.
 * @param <S> the type of {@link VersionedUpdatable} based {@link Set} being tested.
 */
public abstract class GrowableUpdatableSetAbstractTest<E, K, T extends Comparable<T>, U extends VersionedUpdateMessage<K, T>, S extends Set<E> & VersionedUpdatable<K, T, U>>
    implements SetTestInterface<E, S> {

  private static final Logger LOGGER =
      Logger.getLogger(GrowableUpdatableSetAbstractTest.class.getName());

  public static final int MAX_OPERATIONS = 10;

  @Captor
  public ArgumentCaptor<U> updateMessageCaptor;

  @Before
  public void setUpUpdateMessageCaptor() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Get a {@link VersionedUpdateMessage} which adds the given elements to the set.
   *
   * @param set the set which the update will be delivered to.
   * @param identifier the identifier of the node that the update message should come from.
   * @param version the version vector at the time of the update,
   * @param elements the elements that should be added as part of the update message.
   * @return an {@link VersionedUpdateMessage} representing the addition of the given elements.
   */
  protected abstract U getAddUpdate(S set, K identifier, VersionVector<K, T> version,
      Collection<E> elements);

  /**
   * Get a {@link VersionedUpdateMessage} which adds the given elements to the set.
   *
   * @param set the set which the update will be delivered to.
   * @param identifier the identifier of the node that the update message should come from.
   * @param version the version vector at the time of the update,
   * @param elements the elements that should be added as part of the update message.
   * @return an {@link VersionedUpdateMessage} representing the addition of the given elements.
   * @see #getAddUpdate(Set, Object, VersionVector, Collection) for more details.
   */
  protected final U getAddUpdate(S set, K identifier, VersionVector<K, T> version, E... elements) {
    return getAddUpdate(set, identifier, version, Arrays.asList(elements));
  }

  /**
   * Assert that the update message is as expected. This includes checking that the updateMessage
   * identifier matches that of the set and that the version matches the expected version.
   *
   * @param set the {@link Set} instance being tested.
   * @param expectedVersion the expected {@link VersionVector} to compare with.
   * @param updateMessage the update message to make the assertions against.
   */
  protected final void assertExpectedUpdateMessage(S set, VersionVector<K, T> expectedVersion,
      U updateMessage) {
    assertEquals("Update message identifier should be the same as the set's", set.getIdentifier(),
        updateMessage.getIdentifier());
    assertTrue("Update version should be as expected",
        updateMessage.getVersionVector().identical(expectedVersion));
  }

  /**
   * Ensure that when an element is added, that the change is published to the
   * {@linkplain DeliveryChannel}.
   */
  @Test
  public void testAdd() {
    LOGGER.log(Level.INFO, "testAdd: "
        + "Ensure that when an element is added, that the change is published to the DeliveryChannel.");
    final S set = getSet();

    final VersionVector<K, T> expectedVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      expectedVersionVector.increment(set.getIdentifier());
      final DeliveryChannel<K, U> deliveryChannel = set.getDeliveryChannel();

      final E element = getElement(i);
      Mockito.reset(deliveryChannel);
      set.add(element);

      Mockito.verify(deliveryChannel).publish(updateMessageCaptor.capture());
      Mockito.verifyNoMoreInteractions(deliveryChannel);

      U updateMessage = updateMessageCaptor.getValue();

      assertExpectedUpdateMessage(set, expectedVersionVector, updateMessage);
      assertAdd(set, element, updateMessage);
    }
  }

  /**
   * Make assertions for the {@linkplain #testAdd()} method. Needs to ensure that the update message
   * is accurate given the element that was added.
   *
   * @param set the {@link Set} instance being tested.
   * @param element the element that was added to the {@link Set} being tested.
   * @param updateMessage the {@link UpdateMessage} that was published by the {@link Set} being
   *        tested.
   */
  protected abstract void assertAdd(S set, E element, U updateMessage);

  /**
   * Ensure that when an element that has already been added is added, that no change is published
   * to the {@linkplain DeliveryChannel}.
   */
  @Test
  public void testAdd_Duplicate() {
    LOGGER.log(Level.INFO,
        "testAdd_Duplicate: Ensure that when an element that has already been added is added, "
            + "that no change is published to the DeliveryChannel.");
    final S set = getSet();

    final VersionVector<K, T> expectedVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      expectedVersionVector.increment(set.getIdentifier());
      final DeliveryChannel<K, U> deliveryChannel = set.getDeliveryChannel();

      final E element = getElement(i);
      set.add(element);
      Mockito.reset(deliveryChannel);
      set.add(element);
      Mockito.verifyZeroInteractions(deliveryChannel);

      assertAdd_Duplicate(set, element);
    }
  }

  /**
   * Make assertions for the {@linkplain #testAdd_Duplicate()} method.
   *
   * @param set the {@link Set} instance being tested.
   * @param element the element that was added to the {@link Set} being tested.
   */
  protected void assertAdd_Duplicate(S set, E element) {
    // Do nothing by default
  }

  /**
   * Ensure that when an element is added using {@linkplain Set#addAll(java.util.Collection)}, that
   * the change is published to the {@linkplain DeliveryChannel}.
   */
  @Test
  public void testAddAll_Single() {
    LOGGER.log(Level.INFO, "testAddAll_Single: Ensure that when an element is added using addAll, "
        + "that the change is published to the DeliveryChannel.");
    final S set = getSet();

    final VersionVector<K, T> expectedVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      expectedVersionVector.increment(set.getIdentifier());
      final DeliveryChannel<K, U> deliveryChannel = set.getDeliveryChannel();

      final E element = getElement(i);
      Mockito.reset(deliveryChannel);
      set.addAll(new HashSet<>(Arrays.asList(getElement(i))));

      Mockito.verify(deliveryChannel).publish(updateMessageCaptor.capture());
      Mockito.verifyNoMoreInteractions(deliveryChannel);

      U updateMessage = updateMessageCaptor.getValue();

      assertExpectedUpdateMessage(set, expectedVersionVector, updateMessage);
      assertAddAll_Single(set, element, updateMessage);
    }
  }

  /**
   * Make assertions for the {@linkplain #testAddAll_Single()} method. Needs to ensure that the
   * update message is accurate given the element that was added.
   *
   * @param set the {@link Set} instance being tested.
   * @param element the element that was added to the {@link Set} being tested.
   * @param updateMessage the {@link UpdateMessage} that was published by the {@link Set} being
   *        tested.
   */
  protected abstract void assertAddAll_Single(S set, E element, U updateMessage);

  /**
   * Ensure that when elements are added using {@linkplain Set#addAll(java.util.Collection)}, that
   * the change is published to the {@linkplain DeliveryChannel}.
   */
  @Test
  public void testAddAll_Multiple() {
    LOGGER.log(Level.INFO, "testAddAll_Multiple: Ensure that when elements are added using addAll, "
        + "that the change is published to the DeliveryChannel.");
    final S set = getSet();

    final VersionVector<K, T> expectedVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      expectedVersionVector.increment(set.getIdentifier());
      final DeliveryChannel<K, U> deliveryChannel = set.getDeliveryChannel();

      final HashSet<E> elements = new HashSet<>(
          Arrays.asList(getElement(3 * i), getElement(3 * i + 1), getElement(3 * i + 2)));
      Mockito.reset(deliveryChannel);
      set.addAll(elements);

      Mockito.verify(deliveryChannel).publish(updateMessageCaptor.capture());
      Mockito.verifyNoMoreInteractions(deliveryChannel);

      U updateMessage = updateMessageCaptor.getValue();

      assertExpectedUpdateMessage(set, expectedVersionVector, updateMessage);
      assertAddAll_Multiple(set, elements, updateMessage);
    }
  }

  /**
   * Make assertions for the {@linkplain #testAddAll_Multiple()} method. Needs to ensure that the
   * update message is accurate given the elements that were added.
   *
   * @param set the {@link Set} instance being tested.
   * @param elements A {@link Collection} that contains the elements that were added to the
   *        {@link Set} being tested.
   * @param updateMessage the {@link UpdateMessage} that was published by the {@link Set} being
   *        tested.
   */
  protected abstract void assertAddAll_Multiple(S set, Set<E> elements, U updateMessage);

  /**
   * Ensure that when an elements are added using {@linkplain Set#addAll(java.util.Collection)},
   * that only new elements are published to the {@linkplain DeliveryChannel}.
   */
  @Test
  public void testAddAll_Overlap() {
    LOGGER.log(Level.INFO,
        "testAddAll_Overlap: Ensure that when an elements are added using addAll, "
            + "that only new elements are published to the DeliveryChannel.");
    final S set = getSet();

    final VersionVector<K, T> expectedVersionVector = set.getVersion().copy();

    set.addAll(new HashSet<>(Arrays.asList(getElement(0), getElement(1), getElement(2))));
    expectedVersionVector.increment(set.getIdentifier());

    for (int i = 1; i < MAX_OPERATIONS; i++) {
      expectedVersionVector.increment(set.getIdentifier());
      final DeliveryChannel<K, U> deliveryChannel = set.getDeliveryChannel();

      final HashSet<E> elements = new HashSet<>(
          Arrays.asList(getElement(2 * i), getElement(2 * i + 1), getElement(2 * i + 2)));
      final HashSet<E> newElements =
          new HashSet<>(Arrays.asList(getElement(2 * i + 1), getElement(2 * i + 2)));
      Mockito.reset(deliveryChannel);
      set.addAll(elements);

      Mockito.verify(deliveryChannel).publish(updateMessageCaptor.capture());
      Mockito.verifyNoMoreInteractions(deliveryChannel);

      U updateMessage = updateMessageCaptor.getValue();

      assertExpectedUpdateMessage(set, expectedVersionVector, updateMessage);
      assertAddAll_Overlap(set, elements, newElements, updateMessage);
    }
  }

  /**
   * Make assertions for the {@linkplain #testAddAll_Overlap()} method. Needs to ensure that the
   * update message is accurate given the elements that were added.
   *
   * @param set the {@link Set} instance being tested.
   * @param elements A {@link Collection} that contains the elements that were added to the
   *        {@link Set} being tested.
   * @param newElements A {@link Collection} that contains the new elements that were added to the
   *        {@link Set} being tested.
   * @param updateMessage the {@link UpdateMessage} that was published by the {@link Set} being
   *        tested.
   */
  protected abstract void assertAddAll_Overlap(S set, Set<E> elements, Set<E> newElements,
      U updateMessage);

  /**
   * Ensure that when duplicates element is added, that no change is published to the
   * {@linkplain DeliveryChannel}.
   */
  @Test
  public void testAddAll_Duplicate() {
    LOGGER.log(Level.INFO, "testAdd_Publish: "
        + "Ensure that when duplicate elements are added, that no change is published to the DeliveryChannel.");
    final S set = getSet();

    final VersionVector<K, T> expectedVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      expectedVersionVector.increment(set.getIdentifier());
      final DeliveryChannel<K, U> deliveryChannel = set.getDeliveryChannel();

      final Collection<E> elements = new ArrayList<>();
      for (int j = 0; j < MAX_OPERATIONS; j++) {
        elements.add(getElement(i * MAX_OPERATIONS + j));
      }
      set.addAll(elements);
      Mockito.reset(deliveryChannel);
      set.addAll(elements);
      Mockito.verifyZeroInteractions(deliveryChannel);

      assertAddAll_Duplicate(set, elements);
    }
  }

  /**
   * Make assertions for the {@linkplain #testAddAll_Duplicate()} method.
   *
   * @param set the {@link Set} instance being tested.
   * @param elements A {@link Set} that contains the elements that were added to the {@link Set}
   *        being tested.
   */
  protected void assertAddAll_Duplicate(S set, Collection<E> elements) {
    // Do nothing by default
  }

  /**
   * Test applying an update with a single element to be added.
   *
   * @throws Exception if the test fails.
   */
  @Test
  public void testUpdate_Add_Single() throws Exception {
    LOGGER.log(Level.INFO,
        "testUpdate_Add_Single: Test applying an update with a single element to be added.");
    final S set = getSet();

    final VersionVector<K, T> messageVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      final E element = getElement(i);

      messageVersionVector.increment(set.getIdentifier());
      final U message = getAddUpdate(set, set.getIdentifier(), messageVersionVector, element);

      set.update(message);

      assertTrue("The set should contain the element which was added", set.contains(element));

      assertUpdate_Add_Single(set, message);
    }
  }

  /**
   * Make assertions for the {@linkplain #testUpdate_Add_Single()} method. Needs to ensure that the
   * update message is accurate given the element that was added.
   *
   * @param set the {@link Set} instance being tested.
   * @param updateMessage the {@link UpdateMessage} that was applied to the {@link Set} being
   *        tested.
   */
  protected void assertUpdate_Add_Single(S set, U updateMessage) {
    // Do nothing by default
  }

  /**
   * Test applying an update with multiple elements to be added.
   *
   * @throws Exception if the test fails.
   */
  @Test
  public void testUpdate_Add_Multiple() throws Exception {
    LOGGER.log(Level.INFO,
        "testUpdate_Add_Multiple: Test applying an update with multiple element to be added.");
    final S set = getSet();

    final VersionVector<K, T> messageVersionVector = set.getVersion().copy();

    for (int i = 0; i < MAX_OPERATIONS; i++) {
      final HashSet<E> elements = new HashSet<>(
          Arrays.asList(getElement(3 * i), getElement(3 * i + 1), getElement(3 * i + 2)));

      messageVersionVector.increment(set.getIdentifier());
      final U message = getAddUpdate(set, set.getIdentifier(), messageVersionVector, elements);

      set.update(message);

      assertTrue("The set should contain the elements which were added", set.containsAll(elements));

      assertUpdate_Add_Multiple(set, message);
    }
  }

  /**
   * Make assertions for the {@linkplain #testUpdate_Add_Multiple()} method. Needs to ensure that
   * the update message is accurate given the element that was added.
   *
   * @param set the {@link Set} instance being tested.
   * @param updateMessage the {@link UpdateMessage} that was applied to the {@link Set} being
   *        tested.
   */
  protected void assertUpdate_Add_Multiple(S set, U updateMessage) {
    // Do nothing by default
  }

}
