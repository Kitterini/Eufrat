package com.kitter.eufrat.tools;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;

public class TextureUtils {

    private static volatile TextureUtils single_instance = null;
    public HashMap<String, Texture> utilsMap = new HashMap<>();
    String[] names = {"highlight"};
    private TextureUtils() {
        for (String n : names) {
            utilsMap.put(n, new Texture(n + ".png"));
        }
    }
    public static TextureUtils getInstance() {
        if (single_instance == null) {
            synchronized (AnimalAnims.class) {
                if (single_instance == null) {
                    single_instance = new TextureUtils();
                }
            }
        }
        return single_instance;
    }
}
