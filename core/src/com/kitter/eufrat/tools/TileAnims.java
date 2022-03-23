package com.kitter.eufrat.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class TileAnims {
    public HashMap<String, AnimKey> animsMap = new HashMap<>();
    private static volatile TileAnims single_instance = null;

    private TileAnims()  {

        JSONParser p = new JSONParser();
        JSONObject landTypes = new JSONObject();
        try {
            FileHandle reader = Gdx.files.internal("tiles/types.json");
            landTypes = (JSONObject) p.parse(reader.readString());
        } catch (ParseException e) {
            Gdx.app.log("ERROR", "Tile types json file is wrong");
        }

        Gdx.app.log("JSON", String.valueOf(landTypes.keySet()));
        for (Object t : landTypes.keySet()) {
            ArrayList<String> tileNames;
            tileNames = listFilesForFolder(t.toString());
            //identify number of different sprites
            AnimKey temp = new AnimKey();
            JSONObject sizes = (JSONObject) landTypes.get(t);

            if(sizes.get("depletion")!=null){
                if(sizes.get("depletion").getClass()==JSONArray.class) {

                    JSONArray depletionArray = (JSONArray) sizes.get("depletion");
                    for(String name : tileNames) {
                        for (Object o : depletionArray) {
                            temp.depletionList.add(prepareAdditionalAnim(name.split("\\.")[0], (String) o));
                        }
                    }
                }
            }
            temp.sizeX = Float.parseFloat(String.valueOf(sizes.get("scaleX")));
            temp.sizeY =  Float.parseFloat(String.valueOf(sizes.get("scaleY")));

            for(String name : tileNames) {
                temp.putNew(prepareAnim(name));
            }
            animsMap.put(t.toString(), temp);
        }
    }
    protected TextureRegion prepareTex(String type) {
        return new TextureRegion(new Texture("tiles/" + type ));
    }

    private Animation prepareAnim(String type){
        Texture spritemap = new Texture("tiles/" + type);
        int anim_num = spritemap.getWidth() / spritemap.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(spritemap,
                spritemap.getWidth() / anim_num,
                spritemap.getHeight());
        TextureRegion[] frames = new TextureRegion[spritemap.getWidth() / spritemap.getHeight()];
        if (anim_num >= 0) {
            System.arraycopy(tmp[0], 0, frames, 0, anim_num);
        }
        if (type.equals("coast")) {
            return new Animation<TextureRegion>(0.3f, frames);
        } else {
            return new Animation<TextureRegion>(0.15f, frames);
        }
    }

    private Animation prepareAdditionalAnim(String type,String addition){
        Texture spritemap = new Texture("tiles/" + type + "_" + addition + ".png");
        int anim_num = spritemap.getWidth() / spritemap.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(spritemap,
                spritemap.getWidth() / anim_num,
                spritemap.getHeight());
        TextureRegion[] frames = new TextureRegion[spritemap.getWidth() / spritemap.getHeight()];
        if (anim_num >= 0) {
            System.arraycopy(tmp[0], 0, frames, 0, anim_num);
        }
        if (type.equals("coast")) {
            return new Animation<TextureRegion>(0.3f, frames);
        } else {
            return new Animation<TextureRegion>(0.15f, frames);
        }
    }

    public ArrayList<String> listFilesForFolder(String type) {
        ArrayList<String> tileNames = new ArrayList<>();
        FileHandle folder = Gdx.files.internal("tiles/");

        for (FileHandle fileEntry : folder.list()) {
            if (!fileEntry.isDirectory()) {
                if (fileEntry.name().contains(type) && !fileEntry.name().contains("_")) {
                    tileNames.add(fileEntry.name());
                }
            }
        }
        return tileNames;
    }

    public static TileAnims getInstance() {
        if (single_instance == null) {
            synchronized (TileAnims.class) {
                if (single_instance == null) {
                    single_instance = new TileAnims();
                }
            }
        }
        return single_instance;
    }

}

