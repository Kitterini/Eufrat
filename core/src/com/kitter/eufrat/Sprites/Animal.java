package com.kitter.eufrat.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.AnimalDef;
import com.kitter.eufrat.tools.TextureUtils;

import java.util.Random;


public abstract class Animal extends Sprite implements Runnable {
    public enum State{IDLE, WALKING, SPEAK, COURT, PREGNANT, SEEKING_FOOD, EATING};
    protected int PREGGO_TIME = 10;
    protected int EATING_TIME = 2;

    // percentages for randomized states
    protected int IDLE_PERCENTAGE = 15;
    protected int WALKING_PERCENTAGE = 15;
    protected int SPEAK_PERCENTAGE = 10;
    protected int COURT_PERCENTAGE = 50;
    protected int FEED_PERCENTAGE = 10;

    // range for animals to look for food or mating
    protected int FEED_RANGE = 15;
    protected int COURT_RANGE = 10;

    public State currentState;
    public Potamos.Sex sex;
    public World world;
    public float hunger;
    public float age;
    public int meal;
    protected boolean pregnant;
    protected float conceiveTime;
    protected float eatingStart;
    public Animation<TextureRegion> idle;
    public Animation<TextureRegion> eating;
    public Animation<TextureRegion> walking;
    public Texture highlight;
    protected Vector2 speed;
    protected boolean stateExecuted;
    public boolean walkingRight;

    protected boolean highlighted;
    protected String speakFile;
    protected String speakFile2;

    protected boolean foodFound;
    protected Vector2 foodPos;

    protected boolean femaleFound;
    protected Vector2 femalePos;

    protected TextureRegion reg;
    protected Random rand;
    public Body b2body;
    private volatile boolean threadExit = false;
    public Animal( float x, float y, Potamos.Sex gender) {
        setPosition(x, y);
        hunger = 0;

        //lastSex = 0;
        this.world = GameScreen.getWorld();
        sex = gender;
        speed = new Vector2(0,0);
        pregnant = false;
        walkingRight = false;
        femaleFound = false;
        foodFound = false;
        highlighted = false;
        currentState = State.IDLE;
        rand = WorldHandler.getInstance().rand;
    }

    @Override
    public void draw(Batch batch, float delta) {
        TextureRegion r = getFrame(delta);
        if(walkingRight && !r.isFlipX()){
            r.flip(true,false);
        }
        else if(!walkingRight && r.isFlipX()){
            r.flip(true,false);
        }
        batch.draw(r,getX(),getY(),getWidth()/2,getHeight()/2,getWidth(),getHeight(),getScaleX(),getScaleY(),getRotation());
    }

    public void drawHighlighted(Batch batch) {
        batch.draw(highlight,getX()-5,getY()-5,getWidth()+10,getHeight()+5);
    }

    public TextureRegion getFrame(float dt) {

        switch(currentState) {
            case IDLE:
                reg = idle.getKeyFrame(dt);
                break;
            case SEEKING_FOOD:
            case WALKING:
            case COURT:
                reg = walking.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case SPEAK:
                reg = idle.getKeyFrame(dt);
                // maybe some open face
                break;
            case EATING:
                reg = eating.getKeyFrame(dt);
        }
        return reg;
    }

