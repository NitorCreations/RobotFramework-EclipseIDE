/**
 * Copyright 2012-2013 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.internal.util;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A double-ended queue (Deque) with support for priority levels and preservation of order of items with the same
 * priority level. Priority levels can be given either explicitly or determined implicitly by a given
 * {@link Prioritizer} instance.
 */
public class ArrayPriorityDeque<T> extends AbstractQueue<T> implements PriorityDeque<T> {

    private final Deque<T>[] queues;
    private final Prioritizer<T> prioritizer;
    private int mod;

    public ArrayPriorityDeque(int priorityLevels) {
        this(priorityLevels, null);
    }

    public ArrayPriorityDeque(Deque<T>[] queues) {
        this(queues, null);
    }

    @SuppressWarnings("unchecked")
    public ArrayPriorityDeque(int priorityLevels, Prioritizer<T> prioritizer) {
        this(prioritizer, (Deque<T>[]) createDequesArray(priorityLevels));
    }

    public ArrayPriorityDeque(Deque<T>[] queues, Prioritizer<T> prioritizer) {
        this(prioritizer, Arrays.copyOf(queues, queues.length));
    }

    private ArrayPriorityDeque(Prioritizer<T> prioritizer, Deque<T>[] queues) {
        if (queues.length == 0) {
            throw new IllegalArgumentException("Must have a positive amount of priority levels");
        }
        this.queues = queues;
        this.prioritizer = prioritizer;
    }

    private static Deque<?>[] createDequesArray(int priorityLevels) {
        if (priorityLevels < 1) {
            throw new IllegalArgumentException("Must have a positive amount of priority levels");
        }
        Deque<?>[] queues = new Deque[priorityLevels];
        for (int i = 0; i < priorityLevels; ++i) {
            queues[i] = new ArrayDeque<Object>();
        }
        return queues;
    }

    // --------------------------------------

    private int p(T t) {
        if (prioritizer == null) {
            throw new IllegalStateException("Insertion methods without explicit priority argument are not supported because Prioritizer instance not given as a parameter to the constructor");
        }
        return prioritizer.prioritize(t);
    }

    private Deque<T> q(int priority) {
        qc(priority);
        Deque<T> deque = queues[priority];
        return deque;
    }

