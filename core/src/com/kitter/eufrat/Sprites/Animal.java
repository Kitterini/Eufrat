package com.kitter.eufrat.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.tools.*;
import com.kitter.eufrat.screens.GameScreen;

import java.util.Random;



public abstract class Animal extends Sprite {  // implements Runnable {
    protected int PREGGO_TIME = 10;
    protected int ACTION_TIME = 3;
    protected int STATE_EXECUTION_TIME = 15;

    // percentages for randomized states
    protected int IDLE_PERCENTAGE;
    protected int WALKING_PERCENTAGE;
    protected int SPEAK_PERCENTAGE;
    protected int COURT_PERCENTAGE;
    protected int FEED_PERCENTAGE;
    protected int RUN_PERCENTAGE;

    // range for animals to look for food or mating
    protected int feedRange;
    protected int courtRange;
    protected int runRange;

    public float adolescenceTime;
    public int lifeTime;
    protected int mealSize;

    public int generation;
    public Potamos.State currentState;
    public Potamos.Sex sex;
    public World world;
    public float hunger;
    public float birthTime;

    public String name;
    protected boolean pregnant;
    public boolean vanquished;
    public float conceiveTime;
    protected float stateStartTime;
    protected float  nextStateLookup;
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
    protected boolean destroyed;
    public boolean timeToEat;
    protected String speakFile;
    protected String speakFile2;

