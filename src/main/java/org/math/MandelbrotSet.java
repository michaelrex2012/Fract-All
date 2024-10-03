package org.math;

public class MandelbrotSet {
    public static boolean checkComplex(ComplexNumber complexToTest, int iterations) {
        ComplexNumber z = new ComplexNumber(0, 0);
        ComplexNumber threshold = new ComplexNumber(2, 2);
        int i = 0;

        while (i < iterations) {
            z = z.square().add(complexToTest);
            if (z.greaterThan(threshold)) {
                System.out.println("\u001B[31m" + "Point escaped in " + i + " iterations" + "\u001B[0m");
                return false;
            }
            i++;
        }
        System.out.println("\u001B[32m" + "Point is within the set after " + iterations + " iterations" + "\u001B[0m");
        return true;
    }

    public static int getIterations(ComplexNumber complexToTest, int maxIterations) {
        ComplexNumber z = new ComplexNumber(0, 0);
        ComplexNumber threshold = new ComplexNumber(2, 2);
        int i = 0;

        while (i < maxIterations) {
            z = z.square().add(complexToTest);
            if (z.greaterThan(threshold)) {
                return i; // Return the iteration count at which the point escaped
            }
            i++;
        }
        return maxIterations; // Return the maximum iterations if the point did not escape
    }
}