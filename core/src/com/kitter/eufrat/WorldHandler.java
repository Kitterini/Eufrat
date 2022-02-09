package com.kitter.eufrat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector3;
import com.kitter.eufrat.Sprites.Animal;
import com.kitter.eufrat.Sprites.Auroch;
import com.kitter.eufrat.Sprites.Tile;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.AnimalDef;
import com.kitter.eufrat.tools.TileAnims;
import com.kitter.eufrat.tools.WorldCreator;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class WorldHandler {
    private static WorldHandler single_instance = null;
    public static long SEED;

    public static Tile[][] worldTiles;
    public static ArrayList<Animal> animalsHighlighted;
    public static ArrayList<Animal> animals;
    public static ArrayList<Thread> animalThreads;
    public static ArrayList<ArrayList<Animal>> tempAnimalPos;
    public static LinkedBlockingQueue<AnimalDef> animalsToSpawn;
    public Sprite backg;

    TileAnims tileAnims;
    AnimalAnims animalAnims;
    public float stateTime;
    public int screenXmin;
    public int screenYmin;
    public int screenXmax;
    public int screenYmax;
    public Random rand;
    public WorldHandler(){
        stateTime = 0f;
        screenXmin = 0;
        screenYmin = 0;
        screenXmax = 0;
        screenYmax = 0;
        rand= new Random(SEED);
        worldTiles = new Tile[GameScreen.MAP_SIZE][GameScreen.MAP_SIZE];
        animalsHighlighted = new ArrayList<Animal>();
        animals = new ArrayList<Animal>();
        animalThreads = new ArrayList<Thread>();
        tempAnimalPos = new ArrayList<ArrayList<Animal>>(GameScreen.MAP_SIZE);

        for(int i=0; i< GameScreen.MAP_SIZE;i++){
            tempAnimalPos.add(new ArrayList<Animal>());
        }
        backg = new Sprite(new Texture("background.png"));
        backg.setSize(Eufrat.PPM * GameScreen.MAP_SIZE * 1.5f, Eufrat.PPM * GameScreen.MAP_SIZE * 1.5f);
        backg.setPosition(Eufrat.PPM * -GameScreen.MAP_SIZE /4, Eufrat.PPM * -GameScreen.MAP_SIZE /4);
        sortAnimals();
        tileAnims = TileAnims.getInstance();
        animalAnims = AnimalAnims.getInstance();
        animalsToSpawn = new LinkedBlockingQueue<AnimalDef>();
        Gdx.app.log("JSON", String.valueOf(AnimalAnims.getInstance().animsMap.keySet()));
    }
    public void initWorld(){
        WorldCreator.create();
    }
    public void draw(SpriteBatch b, float delta){
//        backg.draw(b);
//        this.stateTime += delta;
//        for(int i = GameScreen.MAP_SIZE-1; i>=0;i--){
//            for(int j = GameScreen.MAP_SIZE-1; j>=0; j--){
//                worldTiles[j][i].draw(b, delta);
//            }
//            for (Auroch a : tempAurochPos.get(i)) {
//                a.draw(b, delta);
//                a.update(delta);
//
//            }
//        }


        // check if its super fast
        backg.draw(b);
        this.stateTime += delta;
        // draw based on two xy coords
        for(int i = screenYmax; i>=screenYmin;i--){
            for(int j = screenXmax; j>=screenXmin; j--){
                worldTiles[j][i].draw(b, delta);
            }

            for (Animal a : tempAnimalPos.get(i)) {
                a.draw(b, delta);
                a.update(delta);
            }
        }
        for (Animal a : animalsHighlighted) {
            a.drawHighlighted(b);
        }
    }

    public void sortAnimals(){
        // if any speed issues come up, find less consuming solution
        //Gdx.app.log("ANIMAL", "CHUJ");
        for(int i=0; i< GameScreen.MAP_SIZE;i++){
            tempAnimalPos.get(i).clear();
        }
        for(int i = 0; i< animals.size(); i++){
            if(animals.get(i).getClass()==Auroch.class){
                if(animals.get(i).age+ Eufrat.AUROCH_ADOLECENCE_TIME<WorldHandler.getInstance().stateTime &&
                animals.get(i).sex == Eufrat.Sex.CHILD){
                growUp((Auroch) animals.get(i));
                }
                if(animals.get(i).age+ Eufrat.AUROCH_LIFE_TIME<WorldHandler.getInstance().stateTime){
                    animalDie(i);
                    i++;
                }
            }
            if(i< animals.size()) {
                tempAnimalPos.get((int) (animals.get(i).getY() / Eufrat.PPM)).add((Auroch) animals.get(i));
            }
        }
    }

    public void animalDie(int i){
        GameScreen.getWorld().destroyBody(animals.get(i).b2body);
        animals.remove(i);
        //animalThreads.get(i).interrupt();
        animalThreads.remove(i);
    }

    public void growUp(Auroch a){
        a.growUp(Eufrat.AUROCH_BIT);
    }

    public void setVisibility(Vector3 pos){
        // determine the area of the change
        // this means only width and height has changed
        // parameter needed
        // prev need to be cleaned
            float zoom = GameScreen.getGameCam().zoom;
             if ((pos.x - GameScreen.getGameCam().viewportWidth*zoom/2)  > 0) {
                 screenXmin = (int) ((pos.x - GameScreen.getGameCam().viewportWidth*zoom/2) / Eufrat.PPM-1);
             }
             else{
                 screenXmin = 0;
             }
             if (((pos.x + GameScreen.getGameCam().viewportWidth*zoom/2) / Eufrat.PPM) < GameScreen.MAP_SIZE ) {
                 screenXmax = (int) ((pos.x+ GameScreen.getGameCam().viewportWidth*zoom/2 ) / Eufrat.PPM);
             }
             else{
                 screenXmax = GameScreen.MAP_SIZE-1;
             }
             if ((pos.y - GameScreen.getGameCam().viewportHeight*zoom/2)  > 0) {
                 screenYmin = (int) ((pos.y - GameScreen.getGameCam().viewportHeight*zoom/2) / Eufrat.PPM-1);
             }
             else{
                 screenYmin = 0;
             }
             if ( ((pos.y + GameScreen.getGameCam().viewportHeight*zoom/2) / Eufrat.PPM) < GameScreen.MAP_SIZE) {
                 screenYmax = (int) ((pos.y + GameScreen.getGameCam().viewportHeight*zoom/2) / Eufrat.PPM);
             }
             else{
                 screenYmax = GameScreen.MAP_SIZE-1;
             }
    }


    public boolean withinScreen(float x, float y){
        if(screenXmax* Eufrat.PPM>x &&
            screenXmin* Eufrat.PPM<x &&
            screenYmax* Eufrat.PPM>y &&
            screenYmin* Eufrat.PPM<y){
            return true;
        }
        else{
            return false;
        }
    }

    public void setVisibilityMove(Vector3 pos){
        //camPos = pos;
    }

    public void addAuroch(float x, float y, Eufrat.Sex gender){
        animals.add(new Auroch(x, y, gender));
        animalThreads.add(new Thread(WorldHandler.animals.get(animals.size()-1)));
        animalThreads.get(animals.size()-1).start();
    }

    public void setSeed(int seed){
        SEED = seed;
    }
    public static WorldHandler getInstance() {
        if (single_instance == null) {
            synchronized (WorldHandler.class) {
                if (single_instance == null) {
                    single_instance = new WorldHandler();
                }
            }
        }
        return single_instance;
    }
}
