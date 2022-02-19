package com.kitter.eufrat.Sprites;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.MouseInputProcessor;


public class Auroch extends Animal {
    MouseInputProcessor mouse;
    public Auroch(float x, float y, Potamos.Sex gender) {
        super(x,y,gender);
        if(sex == Potamos.Sex.FEMALE) {
            idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_female_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
            age = WorldHandler.getInstance().stateTime- Potamos.AUROCH_ADOLECENCE_TIME-rand.nextInt(100);
        }
        else if(sex == Potamos.Sex.MALE){
            idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("auroch_male_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
            age = WorldHandler.getInstance().stateTime- Potamos.AUROCH_ADOLECENCE_TIME-rand.nextInt(100);
        }
        else if(sex == Potamos.Sex.CHILD){
            idle = AnimalAnims.getInstance().animsMap.get("calf_idle").animList.get(0);
            eating = AnimalAnims.getInstance().animsMap.get("calf_eating").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("calf_move").animList.get(0);
            age = WorldHandler.getInstance().stateTime;
            Gdx.app.log("CALF","CALF");
        }
        meal = Potamos.AUROCH_MEAL;
        speakFile = "audio/sounds/mooo.mp3";
        speakFile2 = "audio/sounds/mooo2.mp3";
        setBounds(x,y,idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4);
        //setSize(idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4);
        reg = idle.getKeyFrame(0);
        setOrigin(idle.getKeyFrame(0).getRegionWidth()/2,idle.getKeyFrame(0).getRegionHeight()/2);
        define(Potamos.AUROCH_BIT);
        mouse = new MouseInputProcessor() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 vec=new Vector3(screenX,screenY,0);
                GameScreen.getGameCam().unproject(vec);
                if(getX()<vec.x && getY()<vec.y && getX()+getWidth()>vec.x && getY()+getHeight()>vec.y){
                    Gdx.app.log("CLICK","ON THE COW");
                    highlighted = !highlighted;
                    if(highlighted){
                        WorldHandler.animalsHighlighted.add(Auroch.this);
                    }
                    else{
                        WorldHandler.animalsHighlighted.remove(Auroch.this);
                    }
                }
                return false;
            }
        };
        GameScreen.getImultiplexer().addProcessor(mouse);
        Gdx.input.setInputProcessor(GameScreen.getImultiplexer());
    }

    @Override
    protected void scanForFood() {
        Vector2 currentPos = new Vector2 ((int) (getX()/ Potamos.PPM),(int) (getY()/ Potamos.PPM));
        int flucX = 0;
        int flucY = 0;
        boolean moveX = true;
        while(flucX < FEED_RANGE) {
            if((int) (currentPos.x + flucX) < GameScreen.MAP_SIZE &&
               (int) (currentPos.y + flucY) < GameScreen.MAP_SIZE &&
               (int) (currentPos.x + flucX) > 0 &&
               (int) (currentPos.y + flucY) > 0) {
                if ((WorldHandler.worldTiles[(int) (currentPos.x + flucX)][(int) (currentPos.y + flucY)].type == "meadow" ||
                    WorldHandler.worldTiles[(int) (currentPos.x + flucX)][(int) (currentPos.y + flucY)].type == "fertile") &&
                        WorldHandler.worldTiles[(int) (currentPos.x + flucX)][(int) (currentPos.y + flucY)].capacity>0) {
                    foodFound = true;
                    foodPos = new Vector2((currentPos.x + flucX) * Potamos.PPM + Potamos.PPM / 2,
                            (currentPos.y + flucY) * Potamos.PPM + Potamos.PPM / 2);
                    return;
                }
            }
            if (moveX) {
                if (flucX > 0) {
                    flucX = -(flucX);
                } else {
                    flucX = -(flucX - 1);

                }
            } else {
                if (flucY > 0) {
                    flucY = -(flucY);
                } else {
                    flucY = -(flucY - 1);
                }
            }
            moveX = !moveX;
        }
    }

    public void dispose () {
        super.dispose();
        GameScreen.getImultiplexer().removeProcessor(mouse);
    }
}
