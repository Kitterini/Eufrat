package com.kitter.eufrat.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector3;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.Sprites.Animal;
import com.kitter.eufrat.Sprites.Auroch;
import com.kitter.eufrat.Sprites.Tile;
import com.kitter.eufrat.screens.GameScreen;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class WorldHandler {
    private static WorldHandler single_instance = null;
    public static long SEED;

    public static Tile[][] worldTiles;
    public static ArrayList<Animal> animalsHighlighted;
    public static ArrayList<Animal> animals;
    public static ArrayList<Animal> deadAnimals;
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
        rand = new Random(SEED);
        worldTiles = new Tile[GameScreen.MAP_SIZE][GameScreen.MAP_SIZE];
        animalsHighlighted = new ArrayList<Animal>();
        animals = new ArrayList<Animal>();
        deadAnimals = new ArrayList<Animal>();
        tempAnimalPos = new ArrayList<ArrayList<Animal>>(GameScreen.MAP_SIZE);

        for(int i=0; i< GameScreen.MAP_SIZE;i++){
            tempAnimalPos.add(new ArrayList<Animal>());
        }
        backg = new Sprite(new Texture("background.png"));
        backg.setSize(Potamos.PPM * GameScreen.MAP_SIZE * 1.5f, Potamos.PPM * GameScreen.MAP_SIZE * 1.5f);
        backg.setPosition(Potamos.PPM * -GameScreen.MAP_SIZE /4, Potamos.PPM * -GameScreen.MAP_SIZE /4);
        handleAnimals();
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

    public void handleAnimals(){
        // if any speed issues come up, find less consuming solution
        for(int i=0; i< GameScreen.MAP_SIZE;i++){
            tempAnimalPos.get(i).clear();
        }
        for(int i = 0; i< animals.size(); i++){
            // auroch section
            if(animals.get(i).getClass() == Auroch.class){
                if(animals.get(i).age+ Potamos.AUROCH_ADOLECENCE_TIME<WorldHandler.getInstance().stateTime &&
                   animals.get(i).sex == Potamos.Sex.CHILD){
                    growUp((Auroch) animals.get(i));
                }
//                if(animals.get(i).age + Potamos.AUROCH_LIFE_TIME < WorldHandler.getInstance().stateTime){
//                    Gdx.app.log("DEATH","AGE");
//                    animals.get(i).die();
//                    i++;
//                }
            }
            if(i < animals.size()) {
                    if (animals.get(i).hunger > 100f) {
                        Gdx.app.log("DEATH", "HUNGER");
                        animals.get(i).die();
                        i++;
                    } else {
                        animals.get(i).hunger += 0.05;
                    }
            }
            if(i < animals.size()) {
                tempAnimalPos.get((int) (animals.get(i).getY() / Potamos.PPM)).add(animals.get(i));
            }
        }
        for (Animal deadAnimal : deadAnimals) {
            tempAnimalPos.get((int) (deadAnimal.getY() / Potamos.PPM)).add(deadAnimal);
        }
    }
    // for speedup purposes
    public void adjustAnimalSpeed(float speed){
        for(Animal animal : animals){
            animal.b2body.setLinearVelocity(animal.b2body.getLinearVelocity().x * speed,
                                            animal.b2body.getLinearVelocity().y * speed);
        }
    }

    public static void handleSpawningAnimals(){
        if(!animalsToSpawn.isEmpty()){
            Gdx.app.log("Spawn", "spawning");
            AnimalDef adef = animalsToSpawn.poll();
            if(adef.type == Auroch.class){
                WorldHandler.getInstance().addAuroch(adef.position.x, adef.position.y, adef.sex, adef.generation);
            }
        }
    }

    public static void animalRemove(Animal a){
        deadAnimals.remove(a);
    }

    public void growUp(Auroch a){
        a.growUp(Potamos.AUROCH_BIT);
    }

    public void setVisibility(Vector3 pos){
        // determine the area of the change
        // this means only width and height has changed
        // parameter needed
        // prev need to be cleaned
            float zoom = GameScreen.getGameCam().zoom;
             if ((pos.x - GameScreen.getGameCam().viewportWidth*zoom/2)  > 0) {
                 screenXmin = (int) ((pos.x - GameScreen.getGameCam().viewportWidth*zoom/2) / Potamos.PPM-1);
             }
             else{
                 screenXmin = 0;
             }
             if (((pos.x + GameScreen.getGameCam().viewportWidth*zoom/2) / Potamos.PPM) < GameScreen.MAP_SIZE ) {
                 screenXmax = (int) ((pos.x+ GameScreen.getGameCam().viewportWidth*zoom/2 ) / Potamos.PPM);
             }
             else{
                 screenXmax = GameScreen.MAP_SIZE-1;
             }
             if ((pos.y - GameScreen.getGameCam().viewportHeight*zoom/2)  > 0) {
                 screenYmin = (int) ((pos.y - GameScreen.getGameCam().viewportHeight*zoom/2) / Potamos.PPM-1);
             }
             else{
                 screenYmin = 0;
             }
             if ( ((pos.y + GameScreen.getGameCam().viewportHeight*zoom/2) / Potamos.PPM) < GameScreen.MAP_SIZE) {
                 screenYmax = (int) ((pos.y + GameScreen.getGameCam().viewportHeight*zoom/2) / Potamos.PPM);
             }
             else{
                 screenYmax = GameScreen.MAP_SIZE-1;
             }
    }


    public boolean withinScreen(float x, float y){
        if(screenXmax* Potamos.PPM>x &&
            screenXmin* Potamos.PPM<x &&
            screenYmax* Potamos.PPM>y &&
            screenYmin* Potamos.PPM<y){
            return true;
        }
        else{
            return false;
        }
    }

    public void addAuroch(float x, float y, Potamos.Sex gender, int generation){
        animals.add(new Auroch(x, y, gender));
        WorldHandler.animals.get(animals.size()-1).generation = generation;
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
