package com.kitter.eufrat.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.tools.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalDef;
import com.kitter.eufrat.tools.MouseInputProcessor;
import com.kitter.eufrat.tools.TextureUtils;

import java.util.Locale;
import java.util.Random;



public abstract class Animal extends Sprite implements Runnable {
    public enum State{IDLE, WALKING, SPEAK, COURT, PREGNANT, SEEKING_FOOD, EATING, DEAD};
    protected int PREGGO_TIME = 10;
    protected int EATING_TIME = 2;
    protected int STATE_EXECUTION_TIME = 10;

    // percentages for randomized states
    protected int IDLE_PERCENTAGE = 5;
    protected int WALKING_PERCENTAGE = 30;
    protected int SPEAK_PERCENTAGE = 10;
    protected int COURT_PERCENTAGE = 30;
    protected int FEED_PERCENTAGE = 25;

    // range for animals to look for food or mating
    protected int FEED_RANGE = 15;
    protected int COURT_RANGE = 10;

    public int generation;
    public State currentState;
    public Potamos.Sex sex;
    public World world;
    public float hunger;
    public float age;
    public int meal;
    public String name;
    protected boolean pregnant;
    protected float conceiveTime;
    protected float stateStartTime;
    protected float goDirectionTime;
    public float deathTime;
    protected float eatingStart;
    public Animation<TextureRegion> idle;
    public Animation<TextureRegion> eating;
    public Animation<TextureRegion> walking;
    public Animation<TextureRegion> voice;
    public Animation<TextureRegion> dead;
    public Texture highlight;
    public Texture menu_background;
    protected Vector2 speed;
    protected boolean stateExecuted;
    public boolean walkingRight;

    public boolean highlighted;
    protected boolean menu;
    protected String speakFile;
    protected String speakFile2;

    protected boolean foodFound;
    protected Vector2 foodPos;

    protected boolean femaleFound;
    protected Vector2 femalePos;

    protected TextureRegion reg;
    protected Random rand;
    public Body b2body;
    MouseInputProcessor mouse;

    private volatile boolean threadExit = false;
    public Animal( float x, float y, Potamos.Sex gender) {
        setPosition(x, y);

        generation = 0;
        //lastSex = 0;
        this.world = GameScreen.getWorld();
        sex = gender;
        speed = new Vector2(0,0);
        pregnant = false;
        walkingRight = false;
        femaleFound = false;
        foodFound = false;
        highlighted = false;
        menu = false;
        goDirectionTime = WorldHandler.getInstance().stateTime;
        currentState = State.IDLE;
        rand = WorldHandler.getInstance().rand;
        hunger = rand.nextInt(50);
        String[] className = this.getClass().getName().split("\\.");
        name = className[className.length-1];
        stateStartTime = 0;

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
        if(menu){
            batch.draw(menu_background,getX()-5,getY()-170,getWidth()*3,160);
            GameScreen.getHud().hudfont.draw(batch, name, getX()+5,getY()-20);
            GameScreen.getHud().hudfont.draw(batch,"state: " + currentState.toString().toLowerCase(Locale.ROOT),getX()+5,getY()-50);
            drawHunger(batch);
            drawAge(batch);
            GameScreen.getHud().hudfont.draw(batch,"generation: "+ generation,getX()+5,getY()-140);
        }
    }
    private void drawHunger(Batch batch){
        if(hunger<0) {
            GameScreen.getHud().hudfont.draw(batch, "full", getX() + 5, getY() - 80);
        }
        else if(hunger<50){
            GameScreen.getHud().hudfont.draw(batch, "not hungry", getX() + 5, getY() - 80);
        }
        else if(hunger<75){
            GameScreen.getHud().hudfont.draw(batch, "hungry", getX() + 5, getY() - 80);
        }
        else{
            GameScreen.getHud().hudfont.draw(batch, "starving", getX() + 5, getY() - 80);
        }
    }
    protected abstract void drawAge(Batch batch);

