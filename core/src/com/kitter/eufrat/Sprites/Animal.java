package com.kitter.eufrat.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.Eufrat;
import com.kitter.eufrat.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.AnimalDef;
import com.kitter.eufrat.tools.TextureUtils;

import java.util.Random;


public abstract class Animal extends Sprite implements Runnable {
    public enum State{IDLE, WALKING, SPEAK, COURT, PREGNANT};
    protected int PREGGO_TIME = 10;
    protected int IDLE_PERCENTAGE = 30;
    protected int WALKING_PERCENTAGE = 30;
    protected int SPEAK_PERCENTAGE = 10;
    protected int COURT_PERCENTAGE = 30;
    protected int COURT_RANGE = 15;
    public State currentState;
    public Eufrat.Sex sex;
    public World world;
    protected int hunger;
    public float age;
    protected boolean pregnant;
    protected float conceiveTime;
    public Animation<TextureRegion> idle;
    public Animation<TextureRegion> walking;
    public Texture highlight;
    protected Vector2 speed;
    protected boolean stateExecuted;
    public boolean walkingRight;
    protected boolean femaleFound;
    protected boolean highlighted;
    protected String speakFile;
    protected String speakFile2;
    Vector2 femalePos;
    TextureRegion reg;
    Random rand;
    public Body b2body;
    ShapeRenderer sr;
    public Animal( float x, float y, Eufrat.Sex gender) {
        setPosition(x, y);
        hunger = 0;

        //lastSex = 0;
        this.world = GameScreen.getWorld();
        sex = gender;
        speed = new Vector2(0,0);
        pregnant = false;
        walkingRight = false;
        femaleFound = false;
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
            case WALKING:
            case COURT:
                reg = walking.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case SPEAK:
                reg = idle.getKeyFrame(dt);
                // maybe some open face
                break;
        }
        return reg;
    }

    public void update(float dt) {
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
                    if (hunger < 50 && sex == Eufrat.Sex.MALE && !femaleFound) {
                        scanForFemale(this.getClass());
                        if (femaleFound) {
                            speed.x = (femalePos.x - b2body.getPosition().x) * 1000f;
                            speed.y = (femalePos.y - b2body.getPosition().y) * 1000f;

                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                        }
                        if(femalePassed()){
                            femaleFound = false;
                            b2body.applyLinearImpulse(new Vector2(-speed.x, -speed.y), b2body.getWorldCenter(), true);
                            stateExecuted = true;
                        }
                    }
                    else{
                        stateExecuted = true;
                    }
                    break;
                case SPEAK:
                    b2body.applyLinearImpulse(new Vector2(-b2body.getLinearVelocity().x, -b2body.getLinearVelocity().y), b2body.getWorldCenter(), true);
                    if (WorldHandler.getInstance().withinScreen(getX(), getY())) {
                        int x = rand.nextInt(6000);
                        float volume = 0.15f / (float) (WorldHandler.getInstance().screenXmax - WorldHandler.getInstance().screenXmin);
                        if (x < 1000) {
                            Eufrat.manager.get(speakFile, Sound.class).play(volume);
                        } else if (x < 2000) {
                            Eufrat.manager.get(speakFile2, Sound.class).play(volume);
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
                                Eufrat.Sex.CHILD));
                        currentState = State.IDLE;
                        pregnant = false;
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

    private boolean femalePassed(){
        if(femalePos==null) {
            return true;
        }
        if (speed.x > 0 && speed.y > 0) {
            return getX() > femalePos.x && getY() > femalePos.y;
        }
        if (speed.x < 0 && speed.y < 0) {
            return getX() < femalePos.x && getY() < femalePos.y;
        }
        if (speed.x > 0 && speed.y < 0) {
            return getX() > femalePos.x && getY() < femalePos.y;
        }
        if (speed.x < 0 && speed.y > 0) {
            return getX() < femalePos.x && getY() > femalePos.y;
        }
        return false;
    }

    protected void scanForFemale(Class c) {
        int currentPos = (int) (getY()/ Eufrat.PPM);
        int fluc = -1;
        for(int i=currentPos; i<currentPos+COURT_RANGE; ){
            if(i< GameScreen.MAP_SIZE && i> 0) {
                for (Animal animal : WorldHandler.tempAnimalPos.get(i)) {
                    if(animal.getClass()== c) {
                        if (animal.sex == Eufrat.Sex.FEMALE) {
                            if (calcDistance(b2body.getPosition().x, b2body.getPosition().y, animal.b2body.getPosition().x, animal.b2body.getPosition().y) < Eufrat.PPM * COURT_RANGE) {
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
        //return Vector2
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
            if(!pregnant) {
                int sum = IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE + COURT_PERCENTAGE;
                int state = rand.nextInt(sum);
                if (state < IDLE_PERCENTAGE) {
                    currentState = State.IDLE;
                } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE) {
                    currentState = State.WALKING;
                } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE) {
                    currentState = State.SPEAK;
                } else {
                    currentState = State.COURT;
                }
            }
            else {
                if(currentState != State.PREGNANT) {
                    Gdx.app.log("SIUR","PREGNANCY STARTED");
                    conceiveTime = WorldHandler.getInstance().stateTime;
                }
                currentState = State.PREGNANT;
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
        if(b2body.getFixtureList().get(0).getFilterData().categoryBits == Eufrat.AUROCH_BIT) {
            if (rand.nextInt(2) == 1) {
                idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
                walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
                sex = Eufrat.Sex.FEMALE;
            } else {
                idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
                walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
                sex = Eufrat.Sex.MALE;
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
        fdef.filter.maskBits = (short) (ANIMAL_BIT | Eufrat.TILE_BIT);

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
        while(true) {
            try {
                Thread.sleep(rand.nextInt(10)*1000);
                findState();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void dispose () {
        b2body.destroyFixture(b2body.getFixtureList().get(0));
    }
}
