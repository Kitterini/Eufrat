package com.kitter.eufrat.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;

public class AnimalAnims {
    public HashMap<String, AnimKey> animsMap = new HashMap<>();
    private static volatile AnimalAnims single_instance = null;
    String[] behaviours = {"idle", "move", "eating"};
    String path = "animals/";
    private AnimalAnims()  {

        JSONParser p = new JSONParser();
        JSONObject animalTypes = new JSONObject();
        try {
            FileHandle reader = Gdx.files.internal(path + "types.json");
            animalTypes = (JSONObject) p.parse(reader.readString());
        } catch (ParseException e) {
            Gdx.app.log("ERROR", "Animal types json file is wrong");
        }

        Gdx.app.log("JSON", String.valueOf(animalTypes.keySet()));

        for (String behaviour : behaviours) {
            for (Object a : animalTypes.keySet()) {

                //identify number of different sprites
                AnimKey temp = new AnimKey();
                JSONObject sizes = (JSONObject) animalTypes.get(a);
                temp.sizeX = Float.parseFloat(String.valueOf(sizes.get("sizeX")));
                temp.sizeY = Float.parseFloat(String.valueOf(sizes.get("sizeY")));

                temp.putNew(prepareAnim(a.toString() + "_" + behaviour, (int) temp.sizeY));
                animsMap.put(a.toString() + "_" + behaviour, temp);
            }
        }
    }

    protected TextureRegion prepareTex(String type) {
        return new TextureRegion(new Texture(path + type ));
    }

    private Animation prepareAnim(String type, int anim_width){
        Texture spritemap = new Texture(path + type + ".png");

        int anim_num = spritemap.getWidth() / anim_width;

        TextureRegion[][] tmp = TextureRegion.split(spritemap,
                anim_width,
                spritemap.getHeight());
        TextureRegion[] frames = new TextureRegion[anim_num];
        //if (anim_num >= 0) {
            System.arraycopy(tmp[0], 0, frames, 0, anim_num);
        //}
        return new Animation<TextureRegion>(0.15f, frames);
    }



    public static AnimalAnims getInstance() {
        if (single_instance == null) {
            synchronized (AnimalAnims.class) {
                if (single_instance == null) {
                    single_instance = new AnimalAnims();
                }
            }
        }
        return single_instance;
    }
}
