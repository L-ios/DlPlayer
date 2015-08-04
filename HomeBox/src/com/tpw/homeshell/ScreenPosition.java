package com.tpw.homeshell;

public class ScreenPosition {
    public int s;    //screen index
    public int x;   //x index
    public int y;   //y index

    public ScreenPosition(int s, int x, int y) {
        this.s = s;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "ScreenPosition [s=" + s + ", x=" + x + ", y=" + y + "]";
    }
    
}