    private void qc(int priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("Illegal negative priority level");
        }
        if (priority >= queues.length) {
            throw new IllegalArgumentException("Too great priority level - levels 0.." + (queues.length - 1) + " supported by this instance");
        }
    }

    // --------------------------------------

    @Override
    public void addFirst(int priority, T e) {
        Deque<T> deque = q(priority);
        deque.addFirst(e);
        ++mod;
    }

    @Override
    public void addLast(int priority, T e) {
        Deque<T> deque = q(priority);
        deque.addLast(e);
        ++mod;
    }

    @Override
    public boolean add(int priority, T e) {
        if (offerLast(priority, e)) {
            return true;
        }
        throw new IllegalStateException("Queue full for priority " + priority);
    }

    @Override
    public boolean offerFirst(int priority, T e) {
        Deque<T> deque = q(priority);
        boolean success = deque.offerFirst(e);
        if (success) {
            ++mod;
        }
        return success;
    }

    @Override
    public boolean offerLast(int priority, T e) {
        Deque<T> deque = q(priority);
        boolean success = deque.offerLast(e);
        if (success) {
            ++mod;
        }
        return success;
    }

    @Override
    public boolean offer(int priority, T e) {
        return offerLast(priority, e);
    }

    @Override
    public void push(int priority, T e) {
        addFirst(priority, e);
    }

    @Override
    public void clear(int priority) {
        Deque<T> deque = q(priority);
        deque.clear();
        ++mod;
    }

    @Override
    public void clear(int minPriority, int maxPriority) {
        qc(minPriority);
        qc(maxPriority);
        for (int i = minPriority; i <= maxPriority; ++i) {
            queues[i].clear();
        }
        ++mod;
    }

    @Override
    public int getNumberOfPriorityLevels() {
        return queues.length;
    }

    @Override
    public int peekLowestPriority() {
        for (int i = 0; i < queues.length; ++i) {
            Deque<T> queue = queues[i];
            if (!queue.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int peekHighestPriority() {
        for (int i = queues.length - 1; i >= 0; --i) {
            Deque<T> queue = queues[i];
            if (!queue.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    // --------------------------------------

    @Override
    public void addFirst(T e) {
        addFirst(p(e), e);
    }

    @Override
    public void addLast(T e) {
        addLast(p(e), e);
    }

    @Override
    public boolean add(T e) {
        return add(p(e), e);
    }

    @Override
    public boolean offerFirst(T e) {
        return offerFirst(p(e), e);
    }

    @Override
    public boolean offerLast(T e) {
        return offerLast(p(e), e);
    }

    @Override
    public boolean offer(T e) {
        return offerLast(p(e), e);
    }

    @Override
    public T poll() {
        return pollFirst();
    }

    @Override
    public T peek() {
        return peekFirst();
    }

    @Override
    public void clear() {
        for (Deque<T> queue : queues) {
            queue.clear();
        }
        ++mod;
    }

    @Override
    public int size() {
        int size = 0;
        for (Deque<T> queue : queues) {
            size += queue.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (Deque<T> queue : queues) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public T pollFirst() {
        for (Deque<T> queue : queues) {
            T t = queue.pollFirst();
            if (t != null) {
                ++mod;
                return t;
            }
        }
        return null;
    }

    @Override
    public T pollLast() {
        for (int i = queues.length - 1; i >= 0; --i) {
            Deque<T> queue = queues[i];
            T t = queue.pollLast();
            if (t != null) {
                ++mod;
                return t;
            }
        }
        return null;
    }

    @Override
    public T peekFirst() {
        for (Deque<T> queue : queues) {
            T t = queue.peekFirst();
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    @Override
    public T peekLast() {
        for (int i = queues.length - 1; i >= 0; --i) {
            Deque<T> queue = queues[i];
            T t = queue.peekLast();
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    @Override
    public T removeFirst() {
        T t = pollFirst();
        if (t == null) {
            throw new NoSuchElementException();
        }
        return t;
    }

    @Override
    public T removeLast() {
        T t = pollLast();
        if (t == null) {
            throw new NoSuchElementException();
        }
        return t;
    }

    @Override
    public T getFirst() {
        T t = peekFirst();
        if (t == null) {
            throw new NoSuchElementException();
        }
        return t;
    }

    @Override
    public T getLast() {
        T t = peekLast();
        if (t == null) {
            throw new NoSuchElementException();
        }
        return t;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        for (Deque<T> queue : queues) {
            if (queue.removeFirstOccurrence(o)) {
                ++mod;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        for (int i = queues.length - 1; i >= 0; --i) {
            Deque<T> queue = queues[i];
            if (queue.removeLastOccurrence(o)) {
                ++mod;
                return true;
            }
        }
        return false;
    }

    @Override
    public void push(T e) {
        addFirst(e);
    }

    @Override
    public T pop() {
        return removeFirst();
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr() {
            {
                queue = -1;
            }

            @Override
            Iterator<T> nextQueueIterator() {
                ++queue;
                if (queue >= queues.length) {
                    return null;
                }
                return queues[queue].iterator();
            }
        };
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new Itr() {
            {
                queue = queues.length;
            }

            @Override
            Iterator<T> nextQueueIterator() {
                --queue;
                if (queue < 0) {
                    return null;
                }
                return queues[queue].descendingIterator();
            }
        };
    }

    private abstract class Itr implements Iterator<T> {
        private int myMod = mod;
        protected int queue;
        private Iterator<T> listIt;
        private Iterator<T> lastSuccessfulListIt;

        Itr() {}

        private void modCheck() {
            if (myMod != mod) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            modCheck();
            while (listIt == null || !listIt.hasNext()) {
                listIt = nextQueueIterator();
                if (listIt == null) {
                    return false;
                }
            }
            return true;
        }

        abstract Iterator<T> nextQueueIterator();

        @Override
        public T next() {
            modCheck();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T next = listIt.next();
            lastSuccessfulListIt = listIt;
            return next;
        }

        @Override
        public void remove() {
            if (lastSuccessfulListIt == null) {
                throw new IllegalStateException();
            }
            modCheck();
            lastSuccessfulListIt.remove();
            myMod = ++mod;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < queues.length; i++) {
            Deque<T> queue = queues[i];
            if (!queue.isEmpty()) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append("priority_").append(i).append('=').append(queue.toString());
            }
        }
        return sb.append(']').toString();
    }

}
