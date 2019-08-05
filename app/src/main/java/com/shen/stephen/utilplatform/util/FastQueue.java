package com.shen.stephen.utilplatform.util;

/**
 * class FastQueue implements first-in first-out algorithm
 */

public final class FastQueue <T>implements java.io.Serializable {

    private static final long serialVersionUID = -6446123127026893803L;

    static final int DEFAULT_CAPACITY = 10;

    static final int DEFAULT_FREE_SPACE_GROW_FACTOR = 10; // %
    // free < capacity * m_grow_factor / 100 >> grow
    int m_grow_factor = DEFAULT_FREE_SPACE_GROW_FACTOR;

    Object[] m_data;
    int m_last = 0;
    int m_first = 0;

    public FastQueue() {
        this(DEFAULT_CAPACITY, DEFAULT_FREE_SPACE_GROW_FACTOR);
    }

    public FastQueue(int capacity, int grow_factor) {
        if (capacity <= 0) {
            capacity = DEFAULT_CAPACITY;
        }
        if (grow_factor == 0) {
            grow_factor = DEFAULT_FREE_SPACE_GROW_FACTOR;
        }

        m_grow_factor = grow_factor;
        m_data = new Object[capacity];
    }

    public void clear() {
        m_last = m_first = 0;
        m_data = new Object[m_data.length];
    }

    @Override
    public Object clone() {
        try {
            FastQueue v = (FastQueue) super.clone();
            v.m_data = m_data.clone();
            // v.m_data = new Object[m_data.length];
            // System.arraycopy(m_data, 0, v.m_data, 0, m_data.length);
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            e.printStackTrace();
            throw new InternalError();
        }
    }

    public boolean empty() {
        return m_last == m_first;
    }

    void grow() {
        Object[] old_data = m_data;
        Object[] new_data = m_data;

        if (m_data.length - m_last <= m_data.length * m_grow_factor / 100) {
            new_data = new Object[m_data.length * 2];
        }

        if (m_last > m_first) {
            System.arraycopy(old_data, m_first, new_data, 0, m_last - m_first);
        }
        m_data = new_data;
        m_last -= m_first;
        m_first = 0;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        return (T) m_data[m_first];
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (m_first >= m_last) {
            return null;
        }

        T ret = (T) m_data[m_first];

        // release the data.
        m_data[m_first] = null;
        if (++m_first == m_last) {

            // if the queue is empty reset the index to the start of the array.
            m_first = m_last = 0;
        }

        return ret;
    }

    public void push(T obj) {
        if (m_last == m_data.length) {
            grow();
        }

        m_data[m_last++] = obj;
    }

    public int size() {
        return m_last - m_first;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        int maxIndex = m_last - 1;
        for (int i = m_first; i <= maxIndex; i++) {
            buf.append(String.valueOf(m_data[i]));
            if (i < maxIndex) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }

}
