package com.neverEngineLabs.GML2019;

import processing.core.PFont;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.HashMap;
import java.util.Map;

public class Type extends PApplet {

    //font sizes
    public static int  H1=36, P=30, TINY=16, TOKEN=24;
    private Map<Integer, PFont> fonts = new HashMap<>();


    public void init(PGraphics _g) {
        this.g = _g;
        fonts.put(TINY, createFont("data/fonts/Lato-Italic.ttf", TINY, false));
        fonts.put(H1, createFont("data/fonts/Lato-Bold.ttf", H1, true));
        fonts.put(P, createFont("data/fonts/Lato-Regular.ttf", P, true));
        fonts.put(TOKEN, createFont("data/fonts/RobotoSlab-Regular.ttf", TOKEN, true));
    }


    public PFont getFont( int tag ) {
        return  (fonts.get(tag));
    }

    public void setStyleSize (int tag, float size) {
        setStyle(tag);
        textSize(size);
        P = (int) size;
    }
    public void setStyle(int tag) {
        textFont(fonts.get(tag));
    }

    public void setP() {
        setStyle(P);
    }

    public void setH1() {
        setStyle(H1);
    }
    public void setTINY() {
        setStyle(TINY);
    }
    public void setTOKEN() {
        setStyle(TOKEN);
    }
}