    public TextureRegion getFrame(float dt) {

        switch(currentState) {
            case IDLE:
            case PREGNANT:
                reg = idle.getKeyFrame(dt);
                break;
            case SEEKING_FOOD:
            case WALKING:
            case COURT:
                reg = walking.getKeyFrame(WorldHandler.getInstance().stateTime, true);
                break;
            case SPEAK:
                reg = voice.getKeyFrame(dt);
                break;
            case EATING:
                reg = eating.getKeyFrame(dt);
                break;
            case DEAD:
                reg = dead.getKeyFrame(dt);
                break;
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
                    b2body.setLinearVelocity(0, 0);
                    stateExecuted = true;
                    break;
                case WALKING:
                    speed.x = (rand.nextFloat()-0.5f) * 1000f;
                    speed.y = (rand.nextFloat()-0.5f) * 1000f;
                    b2body.applyLinearImpulse(speed, b2body.getWorldCenter(), true);
                    stateExecuted = true;
                    break;
                case COURT:
                    if (sex == Potamos.Sex.MALE && !femaleFound) {
                        scanForFemale(this.getClass());
                        if (femaleFound) {
                            goDirectionTime = WorldHandler.getInstance().stateTime;
                            speed.x = (femalePos.x - b2body.getPosition().x) * 500f;
                            speed.y = (femalePos.y - b2body.getPosition().y) * 500f;
                            b2body.applyLinearImpulse(speed, b2body.getWorldCenter(), true);
                        }
                        else {
                            currentState = State.WALKING;
                        }
                    }
                    else{
                        if(passed(femalePos)){
                            femaleFound = false;
                            currentState = State.IDLE;
                        }
                        if(WorldHandler.getInstance().stateTime>goDirectionTime+1 && femaleFound){
                            speed.x = (femalePos.x - b2body.getPosition().x) * 500f;
                            speed.y = (femalePos.y - b2body.getPosition().y) * 500f;
                            b2body.applyLinearImpulse(speed, b2body.getWorldCenter(), true);
                            goDirectionTime = WorldHandler.getInstance().stateTime;
                        }
                    }
                    break;
                case SPEAK:
                    b2body.applyLinearImpulse(new Vector2(-b2body.getLinearVelocity().x, -b2body.getLinearVelocity().y), b2body.getWorldCenter(), true);
                    if (WorldHandler.getInstance().withinScreen(getX(), getY())) {
                        int x = rand.nextInt(2000);
                        if (x < 1000) {
                            Potamos.manager.get(speakFile, Sound.class).play(GameScreen.VOLUME);
                        } else {
                            Potamos.manager.get(speakFile2, Sound.class).play(GameScreen.VOLUME);
                        }
                    }
                    stateExecuted = true;
                    break;
                case PREGNANT:
                    if(conceiveTime < WorldHandler.getInstance().stateTime - PREGGO_TIME){
                        WorldHandler.animalsToSpawn.add(new AnimalDef(this.getClass(),
                                new Vector2(getX(),
                                            getY()),
                                Potamos.Sex.CHILD,
                                generation+1));
                        currentState = State.IDLE;
                        pregnant = false;
                        stateExecuted = true;
                    }
                    break;
                case SEEKING_FOOD:
                    if (!foodFound) {
                        scanForFood();
                        if (foodFound) {
                            speed.x = (foodPos.x - b2body.getPosition().x) * 1000f ;
                            speed.y = (foodPos.y - b2body.getPosition().y) * 1000f ;
                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                            goDirectionTime = WorldHandler.getInstance().stateTime;
                        }
                        else{
                            Gdx.app.log("FAMINE","DANGER NO FOOD IN RANGE");
                            currentState = State.WALKING;
                        }
                    }
                    else{
                        if(passed(foodPos)) {
                            foodFound = false;
                            b2body.setLinearVelocity(0, 0);
                            currentState = State.EATING;
                            eatingStart = WorldHandler.getInstance().stateTime;

                            if (rand.nextInt(10) == 0) {
                                Potamos.manager.get("audio/sounds/nom-nom-nom.mp3", Sound.class).play(GameScreen.VOLUME);
                            }
                        }
                        if(WorldHandler.getInstance().stateTime>goDirectionTime+1){

                            speed.x = (foodPos.x - b2body.getPosition().x) * 500f ;
                            speed.y = (foodPos.y - b2body.getPosition().y) * 500f ;
                            b2body.applyLinearImpulse(new Vector2(speed.x, speed.y), b2body.getWorldCenter(), true);
                            goDirectionTime = WorldHandler.getInstance().stateTime;
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
                case DEAD:
                    if(WorldHandler.getInstance().stateTime > deathTime + Potamos.AUROCH_DECAY_TIME){
                        WorldHandler.animalRemove(this);
                        GameScreen.getWorld().destroyBody(b2body);
                        dispose();
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

    protected void scanForFood() {
        Vector2 currentPos = new Vector2 ((int) (getX()/ Potamos.PPM),(int) (getY()/ Potamos.PPM));
        int posX = (int)currentPos.x;
        int posY = (int)currentPos.y;
        boolean moveX = true;
        int moveMax = 0;
        int direction = 1;
        while(posX < (currentPos.x + FEED_RANGE)) {
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

    protected abstract boolean checkFood(int posX,int posY);

    protected void scanForFemale(Class c) {
        int currentPos = (int) (getY() / Potamos.PPM);
        int fluc = -1;
        for(int i=currentPos; i<currentPos + COURT_RANGE; ){
            if(i< GameScreen.MAP_SIZE && i> 0) {
                for (Animal animal : WorldHandler.tempAnimalPos.get(i)) {
                    if(animal.getClass()== c) {
                        if (animal.sex == Potamos.Sex.FEMALE && animal.currentState != State.DEAD) {
                            if (calcDistance(b2body.getPosition().x, b2body.getPosition().y, animal.b2body.getPosition().x, animal.b2body.getPosition().y) < Potamos.PPM * COURT_RANGE) {
                                femalePos = new Vector2(animal.b2body.getPosition().x, animal.b2body.getPosition().y);
                                femaleFound = true;
                                return;
                            }
                        }
                    }
                }
            }
            i = currentPos + fluc;
            if(fluc>0){
                fluc = -(fluc+1);
            }
            else{
                fluc = -fluc;
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
        if(stateExecuted || WorldHandler.getInstance().stateTime > stateStartTime + STATE_EXECUTION_TIME) {
            if(hunger>50) {
                currentState = State.SEEKING_FOOD;
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
            stateStartTime = WorldHandler.getInstance().stateTime;
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

    protected abstract void updateAnims();

    public void growUp(short ANIMAL_BIT){
        updateAnims();

        setBounds(getX(),getY(),idle.getKeyFrame(0).getRegionWidth(),idle.getKeyFrame(0).getRegionHeight());
        b2body.getFixtureList().removeIndex(0);
        FixtureDef fdef = defineShape(ANIMAL_BIT);

        b2body.createFixture(fdef).setUserData(this);
    }

    public FixtureDef defineShape(short ANIMAL_BIT){
        FixtureDef fdef = new FixtureDef();
        fdef.filter.categoryBits = ANIMAL_BIT;
        fdef.filter.maskBits = (short) (ANIMAL_BIT | Potamos.TILE_BIT); //this will be shit with new animals

        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(8,8);
        vertices[1] = new Vector2(idle.getKeyFrame(0).getRegionWidth()-8,8);
        vertices[2] = new Vector2(idle.getKeyFrame(0).getRegionWidth()-8,idle.getKeyFrame(0).getRegionHeight()-6);
        vertices[3] = new Vector2(8,idle.getKeyFrame(0).getRegionHeight()-6);
        shape.set(vertices);

        fdef.shape = shape;
        return fdef;
    }

    public void defineHighlight(){
        highlight = TextureUtils.getInstance().utilsMap.get("highlight");
        menu_background = TextureUtils.getInstance().utilsMap.get("menu_background");
    }

    public void die(){

        highlighted = false;
        deathTime = WorldHandler.getInstance().stateTime;
        GameScreen.getImultiplexer().removeProcessor(mouse);
        b2body.setType(BodyDef.BodyType.StaticBody);
        WorldHandler.animalsHighlighted.remove(this);
        WorldHandler.animals.remove(this);
        WorldHandler.deadAnimals.add(this);
        threadStop();

    }

    @Override
    public void run() {
        stateExecuted = true;
        while(!threadExit) {
            try {
                Thread.sleep(rand.nextInt(3)*1000);
                findState();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        currentState = Animal.State.DEAD;
        Gdx.app.log("LIFE THREAD","CUT");
    }

    public void threadStop() {
        threadExit = true;
    }
    public void dispose () {

    }
}
