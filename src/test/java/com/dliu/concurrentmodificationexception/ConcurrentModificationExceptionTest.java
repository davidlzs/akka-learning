package com.dliu.concurrentmodificationexception;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * https://www.javacodegeeks.com/2018/01/deal-concurrentmodificationexception-java-beware-removing-elements-arraylist-loop.html
 */
public class ConcurrentModificationExceptionTest {

    private List<String> listOfBooks;

    @Before
    public void setUp() throws Exception {
        this.listOfBooks = new ArrayList();
        listOfBooks.add("Programming Pearls");
        listOfBooks.add("Design Patterns");
        listOfBooks.add("Effective Java"); // 1st contains Java
        listOfBooks.add("Clean Code");
        listOfBooks.add("Java Design Patterns"); // 2nd contains Java
    }

    @Test(expected = ConcurrentModificationException.class)
    public void singleThread_enhancedForLoop_ListRemove() {
        // setup
        // execute
        for (String b: listOfBooks) { // CME here
            if (b.contains("Java")) {
                listOfBooks.remove(b);
            }
        }
        // assert
    }

    @Test
    public void singleThread_classicForLoop_ListRemoveByIndex() {
        // setup
        // execute
        System.out.println("Before: " + listOfBooks);
        for (int i = 0; i < listOfBooks.size(); i++) {
            String b = listOfBooks.get(i);
            if (b.contains("Java")) {
                listOfBooks.remove(i);
            }
            if (i < listOfBooks.size()  - 1) {
                System.out.println("Next book: " + listOfBooks.get(i + 1));
            }
        }
        System.out.println("After: " + listOfBooks);
        // assert
        assertEquals(3, listOfBooks.size());
    }

    @Test
    public void singleThread_classicForLoop_ListRemove() {
        // setup
        // execute
        System.out.println("Before: " + listOfBooks);
        for (int i = 0; i < listOfBooks.size(); i++) {
            String b = listOfBooks.get(i);
            if (b.contains("Java")) {
                listOfBooks.remove(b);
            }
            if (i < listOfBooks.size()  - 1) {
                System.out.println("Next book: " + listOfBooks.get(i + 1));
            }
        }
        System.out.println("After: " + listOfBooks);
        // assert
        assertEquals(3, listOfBooks.size());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void singleThread_iterator_ListRemove() {
        // setup
        // execute
        System.out.println("Before: " + listOfBooks);
        Iterator<String> iterator = listOfBooks.iterator();
        while(iterator.hasNext()) {
            String b = iterator.next(); // CME here
            if (b.contains("Java")) {
                listOfBooks.remove(b);
            }
        }
        // assert
    }

    /*
        This is preferred method to remove from fast-fail collection. use iterator and use Iterator.remove
     */
    @Test
    public void singleThread_iterator_IteratorRemove() {
        // setup
        // execute
        System.out.println("Before: " + listOfBooks);
        Iterator<String> iterator = listOfBooks.iterator();
        while(iterator.hasNext()) { // use iterator
            String b = iterator.next();
            if (b.contains("Java")) {
                iterator.remove(); // use Iterator.remove
            }
        }
        System.out.println("After: " + listOfBooks);
        // assert
        assertEquals(3, listOfBooks.size());
    }
}
