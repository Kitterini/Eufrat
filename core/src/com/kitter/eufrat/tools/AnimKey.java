package com.kitter.eufrat.tools;

import com.badlogic.gdx.graphics.g2d.Animation;

import java.util.ArrayList;

public class AnimKey {
    public float sizeX;
    public float sizeY;
    public ArrayList<Animation> animList;
    public ArrayList<Animation> depletionList;

    public AnimKey() {
        animList = new ArrayList<>();
        depletionList = new ArrayList<>();
        sizeX = 1;
        sizeY = 1;
    }

    public void putNew(Animation anim) {
        animList.add(anim);
    }
}