    protected boolean foodFound;
    protected Vector2 foodPos;
    protected Vector2 enemyPos;

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
        world = GameScreen.getWorld();
        sex = gender;
        speed = new Vector2(0,0);
        enemyPos = new Vector2(0,0);
        pregnant = false;
        vanquished = false;
        walkingRight = false;
        femaleFound = false;
        foodFound = false;
        highlighted = false;
        menu = false;
        destroyed = false;
        goDirectionTime = WorldHandler.getInstance().stateTime;
        currentState = Potamos.State.IDLE;
        rand = WorldHandler.getInstance().rand;
        hunger = rand.nextInt(50);
        conceiveTime = -100;
        stateStartTime = 0;
        nextStateLookup = 0;
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
            drawState(batch);
            drawHunger(batch);
            drawAge(batch);
            GameScreen.getHud().hudfont.draw(batch, LangPack.data.get("ANIMAL_GENERATION") + ": "+ generation,getX()+5,getY()-140);
        }
    }

    private void drawState(Batch batch){
        String s ="";
        switch(currentState) {
            case RUN:
                s = LangPack.data.get("ANIMAL_STATE_RUN").toString();
                break;
            case SLEEP:
                s = LangPack.data.get("ANIMAL_STATE_SLEEP").toString();
                break;
            case IDLE:
                s = LangPack.data.get("ANIMAL_STATE_IDLE").toString();
                break;
            case PREGNANT:
                s = LangPack.data.get("ANIMAL_STATE_PREGNANT").toString();
                break;
            case SEEKING_FOOD:
                s = LangPack.data.get("ANIMAL_STATE_SEEKING_FOOD").toString();
                break;
            case WALKING:
                s = LangPack.data.get("ANIMAL_STATE_WALKING").toString();
                break;
            case COURT:
                s = LangPack.data.get("ANIMAL_STATE_COURT").toString();
                break;
            case SPEAK:
                s = LangPack.data.get("ANIMAL_STATE_SPEAK").toString();
                break;
            case EATING:
                s = LangPack.data.get("ANIMAL_STATE_EATING").toString();
                break;
            case DEAD:
                s = LangPack.data.get("ANIMAL_STATE_DEAD").toString();
                break;
        }
        GameScreen.getHud().hudfont.draw(batch,LangPack.data.get("ANIMAL_STATE").toString() + ": " + s,getX()+5,getY()-50);
    }
    private void drawHunger(Batch batch){
        if(hunger<0) {
            GameScreen.getHud().hudfont.draw(batch, LangPack.data.get("ANIMAL_FOOD_FULL").toString(), getX() + 5, getY() - 80);
        }
        else if(hunger<50){
            GameScreen.getHud().hudfont.draw(batch, LangPack.data.get("ANIMAL_FOOD_NOT_HUNGRY").toString(), getX() + 5, getY() - 80);
        }
        else if(hunger<75){
            GameScreen.getHud().hudfont.draw(batch, LangPack.data.get("ANIMAL_FOOD_HUNGRY").toString(), getX() + 5, getY() - 80);
        }
        else{
            GameScreen.getHud().hudfont.draw(batch, LangPack.data.get("ANIMAL_FOOD_STARVING").toString(), getX() + 5, getY() - 80);
        }
    }

    private void drawAge(Batch batch){
        float curAge = WorldHandler.getInstance().stateTime- birthTime;
        if(curAge < lifeTime/5f){
            GameScreen.getHud().hudfont.draw(batch,LangPack.data.get("ANIMAL_AGE_JUVENILE").toString(),getX()+5,getY()-110);
        }
        else if(curAge< lifeTime / 5f * 2){
            GameScreen.getHud().hudfont.draw(batch,LangPack.data.get("ANIMAL_AGE_YOUNG").toString(),getX()+5,getY()-110);
        }
        else if(curAge < lifeTime / 5f * 3){
            GameScreen.getHud().hudfont.draw(batch,LangPack.data.get("ANIMAL_AGE_MIDDLE_AGED").toString(),getX()+5,getY()-110);
        }
        else if(curAge < lifeTime / 5f * 4){
            GameScreen.getHud().hudfont.draw(batch,LangPack.data.get("ANIMAL_AGE_OLD").toString(),getX()+5,getY()-110);
        }
        else{
            GameScreen.getHud().hudfont.draw(batch, LangPack.data.get("ANIMAL_AGE_VENERABLE").toString(),getX()+5,getY()-110);
        }
    }

    protected abstract TextureRegion getFrame(float dt);


    public void update() {
        if(WorldHandler.getInstance().stateTime > stateStartTime + nextStateLookup && currentState != Potamos.State.DEAD){
            findState();
        }
        if(!stateExecuted) {
            switch (currentState) {
                case SLEEP:
                case IDLE:
                    b2body.setLinearVelocity(0, 0);
                    if(stateStartTime > WorldHandler.getInstance().stateTime - ACTION_TIME) {
                        stateExecuted = true;
                    }
                    break;
                case WALKING:
                    b2body.applyLinearImpulse(randSpeed(), b2body.getWorldCenter(), true);
                    if(stateStartTime > WorldHandler.getInstance().stateTime - ACTION_TIME) {
                        stateExecuted = true;
                    }
                    break;
                case RUN:
                    if(enemyClose()) {
                        if (calcDistance(getX(), getY(), enemyPos.x, enemyPos.y) < runRange * Potamos.PPM) {
                            Vector2 runVector = new Vector2(-directionSpeed(enemyPos).x, -directionSpeed(enemyPos).y);
                            b2body.applyLinearImpulse(runVector, b2body.getWorldCenter(), true);
                        }
                    } else {
                        currentState = Potamos.State.IDLE;
                    }
                    if(stateStartTime > WorldHandler.getInstance().stateTime - ACTION_TIME) {
                        stateExecuted = true;
                    }
                    break;
                case COURT:
                    if (sex == Potamos.Sex.MALE && !femaleFound) {
                        scanForFemale(this.getClass());
                        if (femaleFound) {
                            goDirectionTime = WorldHandler.getInstance().stateTime;
                            b2body.applyLinearImpulse(directionSpeed(femalePos), b2body.getWorldCenter(), true);
                        }
                        else {
                            currentState = Potamos.State.WALKING;
                        }
                    }
                    else{
                        if(passed(femalePos,2)){
                            femaleFound = false;
                            currentState = Potamos.State.IDLE;
                        }
                        if(WorldHandler.getInstance().stateTime>goDirectionTime+1 && femaleFound){
                            b2body.applyLinearImpulse(directionSpeed(femalePos), b2body.getWorldCenter(), true);
                            goDirectionTime = WorldHandler.getInstance().stateTime;
                        }
                    }
                    break;
                case SPEAK:
                    if (WorldHandler.getInstance().withinScreen(getX(), getY())) {
                        int x = rand.nextInt(2000);
                        if (x < 1000) {
                            Potamos.manager.get(speakFile, Sound.class).play(GameScreen.VOLUME);
                        } else {
                            Potamos.manager.get(speakFile2, Sound.class).play(GameScreen.VOLUME);
                        }
                    }
                    if(stateStartTime > WorldHandler.getInstance().stateTime - ACTION_TIME) {
                        stateExecuted = true;
                    }
                    break;
                case PREGNANT:
                    if(conceiveTime < WorldHandler.getInstance().stateTime - PREGGO_TIME){
                        WorldHandler.animalsToSpawn.add(new AnimalDef(this.getClass(),
                                new Vector2(getX(),
                                            getY()),
                                Potamos.Sex.CHILD,
                                generation+1));
                        currentState = Potamos.State.IDLE;
                        pregnant = false;
                        stateExecuted = true;
                    }
                    break;
                case SEEKING_FOOD:
                    if (!foodFound) {
                        scanForFood();
                        if (foodFound) {
                            b2body.applyLinearImpulse(directionSpeed(foodPos), b2body.getWorldCenter(), true);
                            goDirectionTime = WorldHandler.getInstance().stateTime;
                        }
                        else{
                            Gdx.app.log("FAMINE","DANGER NO FOOD IN RANGE");
                            currentState = Potamos.State.WALKING;
                        }
                    }
                    else{
                        if(eligibleForFood()) {
                            foodFound = false;
                            b2body.setLinearVelocity(0, 0);
                            currentState = Potamos.State.EATING;
                            eatingStart = WorldHandler.getInstance().stateTime;
                            timeToEat = false;
                            if (rand.nextInt(10) == 0 && WorldHandler.getInstance().withinScreen(getX(), getY())) {
                                Potamos.manager.get("audio/sounds/nom-nom-nom.mp3", Sound.class).play(GameScreen.VOLUME);
                            }
                        }
                        else {
                            // make sure passed food, stop running the same direction, look for new one
                            int changeDirection = 1;
                            if (WorldHandler.getInstance().stateTime > goDirectionTime + 1) {
                                scanForFood();
                                b2body.applyLinearImpulse(directionSpeed(foodPos), b2body.getWorldCenter(), true);
                                goDirectionTime = WorldHandler.getInstance().stateTime;
                                changeDirection = rand.nextInt(2);
                            }
                            if (changeDirection == 0) {
                                foodFound = false;
                            }
                        }
                    }
                    break;
                case EATING:
                    if(eatingStart < WorldHandler.getInstance().stateTime - ACTION_TIME){
                        currentState = Potamos.State.IDLE;
                        eat();
                        if(hunger > 0) {
                            hunger -= mealSize;
                        }
                        stateExecuted = true;
                    }
                    break;
                case DEAD:
                    if(WorldHandler.getInstance().stateTime > deathTime + Potamos.ANIMAL_DECAY_TIME && !destroyed){
                        WorldHandler.animalsToDespawn.add(this);
                        destroyed = true;
                    }
                    break;
            }
        }
        if ((currentState == Potamos.State.IDLE || currentState == Potamos.State.EATING || currentState == Potamos.State.PREGNANT || currentState == Potamos.State.SPEAK)
                && b2body.getLinearVelocity().x != 0 && b2body.getLinearVelocity().y != 0) {
            b2body.setLinearVelocity(0, 0);
        }
        if(b2body.getLinearVelocity().x!=0) {
            walkingRight = b2body.getLinearVelocity().x > 0;
        }
        setPosition(b2body.getPosition().x, b2body.getPosition().y);
    }

    private Vector2 directionSpeed(Vector2 pos){
        speed.x = (pos.x - b2body.getPosition().x) * 100f * GameScreen.GAME_SPEED;
        speed.y = (pos.y - b2body.getPosition().y) * 100f * GameScreen.GAME_SPEED;
        return speed;
    }

    private Vector2 randSpeed(){
        speed.x = (rand.nextFloat()-0.5f) * 200f * GameScreen.GAME_SPEED;
        speed.y = (rand.nextFloat()-0.5f) * 200f * GameScreen.GAME_SPEED;
        return speed;
    }

    protected boolean passed(Vector2 pos, int margin){
        if(pos == null) {
            return true;
        }
        if(getX() - margin< pos.x &&
                getY() - margin < pos.y &&
                getX() + getWidth() + margin > pos.x &&
                getY() + getHeight() + margin > pos.y){
            return true;
        }
        return false;
    }

    protected abstract void scanForFood();

    protected abstract boolean checkFood(int posX,int posY);

    protected abstract boolean eligibleForFood();
    protected abstract void eat();

    protected void scanForFemale(Class c) {
        int currentPos = (int) (getY() / Potamos.PPM);
        int fluc = -1;
        for(int i=currentPos; i<currentPos + courtRange; ){
            if(i< GameScreen.MAP_SIZE && i> 0) {
                for (Animal animal : WorldHandler.tempAnimalPos.get(i)) {
                    if(animal.getClass()== c) {
                        if (animal.sex == Potamos.Sex.FEMALE && animal.currentState != Potamos.State.DEAD && animal.getClass() == this.getClass()) {
                            if (calcDistance(b2body.getPosition().x, b2body.getPosition().y, animal.b2body.getPosition().x, animal.b2body.getPosition().y) < Potamos.PPM * courtRange) {
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
    public abstract boolean enemyClose();
    protected void findState() {
        if((stateExecuted || WorldHandler.getInstance().stateTime > stateStartTime + STATE_EXECUTION_TIME) &&
            currentState != Potamos.State.DEAD) {
            if(hunger>75) {
                currentState = Potamos.State.SEEKING_FOOD;
            }
            else {
                if (!pregnant) {
                    int sum = IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE + COURT_PERCENTAGE + FEED_PERCENTAGE + RUN_PERCENTAGE;
                    int state = rand.nextInt(sum);
                    if (state < IDLE_PERCENTAGE) {
                        currentState = Potamos.State.IDLE;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE) {
                        currentState = Potamos.State.WALKING;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE) {
                        currentState = Potamos.State.SPEAK;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE + COURT_PERCENTAGE){
                        currentState = Potamos.State.COURT;
                    } else if (state < IDLE_PERCENTAGE + WALKING_PERCENTAGE + SPEAK_PERCENTAGE + COURT_PERCENTAGE + FEED_PERCENTAGE){
                        currentState = Potamos.State.SEEKING_FOOD;
                    } else {
                        currentState = Potamos.State.RUN;
                    }
                } else {
                    if (currentState != Potamos.State.PREGNANT) {
                        b2body.setLinearVelocity(0, 0);
                        conceiveTime = WorldHandler.getInstance().stateTime;
                    }
                    currentState = Potamos.State.PREGNANT;
                }
            }
            stateExecuted = false;
            stateStartTime = WorldHandler.getInstance().stateTime;
            nextStateLookup = rand.nextInt(5);
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
        setOrigin(idle.getKeyFrame(0).getRegionWidth()/2,idle.getKeyFrame(0).getRegionHeight()/2);
    }

    protected abstract void updateAnims();

    public void growUp(short ANIMAL_BIT){
        updateAnims();

        setBounds(getX(),getY(),idle.getKeyFrame(0).getRegionWidth(),idle.getKeyFrame(0).getRegionHeight());

        world.destroyBody(b2body);
        BodyDef bdef = new BodyDef();
        bdef.position.set(new Vector2(getX(),getY()));
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);
        FixtureDef fdef = defineShape(ANIMAL_BIT);
        fdef.restitution=0.5f;
        b2body.createFixture(fdef).setUserData(this);
        setOrigin(idle.getKeyFrame(0).getRegionWidth()/2,idle.getKeyFrame(0).getRegionHeight()/2);
    }

    public FixtureDef defineShape(short ANIMAL_BIT){
        FixtureDef fdef = new FixtureDef();
        fdef.filter.categoryBits = ANIMAL_BIT;
        fdef.filter.maskBits = defineCollision(ANIMAL_BIT); //this will be shit with new animals

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
    protected abstract short defineCollision(short ANIMAL_BIT);
    public void defineHighlight(){
        highlight = TextureUtils.getInstance().utilsMap.get("highlight");
        menu_background = TextureUtils.getInstance().utilsMap.get("menu_background");
    }
    protected Vector2 getCurrentTilePos(){
        return new Vector2 ((int) ((getX()+ getWidth()/2)/ Potamos.PPM ),(int) ((getY()+ getHeight()/2)/ Potamos.PPM));
    }
    public void die(){
        highlighted = false;
        WorldHandler.animalsHighlighted.remove(this);
        deathTime = WorldHandler.getInstance().stateTime;
        GameScreen.getImultiplexer().removeProcessor(mouse);
        b2body.setType(BodyDef.BodyType.StaticBody);

        WorldHandler.animals.remove(this);
        WorldHandler.deadAnimals.add(this);
        currentState = Potamos.State.DEAD;
        stateExecuted = false;
    }

    public void dispose() {
        GameScreen.getWorld().destroyBody(b2body);
        b2body= null;
    }
}
