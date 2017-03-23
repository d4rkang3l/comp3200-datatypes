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
import java.util.HashSet;
import java.util.Set;
import static uk.ac.soton.ecs.fl4g12.crdt.datatypes.convergent.GSetTest.getGSet;
import uk.ac.soton.ecs.fl4g12.crdt.delivery.StatefulUpdatable;
import uk.ac.soton.ecs.fl4g12.crdt.order.VersionVector;

/**
 * Test of the {@linkplain StatefulUpdatable} based features of the {@linkplain GSet}.
 */
public class GConvergentSetTest extends
    GrowableConvergentSetAbstractTest<Integer, Integer, Integer, GSetState<Integer, Integer, Integer>, GSet<Integer, Integer, Integer>> {

  @Override
  public GSet<Integer, Integer, Integer> getSet() {
    return getGSet();
  }

  @Override
  public Integer getElement(int i) {
    return i;
  }

  @Override
  protected GSetState<Integer, Integer, Integer> getAddUpdate(GSet<Integer, Integer, Integer> set,
      Integer identifier, VersionVector<Integer, Integer> version, Collection<Integer> elements) {
    Set<Integer> state = new HashSet<>(set);
    state.addAll(elements);
    return new GSetState<>(identifier, version, state);
  }

}
