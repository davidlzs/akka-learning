package com.dliu.evictingqueue;

import java.io.Serializable;
import java.util.function.Predicate;

public class EvictingArray<T> implements Serializable {

    private static final long serialVersionUID = 0L;

    private Object[] elements;

    private int index;

    private int capacity;

    private int size;

    public EvictingArray(int capacity) {
        this.elements = new Object[capacity];
        this.capacity = capacity;
        this.size = 0;
        this.index = 0;
    }

    public static EvictingArray create(int capacity) {
        return new EvictingArray(capacity);
    }

    public void add(T element) {
        elements[index++ % capacity] = element;
        if (size < capacity) {
            size++;
        }
    }

    public void clear() {
//        Arrays.fill(elements,null);
        this.size = 0;
        this.index = 0;
    }

    //Get the number of elements that meet the conditions
    public int getQualifiedNums(Predicate<T> predicate) {
        int num = 0;
        for (Object ele : elements) {
            if (predicate.test((T) ele)) {
                num++;
            }
        }
        return num;
    }

    public int getSize() {
        return size;
    }
}