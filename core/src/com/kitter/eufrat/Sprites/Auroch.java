package com.kitter.eufrat.Sprites;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.tools.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.MouseInputProcessor;


public class Auroch extends Animal {

    public Auroch(float x, float y, Potamos.Sex gender) {
        super(x,y,gender);
        if(sex == Potamos.Sex.FEMALE) {
            idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_female_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("auroch_female_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("auroch_female_dead").animList.get(0);
            age = WorldHandler.getInstance().stateTime- Potamos.AUROCH_ADOLECENCE_TIME-rand.nextInt(100);
        }
        else if(sex == Potamos.Sex.MALE){
            idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_male_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("auroch_male_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("auroch_male_dead").animList.get(0);
            age = WorldHandler.getInstance().stateTime- Potamos.AUROCH_ADOLECENCE_TIME-rand.nextInt(100);
        }
        else if(sex == Potamos.Sex.CHILD){
            idle = AnimalAnims.getInstance().animsMap.get("calf_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("calf_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("calf_move").animList.get(0);
            voice = AnimalAnims.getInstance().animsMap.get("calf_voice").animList.get(0);
            dead = AnimalAnims.getInstance().animsMap.get("calf_dead").animList.get(0);
            age = WorldHandler.getInstance().stateTime;
            Gdx.app.log("CALF","CALF");
        }
        meal = Potamos.AUROCH_MEAL_SIZE;
        speakFile = "audio/sounds/mooo.mp3";
        speakFile2 = "audio/sounds/mooo2.mp3";
        setBounds(x,y,idle.getKeyFrame(0).getRegionWidth(),idle.getKeyFrame(0).getRegionHeight());
        //setSize(idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4);
        reg = idle.getKeyFrame(0);
        setOrigin(idle.getKeyFrame(0).getRegionWidth()/2,idle.getKeyFrame(0).getRegionHeight()/2);
        define(Potamos.AUROCH_BIT);
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
    protected void drawAge(Batch batch){
        float curAge = WorldHandler.getInstance().stateTime-age;
        if(curAge<Potamos.AUROCH_LIFE_TIME/5f){
            GameScreen.getHud().hudfont.draw(batch,"juvenile",getX()+5,getY()-110);
        }
        else if(curAge<Potamos.AUROCH_LIFE_TIME/5f*2){
            GameScreen.getHud().hudfont.draw(batch,"young",getX()+5,getY()-110);
        }
        else if(curAge<Potamos.AUROCH_LIFE_TIME/5f*3){
            GameScreen.getHud().hudfont.draw(batch,"middle-aged",getX()+5,getY()-110);
        }
        else if(curAge<Potamos.AUROCH_LIFE_TIME/5f*4){
            GameScreen.getHud().hudfont.draw(batch,"old",getX()+5,getY()-110);
        }
        else{
            GameScreen.getHud().hudfont.draw(batch,"venerable",getX()+5,getY()-110);
        }
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
        b2body.getFixtureList().get(0).getFilterData().categoryBits = Potamos.DEAD_AUROCH_BIT;
    }
    public void dispose () {
        super.dispose();
    }
}
