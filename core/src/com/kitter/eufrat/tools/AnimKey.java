package com.kitter.eufrat.tools;

import com.badlogic.gdx.graphics.g2d.Animation;

import java.util.ArrayList;

public class AnimKey {
    public float sizeX;
    public float sizeY;
     // there may be multiple textures for one land type e.g. meadow1, meadow2 and some like water are anims
    // so the choice has been made that all are anims, super creepy performance wise
     public ArrayList<Animation> animList;
     // when consumed it will be changed for texture from depletion list, multiple textures
     // for multiple land textures are being put one after another,
     // e. g. empty, half, so we can have meadow1_empty, meadow1_half, meadow2_empty, meadow2_half
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
