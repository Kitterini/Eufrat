package com.kitter.eufrat.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.LangPack;
import com.kitter.eufrat.tools.MouseInputProcessor;
import com.kitter.eufrat.tools.WorldHandler;

public class Simbakubwa extends Animal{
    public Animation<TextureRegion> swim;
    public Animation<TextureRegion> sleep;
    public Simbakubwa(float x, float y, Potamos.Sex gender) {
        super(x,y,gender);
        lifeTime = Potamos.SimbaVal.get("LIFE_TIME");
        adolescenceTime = Potamos.SimbaVal.get("ADOLESCENCE_TIME");
        mealSize = Potamos.SimbaVal.get("MEAL_SIZE");
        feedRange = Potamos.SimbaVal.get("FEED_RANGE");
        courtRange = Potamos.SimbaVal.get("COURT_RANGE");


        // percentages for randomized states
        IDLE_PERCENTAGE = 10;
        WALKING_PERCENTAGE = 35;
        SPEAK_PERCENTAGE = 10;
        COURT_PERCENTAGE = 125;
        FEED_PERCENTAGE = 20;
        RUN_PERCENTAGE = 0;

        if(sex == Potamos.Sex.FEMALE) {
            idle = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_dead").animList.get(0);
            swim = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_swim").animList.get(0);
            sleep = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_sleep").animList.get(0);
            birthTime = WorldHandler.getInstance().stateTime - adolescenceTime - rand.nextInt(lifeTime/10*8);
        }
        else if(sex == Potamos.Sex.MALE){
            idle = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_dead").animList.get(0);
            swim = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_swim").animList.get(0);
            sleep = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_sleep").animList.get(0);
            birthTime = WorldHandler.getInstance().stateTime - adolescenceTime - rand.nextInt(lifeTime/10*8);
        }
        else if(sex == Potamos.Sex.CHILD){
            idle = AnimalAnims.getInstance().animsMap.get("pupper_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("pupper_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("pupper_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("pupper_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("pupper_dead").animList.get(0);
            swim = AnimalAnims.getInstance().animsMap.get("pupper_swim").animList.get(0);
            sleep = AnimalAnims.getInstance().animsMap.get("pupper_sleep").animList.get(0);
            birthTime = WorldHandler.getInstance().stateTime;
        }
        speakFile = "audio/sounds/miau.mp3";
        speakFile2 = "audio/sounds/miau2.mp3";
        setBounds(x,y,idle.getKeyFrame(0).getRegionWidth(),idle.getKeyFrame(0).getRegionHeight());
        reg = idle.getKeyFrame(0);

        define(Potamos.SIMBA_BIT);
        name = LangPack.data.get("ANIMAL_SIMBA").toString();
        mouse = new MouseInputProcessor() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 vec=new Vector3(screenX,screenY,0);
                GameScreen.getGameCam().unproject(vec);
                if(getX() < vec.x &&
                        getY() < vec.y &&
                        getX() + getWidth() > vec.x &&
                        getY() + getHeight() > vec.y &&
                        button == Input.Buttons.LEFT){
                    highlighted = !highlighted;
                    if(highlighted){
                        WorldHandler.animalsHighlighted.add(Simbakubwa.this);
                    }
                    else{
                        menu = false;
                        WorldHandler.animalsHighlighted.remove(Simbakubwa.this);
                    }
                }
                if(getX() < vec.x &&
                        getY() < vec.y &&
                        getX() + getWidth() > vec.x &&
                        getY() + getHeight() > vec.y &&
                        button == Input.Buttons.RIGHT &&
                        highlighted){
                    menu = !menu;
                }
                return false;
            }
        };
        GameScreen.getImultiplexer().addProcessor(mouse);
        Gdx.input.setInputProcessor(GameScreen.getImultiplexer());
    }

    @Override
    protected TextureRegion getFrame(float dt){
        switch(currentState) {
            case SLEEP:
                reg = sleep.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case IDLE:
            case PREGNANT:
                if(WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "waters" ||
                   WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "coast"){
                    reg = swim.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                }
                else {
                    reg = idle.getKeyFrame(dt);
                }
                break;
            case SEEKING_FOOD:
            case WALKING:
            case COURT:
            case RUN:
                if(WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "waters"||
                   WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "coast"){
                    reg = swim.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                }
                else {
                    reg = walking.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                }
                break;
            case SPEAK:
                if(WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "waters"||
                   WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "coast"){
                    reg = swim.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                }
                else {
                    reg = voice.getKeyFrame(dt);
                }
                break;
            case EATING:
                reg = eating.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case DEAD:
                if(WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "waters"||
                   WorldHandler.worldTiles[(int)getCurrentTilePos().x][(int)getCurrentTilePos().y].type == "coast"){
                    deathTime = -100;
                }
                reg = dead.getKeyFrame(dt);
                break;
        }
        return reg;
    }

    @Override
    protected void updateAnims(){
        if (rand.nextInt(2) == 1) {
            idle = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("simbakubwa_female_dead").animList.get(0);
            sex = Potamos.Sex.FEMALE;
        } else {
            idle = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("simbakubwa_male_dead").animList.get(0);
            sex = Potamos.Sex.MALE;
        }
    }

    @Override
    protected short defineCollision(short ANIMAL_BIT) {
        return (short) (Potamos.ANIMALS_BIT | Potamos.TILE_BIT | Potamos.DEAD_AUROCH_BIT);
    }

    @Override
    protected void scanForFood() {
        checkFood((int) getCurrentTilePos().x,(int) getCurrentTilePos().y);
    }

    @Override
    public boolean checkFood(int posX, int posY){
        int fluc = -1;
        for(int i = posY; i< posY + feedRange; ){
            if(i< GameScreen.MAP_SIZE && i> 0) {
                for (Animal animal : WorldHandler.tempAnimalPos.get(i)) {
                    if(animal != null && animal.b2body != null && animal.getClass() == Auroch.class) {
                        if (calcDistance(b2body.getPosition().x, b2body.getPosition().y, animal.b2body.getPosition().x, animal.b2body.getPosition().y) < Potamos.PPM * feedRange) {
                                foodPos = new Vector2(animal.b2body.getPosition().x, animal.b2body.getPosition().y);
                                foodFound = true;
                                return true;
                        }
                    }
                }
            }
            i = posY + fluc;
            if(fluc>0){
                fluc = -(fluc+1);
            }
            else{
                fluc = -fluc;
            }
        }
        return false;
    }

    @Override
     protected void findState() {
        super.findState();
        if(hunger<0 && currentState!= Potamos.State.EATING){
            currentState = Potamos.State.SLEEP;
        }
    }
    @Override
    protected boolean eligibleForFood() {
        return timeToEat;
    }

    @Override
    protected void eat() {
        mealSize = Potamos.SimbaVal.get("MEAL_SIZE");
    }

    @Override
    public boolean enemyClose() {
        return false;
    }

    @Override
    public void die() {
        super.die();
        Filter filter = new Filter();
        filter.categoryBits = Potamos.DEAD_SIMBA_BIT;
        filter.maskBits = Potamos.NOTHING_BIT;
        for (Fixture fixture : b2body.getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }
    public void dispose () {
        super.dispose();
    }
}
