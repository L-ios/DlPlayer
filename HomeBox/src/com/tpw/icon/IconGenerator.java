/*
 * use libicongenerator.so to create big icon.
 */
package com.tpw.icon;

public class IconGenerator {
    static {
        System.loadLibrary("icongenerator");
    }

    public native int generator(int input[], int parameters[], int output[]);
}
