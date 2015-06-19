/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.value.nearby;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.AbstractValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;

public class NearEntityNearbyValueSelector extends AbstractValueSelector {

    protected final ValueSelector childValueSelector;
    protected final EntitySelector originEntitySelector;
    protected final NearbyDistanceMeter nearbyDistanceMeter;
    protected final NearbyRandom nearbyRandom;
    protected final boolean randomSelection;
    protected final boolean discardNearbyIndexZero = true;// TODO deactivate me when appropriate

    protected Map<Object, Object[]> originToDestinationsMap = null;

    public NearEntityNearbyValueSelector(ValueSelector childValueSelector, EntitySelector originEntitySelector,
            NearbyDistanceMeter nearbyDistanceMeter, NearbyRandom nearbyRandom, boolean randomSelection) {
        this.childValueSelector = childValueSelector;
        this.originEntitySelector = originEntitySelector;
        this.nearbyDistanceMeter = nearbyDistanceMeter;
        this.nearbyRandom = nearbyRandom;
        this.randomSelection = randomSelection;
        if (randomSelection && nearbyRandom == null) {
            throw new IllegalArgumentException("The valueSelector (" + this
                    + ") with randomSelection (" + randomSelection + ") has no nearbyRandom (" + nearbyRandom + ").");
        }
        // TODO Remove this limitation and unignore test NearEntityNearbyValueSelectorTest.originalSelection()
        if (!childValueSelector.getVariableDescriptor().getVariablePropertyType().isAssignableFrom(
                originEntitySelector.getEntityDescriptor().getEntityClass())) {
            throw new IllegalArgumentException("The valueSelector (" + this
                    + ") has a variable property type ("
                    +  childValueSelector.getVariableDescriptor().getVariablePropertyType()
                    + ") which is not a superclass of the originEntitySelector's entityClass ("
                    + originEntitySelector.getEntityDescriptor().getEntityClass() + ").");
        }
        phaseLifecycleSupport.addEventListener(childValueSelector);
        phaseLifecycleSupport.addEventListener(originEntitySelector);
    }

    @Override
    public GenuineVariableDescriptor getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    @Override
    public void phaseStarted(AbstractPhaseScope phaseScope) {
        // Cannot be done during solverStarted because
        super.phaseStarted(phaseScope);
        long originSize = originEntitySelector.getSize();
        if (originSize > (long) Integer.MAX_VALUE) {
            throw new IllegalStateException("The originEntitySelector (" + originEntitySelector
                    + ") has an entitySize (" + originSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        originToDestinationsMap = new HashMap<Object, Object[]>((int) originSize);
        for (Iterator originIt = originEntitySelector.endingIterator(); originIt.hasNext(); ) {
            final Object origin =  originIt.next();
            long childSize = childValueSelector.getSize(origin);
            if (childSize > (long) Integer.MAX_VALUE) {
                throw new IllegalStateException("The childEntitySelector (" + childValueSelector
                        + ") has an entitySize (" + childSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            Object[] destinations = new Object[(int) childSize];
            int i = 0;
            for (Iterator childIt = childValueSelector.endingIterator(origin); childIt.hasNext(); i++) {
                destinations[i] = childIt.next();
            }
            Arrays.sort(destinations, new Comparator<Object>() {
                @Override
                public int compare(Object a, Object b) {
                    double aDistance = nearbyDistanceMeter.getNearbyDistance(origin, a);
                    double bDistance = nearbyDistanceMeter.getNearbyDistance(origin, b);
                    if (aDistance < bDistance) {
                        return -1;
                    } else if (aDistance > bDistance) {
                        return 1;
                    } else {
                        return 0; // Keep endingIterator order
                    }
                }
            });
            originToDestinationsMap.put(origin, destinations);
        }
    }

    @Override
    public void phaseEnded(AbstractPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        originToDestinationsMap = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isCountable() {
        return childValueSelector.isCountable();
    }

    public boolean isNeverEnding() {
        return randomSelection || !isCountable();
    }

    public long getSize(Object entity) {
        return childValueSelector.getSize(entity) - (discardNearbyIndexZero ? 1 : 0);
    }

    public Iterator<Object> iterator(Object entity) {
        if (!randomSelection) {
            return new OriginalEntityNearbyValueIterator(
                    originEntitySelector.iterator(), childValueSelector.getSize(entity));
        } else {
            return new RandomEntityNearbyValueIterator(
                    originEntitySelector.iterator(), childValueSelector.getSize(entity));
        }
    }

    public Iterator<Object> endingIterator(Object entity) {
        // TODO It should probably use nearby order
        // It must include the origin entity too
        return childValueSelector.endingIterator(entity);
    }

    private class OriginalEntityNearbyValueIterator extends SelectionIterator<Object> {

        private final Iterator<Object> originEntityIterator;
        private final long childSize;

        private boolean originSelected = false;
        private boolean originIsNotEmpty;
        private Object origin;

        private int nextNearbyIndex;

        public OriginalEntityNearbyValueIterator(Iterator<Object> originEntityIterator, long childSize) {
            this.originEntityIterator = originEntityIterator;
            this.childSize = childSize;
            nextNearbyIndex = discardNearbyIndexZero ? 1 : 0;
        }

        private void selectOrigin() {
            if (originSelected) {
                return;
            }
            originIsNotEmpty = originEntityIterator.hasNext();
            origin = originEntityIterator.next();
            originSelected = true;
        }

        @Override
        public boolean hasNext() {
            selectOrigin();
            return originIsNotEmpty && nextNearbyIndex < childSize;
        }

        @Override
        public Object next() {
            selectOrigin();
            Object[] destinations = originToDestinationsMap.get(origin);
            Object next = destinations[nextNearbyIndex];
            nextNearbyIndex++;
            return next;
        }

    }

    private class RandomEntityNearbyValueIterator extends SelectionIterator<Object> {

        private final Iterator<Object> originEntityIterator;
        private final int nearbySize;

        public RandomEntityNearbyValueIterator(Iterator<Object> originEntityIterator, long childSize) {
            this.originEntityIterator = originEntityIterator;
            if (childSize > (long) Integer.MAX_VALUE) {
                throw new IllegalStateException("The valueSelector (" + this
                        + ") has an entitySize (" + childSize
                        + ") which is higher than Integer.MAX_VALUE.");
            }
            nearbySize = (int) childSize - (discardNearbyIndexZero ? 1 : 0);
        }

        @Override
        public boolean hasNext() {
            return originEntityIterator.hasNext() && nearbySize > 0;
        }

        @Override
        public Object next() {
            Object origin = originEntityIterator.next();
            int nearbyIndex = nearbyRandom.nextInt(workingRandom, nearbySize);
            if (discardNearbyIndexZero) {
                nearbyIndex++;
            }
            Object[] destinations = originToDestinationsMap.get(origin);
            return destinations[nearbyIndex];
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + originEntitySelector + ", " + childValueSelector + ")";
    }

}
