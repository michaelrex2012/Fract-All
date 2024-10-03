package org.math;

/**
 <h4>Overview</h4>
 This class usage is to initialize<br>
 an object, ComplexNumber, for use<br>
 in calculating complex numbers.
 <h4>Usage</h4>
 <b><i>ComplexNumber(real number, imaginary number);</i></b><br>
 ↑ <i>Note: This usage is for the constructor</i> ↑
 <h4>Features</h4>
 <ol>
 <li>Return Real</li>
 <li>Return Imaginary</li>
 <li>Convert Complex to string</li>
 <li>Add Complex</li>
 <li>Square Complex</li>
 </ol>
 */

public class ComplexNumber {
    private final double r, i;

    /** This is the constructor.<br>
     <i>Note: See class documentation for usage!</i>
      */
    public ComplexNumber(double real, double imaginary) {
        this.r = real;
        this.i = imaginary;
    }


    // Accessing functions for getting certain values

    /**
     Returns real part of object ComplexNumber
     */
    public double getReal() {
        return r;
    }

    /**
     Returns imaginary part of object ComplexNumber
     */
    public double getImaginary() {
        return i;
    }

    /**
     Converts a ComplexNumber into a string
     */
    public String toString() {
        return "{" + r + "," + i + "}";
    }

    /**
     Adds to an object ComplexNumber<br>
     by the amount given, complexIn
     */
    public ComplexNumber add(ComplexNumber complexIn) {
        return new ComplexNumber(r + complexIn.r, i + complexIn.i);
    }

    /**

     */
    public ComplexNumber square() {
        return new ComplexNumber(r * r - i * i, 2 * r * i);
    }

    public boolean greaterThan(ComplexNumber compareTo) {
        return r * r + i * i > compareTo.r * compareTo.r + compareTo.i * compareTo.i;
    }
}