    public void update(float dt) {
        if(currentState==State.IDLE && b2body.getLinearVelocity().x != 0 && b2body.getLinearVelocity().x != 0) {
            b2body.setLinearVelocity(0, 0);
        }
        if(!stateExecuted) {
            switch (currentState) {
                case IDLE:
                    b2body.applyLinearImpulse(new Vector2(-b2body.getLinearVelocity().x, -b2body.getLinearVelocity().y), b2body.getWorldCenter(), true);
                    stateExecuted = true;
                    break;
                case WALKING:
                    speed = new Vector2((rand.nextFloat()-0.5f)*500f,(rand.nextFloat()-0.5f)*500f);
                    b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                    stateExecuted = true;
                    break;
                case COURT:
                    if (hunger < 80 && sex == Potamos.Sex.MALE && !femaleFound) {
                        scanForFemale(this.getClass());
                        if (femaleFound) {
                            speed.x = (femalePos.x - b2body.getPosition().x) * 1000f;
                            speed.y = (femalePos.y - b2body.getPosition().y) * 1000f;

                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                        }
                        else{
                            speed = new Vector2((rand.nextFloat()-0.5f)*500f,(rand.nextFloat()-0.5f)*500f);
                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                            stateExecuted = true;
                        }

                    }
                    else{
                        if(passed(femalePos)){
                            femaleFound = false;
                            b2body.setLinearVelocity(0,0);
                            //b2body.applyLinearImpulse(new Vector2(-speed.x, -speed.y), b2body.getWorldCenter(), true);
                            stateExecuted = true;
                        }
                    }
                    break;
                case SPEAK:
                    b2body.applyLinearImpulse(new Vector2(-b2body.getLinearVelocity().x, -b2body.getLinearVelocity().y), b2body.getWorldCenter(), true);
                    if (WorldHandler.getInstance().withinScreen(getX(), getY())) {
                        int x = rand.nextInt(6000);

                        if (x < 1000) {
                            Potamos.manager.get(speakFile, Sound.class).play(GameScreen.VOLUME);
                        } else if (x < 2000) {
                            Potamos.manager.get(speakFile2, Sound.class).play(GameScreen.VOLUME);
                        }
                    }
                    stateExecuted = true;
                    break;
                case PREGNANT:
                    if(conceiveTime < WorldHandler.getInstance().stateTime - PREGGO_TIME){
                        Gdx.app.log("SIUR","PREGNANCY ENDED");
                        WorldHandler.animalsToSpawn.add(new AnimalDef(this.getClass(),
                                new Vector2(getX(),
                                            getY()),
                                Potamos.Sex.CHILD));
                        currentState = State.IDLE;
                        pregnant = false;
                        stateExecuted = true;
                    }
                    break;
                case SEEKING_FOOD:
                    if (!foodFound) {
                        scanForFood();
                        if (foodFound) {
                            speed.x = (foodPos.x - b2body.getPosition().x) * 1000f;
                            speed.y = (foodPos.y - b2body.getPosition().y) * 1000f;
                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                        }
                        else{
                            Gdx.app.log("FAMINE","DANGER NO FOOD IN RANGE");
                            speed = new Vector2((rand.nextFloat()-0.5f)*500f,(rand.nextFloat()-0.5f)*500f);
                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                            stateExecuted = true;
                        }
                    }
                    else{
                        if(passed(foodPos)){
                            foodFound = false;
                            b2body.setLinearVelocity(0,0);
                            currentState = State.EATING;
                            eatingStart = WorldHandler.getInstance().stateTime;
                            if( rand.nextInt(10)==0) {
                                Potamos.manager.get("audio/sounds/nom-nom-nom.mp3", Sound.class).play(GameScreen.VOLUME);
                            }
                        }
                    }
                    break;
                case EATING:
                    if(eatingStart < WorldHandler.getInstance().stateTime - EATING_TIME){
                        currentState = State.IDLE;
                        WorldHandler.worldTiles[(int)(foodPos.x/Potamos.PPM)][(int)(foodPos.y/Potamos.PPM)].decreaseCapacity();
                        if(hunger>0) {
                            hunger -= meal;
                        }
                        stateExecuted = true;
                    }
                    break;
            }

        }
        if(b2body.getLinearVelocity().x!=0) {
            walkingRight = b2body.getLinearVelocity().x > 0;
        }
        setPosition(b2body.getPosition().x, b2body.getPosition().y);
    }

