package vsc6;

/*
 *  A quick piece of test code to see what is the effect of 'final'
 *  on arrays. The result is what you should expect if you remember
 *  that arrays are objects. When an array is declared 'final', you
 *  cannot reassign the array, as in the commented-out code in
 *  anotherMethod(). However, you can modify the values stored in
 *  the array, as aMethod() does.
 *
 *  Concepts illustrated by this code:
 *    * the 'final' keyword,
 *    * arrays.
 */

public class FinalTest {
    private final int[] ints;

    FinalTest() {
        ints = new int[10];
        ints[0] = 11;
    }

    void aMethod () {
        ints[0] = 12;
    }

    void anotherMethod () {
        //ints = new int[5];
    }

    public static void main (String[] args) {
        FinalTest ft = new FinalTest();
        ft.aMethod();
        System.out.println(ft.ints[0]);
    }
}
