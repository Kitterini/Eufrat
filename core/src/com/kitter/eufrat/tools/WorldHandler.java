package com.kitter.eufrat.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector3;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.Sprites.Animal;
import com.kitter.eufrat.Sprites.Auroch;
import com.kitter.eufrat.Sprites.Simbakubwa;
import com.kitter.eufrat.Sprites.Tile;
import com.kitter.eufrat.screens.GameScreen;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;


public class WorldHandler {
    private static WorldHandler single_instance = null;
    public static long SEED;

    public static Tile[][] worldTiles;
    public static ArrayList<Tile> TilesEaten;
    public static ArrayList<Animal> animalsHighlighted;
    public static ArrayList<Animal> animals;
    public static ArrayList<Animal> deadAnimals;
    public static ArrayList<ArrayList<Animal>> tempAnimalPos;
    public static LinkedBlockingQueue<AnimalDef> animalsToSpawn;
    public static LinkedBlockingQueue<Animal> animalsToDespawn;
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
        TilesEaten = new ArrayList<Tile>();
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
        animalsToDespawn = new LinkedBlockingQueue<Animal>();
        Gdx.app.log("JSON", String.valueOf(AnimalAnims.getInstance().animsMap.keySet()));
    }
    public void initWorld(){
        WorldCreator.create();
    }
    public void draw(SpriteBatch b, float delta){


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

            }
        }
        for(int i = 0; i< animals.size(); i++){
            animals.get(i).update();
        }
        for(int i = 0; i< deadAnimals.size(); i++){
            deadAnimals.get(i).update();
        }
        for (Animal a : animalsHighlighted) {
            a.drawHighlighted(b);
        }
    }
    public void handleTiles(){//make it run in a separate thread
        for(int i=0; i<TilesEaten.size();i++){
            TilesEaten.get(i).increaseCapacity();
            if(TilesEaten.get(i).capacity>Potamos.TILE_MAX_CAPACITY){
                TilesEaten.remove(i);
                i++;
            }
        }
    }
    public void handleAnimals(){
        // if any speed issues come up, find less consuming solution
        for(int i=0; i< GameScreen.MAP_SIZE;i++){
            tempAnimalPos.get(i).clear();
        }
        for (Animal deadAnimal : deadAnimals) {
            tempAnimalPos.get((int) (deadAnimal.getY() / Potamos.PPM)).add(deadAnimal);
        }
        for(int i = 0; i< animals.size(); i++){
            if(animals.get(i).birthTime + animals.get(i).adolescenceTime < WorldHandler.getInstance().stateTime &&
               animals.get(i).sex == Potamos.Sex.CHILD){
                growUp(animals.get(i));
            }
            if(animals.get(i).birthTime + animals.get(i).lifeTime < WorldHandler.getInstance().stateTime){
                Gdx.app.debug("DEATH","age");
                animals.get(i).die();
                i++;
            }
            if(i < animals.size()) {
                if (animals.get(i).hunger > 100f) {
                    Gdx.app.debug("DEATH", "hunger");
                    animals.get(i).die();
                    i++;
                } else {
                    animals.get(i).hunger += 0.05;
                }
            }
            if(i < animals.size() && animals.get(i).vanquished) {
                Gdx.app.debug("DEATH", "vanquish");
                animals.get(i).die();
                i++;
            }
            if(i < animals.size()) {
                tempAnimalPos.get((int) (animals.get(i).getY() / Potamos.PPM)).add(animals.get(i));
            }
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
            AnimalDef adef = animalsToSpawn.poll();
                WorldHandler.getInstance().addAnimal(adef.type, adef.position.x, adef.position.y, adef.sex, adef.generation);
        }
        if(!animalsToDespawn.isEmpty()){
            Animal a = animalsToDespawn.poll();
            animalRemove(a);
        }
    }

    public void addAnimal(Class<?> type, float x, float y, Potamos.Sex gender, int generation){
        if(type == Auroch.class) {
            animals.add(new Auroch(x, y, gender));
        } else if(type == Simbakubwa.class){
            animals.add(new Simbakubwa(x, y, gender));
        }
        WorldHandler.animals.get(animals.size()-1).generation = generation;
    }

    public static void animalRemove(Animal a){
        deadAnimals.remove(a);
       // tempAnimalPos.get((int) (a.getY() / Potamos.PPM)).remove(a);
        a.dispose();
    }

    public void growUp(Animal a){
        a.growUp(a.b2body.getFixtureList().get(0).getFilterData().categoryBits);
    }

    public void setVisibility(Vector3 pos){
        // determine the area of the change
        // this means only width and height has changed
        // parameter needed
        // prev need to be cleaned
            float zoom = GameScreen.getGameCam().zoom;
             if ((pos.x - GameScreen.getGameCam().viewportWidth*zoom/2) > 0) {
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