    private boolean passed(Vector2 pos){

        if(pos == null) {
            return true;
        }
        if (speed.x > 0 && speed.y > 0) {
            return getX() > pos.x && getY() > pos.y;
        }
        if (speed.x < 0 && speed.y < 0) {
            return getX() < pos.x && getY() < pos.y;
        }
        if (speed.x > 0 && speed.y < 0) {
            return getX() > pos.x && getY() < pos.y;
        }
        if (speed.x < 0 && speed.y > 0) {
            return getX() < pos.x && getY() > pos.y;
        }
        return false;
    }
    protected abstract void scanForFood();
    protected void scanForFemale(Class c) {
        int currentPos = (int) (getY()/ Potamos.PPM);
        int fluc = -1;
        for(int i=currentPos; i<currentPos + COURT_RANGE; ){
            if(i< GameScreen.MAP_SIZE && i> 0) {
                for (Animal animal : WorldHandler.tempAnimalPos.get(i)) {
                    if(animal.getClass()== c) {
                        if (animal.sex == Potamos.Sex.FEMALE) {
                            if (calcDistance(b2body.getPosition().x, b2body.getPosition().y, animal.b2body.getPosition().x, animal.b2body.getPosition().y) < Potamos.PPM * COURT_RANGE) {
                                femalePos = new Vector2(animal.b2body.getPosition().x, animal.b2body.getPosition().y);
                                femaleFound = true;
                                return;
                            }
                        }
                    }
                }
            }
            i = currentPos+ fluc;
            if(fluc>0){
                fluc = -(fluc+1);
            }
            else{
                fluc = -(fluc-1);;
            }
        }

    }

    protected float calcDistance(float x1,
                                 float y1,
                                 float x2,
                                 float y2) {
        return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public void setPregnant(){
        pregnant = true;
    }

    public void findState() {
        if(stateExecuted) {
            if(hunger>50) {
                currentState = State.SEEKING_FOOD;
                Gdx.app.log("FOOD", "VERY HUNGRY");
            }
            else {
                if (!pregnant) {
                    int sum = IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE + COURT_PERCENTAGE + FEED_PERCENTAGE;
                    int state = rand.nextInt(sum);
                    if (state < IDLE_PERCENTAGE) {
                        currentState = State.IDLE;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE) {
                        currentState = State.WALKING;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE) {
                        currentState = State.SPEAK;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE + COURT_PERCENTAGE){
                        currentState = State.COURT;
                    } else {
                        currentState = State.SEEKING_FOOD;
                    }
                } else {
                    if (currentState != State.PREGNANT) {
                        b2body.setLinearVelocity(0, 0);
                        conceiveTime = WorldHandler.getInstance().stateTime;
                    }
                    currentState = State.PREGNANT;
                }
            }
            stateExecuted = false;
        }


    }

    public void define(short ANIMAL_BIT){
        //body section
        defineHighlight();
        BodyDef bdef = new BodyDef();
        bdef.position.set(new Vector2(getX(),getY()));
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);
        FixtureDef fdef = defineShape(ANIMAL_BIT);
        fdef.restitution=0.5f;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void growUp(short ANIMAL_BIT){
        if(b2body.getFixtureList().get(0).getFilterData().categoryBits == Potamos.AUROCH_BIT) {
            if (rand.nextInt(2) == 1) {
                idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
                eating = AnimalAnims.getInstance().animsMap.get("auroch_female_eating").animList.get(0);
                walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
                sex = Potamos.Sex.FEMALE;
            } else {
                idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
                eating = AnimalAnims.getInstance().animsMap.get("auroch_male_eating").animList.get(0);
                walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
                sex = Potamos.Sex.MALE;
            }
        }

        setBounds(getX(),getY(),idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4);
        b2body.getFixtureList().removeIndex(0);
        FixtureDef fdef = defineShape(ANIMAL_BIT);

        b2body.createFixture(fdef).setUserData(this);
    }

    public FixtureDef defineShape(short ANIMAL_BIT){
        FixtureDef fdef = new FixtureDef();
        fdef.filter.categoryBits = ANIMAL_BIT;
        fdef.filter.maskBits = (short) (ANIMAL_BIT | Potamos.TILE_BIT);

        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(0,0);
        vertices[1] = new Vector2(idle.getKeyFrame(0).getRegionWidth()*4,0);
        vertices[2] = new Vector2(idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4-8);
        vertices[3] = new Vector2(0,idle.getKeyFrame(0).getRegionHeight()*4-8);
        shape.set(vertices);

        fdef.shape = shape;
        return fdef;
    }

    public void defineHighlight(){
        highlight = TextureUtils.getInstance().utilsMap.get("highlight");
    }

    @Override
    public void run() {
        stateExecuted = true;
        while(!threadExit) {
            try {
                Thread.sleep(rand.nextInt(5)*1000);
                findState();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Gdx.app.log("THREAD","ENDED");
    }

    public void threadStop() {
        threadExit = true;
    }
    public void dispose () {

    }
}
