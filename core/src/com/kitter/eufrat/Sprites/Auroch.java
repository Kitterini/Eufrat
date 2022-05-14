package com.kitter.eufrat.Sprites;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.tools.LangPack;
import com.kitter.eufrat.tools.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.MouseInputProcessor;


public class Auroch extends Animal {

    public Auroch(float x, float y, Potamos.Sex gender) {
        super(x,y,gender);

        lifeTime = Potamos.AurochVal.get("LIFE_TIME");
        adolescenceTime = Potamos.AurochVal.get("ADOLESCENCE_TIME");
        mealSize = Potamos.AurochVal.get("MEAL_SIZE");
        feedRange = Potamos.AurochVal.get("FEED_RANGE");
        courtRange = Potamos.AurochVal.get("COURT_RANGE");
        runRange = Potamos.AurochVal.get("RUN_RANGE");

        // percentages for randomized states
        IDLE_PERCENTAGE = 10;
        WALKING_PERCENTAGE = 15;
        SPEAK_PERCENTAGE = 10;
        COURT_PERCENTAGE = 20;
        FEED_PERCENTAGE = 20;
        RUN_PERCENTAGE = 25;

        if(sex == Potamos.Sex.FEMALE) {
            idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_female_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("auroch_female_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("auroch_female_dead").animList.get(0);
            birthTime = WorldHandler.getInstance().stateTime - adolescenceTime - rand.nextInt(lifeTime /10*8);
        }
        else if(sex == Potamos.Sex.MALE){
            idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_male_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("auroch_male_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("auroch_male_dead").animList.get(0);
            birthTime = WorldHandler.getInstance().stateTime - adolescenceTime - rand.nextInt(lifeTime /10*8);
        }
        else if(sex == Potamos.Sex.CHILD){
            idle = AnimalAnims.getInstance().animsMap.get("calf_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("calf_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("calf_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("calf_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("calf_dead").animList.get(0);
            birthTime = WorldHandler.getInstance().stateTime;
        }
        mealSize = Potamos.AUROCH_MEAL_SIZE;
        speakFile = "audio/sounds/mooo.mp3";
        speakFile2 = "audio/sounds/mooo2.mp3";
        setBounds(x,y,idle.getKeyFrame(0).getRegionWidth(),idle.getKeyFrame(0).getRegionHeight());
        reg = idle.getKeyFrame(0);

        define(Potamos.AUROCH_BIT);
        name = LangPack.data.get("ANIMAL_AUROCH").toString();
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
                        WorldHandler.animalsHighlighted.add(Auroch.this);
                    }
                    else{
                        menu = false;
                        WorldHandler.animalsHighlighted.remove(Auroch.this);
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
    protected TextureRegion getFrame(float dt) {
        switch(currentState) {
            case IDLE:
            case PREGNANT:
                reg = idle.getKeyFrame(dt);
                break;
            case SEEKING_FOOD:
            case WALKING:
            case COURT:
            case RUN:
                reg = walking.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case SPEAK:
                reg = voice.getKeyFrame(dt);
                break;
            case EATING:
                reg = eating.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case DEAD:
                reg = dead.getKeyFrame(dt);
                break;
        }
        return reg;
    }

    @Override
    protected void scanForFood() {
        // approach when scanning for tiles
        Vector2 currentPos = getCurrentTilePos();
        int posX = (int)currentPos.x;
        int posY = (int)currentPos.y;
        boolean moveX = true;
        int moveMax = 0;
        int direction;
        if(rand.nextInt(2)==0){
            direction = 1;
        }
        else{
            direction = -1;
        }

        while(posX < (currentPos.x + feedRange)) {
            if(moveX) {
                for(int i=0;i<moveMax;i++){
                    if(checkFood(posX,posY)){
                        return;
                    }
                    posX+=direction;
                }
            }
            else{
                for(int i=0;i<moveMax;i++){
                    if(checkFood(posX,posY)){
                        return;
                    }
                    posY+=direction;
                }
                moveMax+=1;
                direction=-direction;
            }
            moveX = !moveX;
        }
        Gdx.app.log("FOOD", "NO FOOD FOUND");
    }

    @Override
    protected boolean eligibleForFood() {
       timeToEat = passed(foodPos,5);
       return timeToEat;
    }

    @Override
    protected void eat() {
        mealSize = WorldHandler.worldTiles[(int)(foodPos.x/Potamos.PPM)][(int)(foodPos.y/Potamos.PPM)].decreaseCapacity(Potamos.AurochVal.get("MEAL_SIZE"));
        WorldHandler.TilesEaten.add(WorldHandler.worldTiles[(int)(foodPos.x/Potamos.PPM)][(int)(foodPos.y/Potamos.PPM)]);
    }

    @Override
    public boolean enemyClose() {
        int fluc = -1;
        int posY = (int) getCurrentTilePos().y;
        for(int i = posY; i< posY + runRange; ){
            if(i< GameScreen.MAP_SIZE && i> 0) {
                for (Animal animal : WorldHandler.tempAnimalPos.get(i)) {
                    if(animal != null && animal.b2body != null && animal.getClass() == Simbakubwa.class) {
                        if (calcDistance(b2body.getPosition().x, b2body.getPosition().y, animal.b2body.getPosition().x, animal.b2body.getPosition().y) < Potamos.PPM * feedRange) {
                            enemyPos = new Vector2(animal.b2body.getPosition().x, animal.b2body.getPosition().y);
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
    protected void updateAnims(){
        if (rand.nextInt(2) == 1) {
            idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_female_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("auroch_female_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("auroch_female_dead").animList.get(0);
            sex = Potamos.Sex.FEMALE;
        } else {
            idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_male_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("auroch_male_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("auroch_male_dead").animList.get(0);
            sex = Potamos.Sex.MALE;
        }
    }

    @Override
    protected short defineCollision(short ANIMAL_BIT) {
        return (short) (Potamos.ANIMALS_BIT | Potamos.TILE_BIT | Potamos.WATER_BIT);
    }


    @Override
    public boolean checkFood(int posX, int posY){
        if (posX < GameScreen.MAP_SIZE &&
            posY < GameScreen.MAP_SIZE &&
            posX > 0 &&
            posY > 0) {
            if ((WorldHandler.worldTiles[posX][posY].type == "meadow" ||
                    WorldHandler.worldTiles[posX][posY].type == "fertile") &&
                    WorldHandler.worldTiles[posX][posY].capacity > 0) {
                foodFound = true;
                foodPos = new Vector2((posX) * Potamos.PPM + Potamos.PPM / 2,
                        (posY) * Potamos.PPM + Potamos.PPM / 2);
                return true;
            }
        }
        return false;
    }
    @Override
    public void die() {
        super.die();
        Filter filter = new Filter();
        filter.categoryBits = Potamos.DEAD_AUROCH_BIT;
        filter.maskBits = Potamos.SIMBA_BIT;
        for (Fixture fixture : b2body.getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }
    public void dispose () {
        super.dispose();
    }
}
