package com.kitter.eufrat.Sprites;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.kitter.eufrat.Eufrat;
import com.kitter.eufrat.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.AnimalAnims;
import com.kitter.eufrat.tools.MouseInputProcessor;


public class Auroch extends Animal {

    public Auroch(float x, float y, Eufrat.Sex gender) {
        super(x,y,gender);
        if(sex == Eufrat.Sex.FEMALE) {
            idle = AnimalAnims.getInstance().animsMap.get("auroch_female_idle").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_female_move").animList.get(0);
            age = WorldHandler.getInstance().stateTime- Eufrat.AUROCH_ADOLECENCE_TIME-rand.nextInt(100);
        }
        else if(sex == Eufrat.Sex.MALE){
            idle = AnimalAnims.getInstance().animsMap.get("auroch_male_idle").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("auroch_male_move").animList.get(0);
            age = WorldHandler.getInstance().stateTime- Eufrat.AUROCH_ADOLECENCE_TIME-rand.nextInt(100);
        }
        else if(sex == Eufrat.Sex.CHILD){
            idle = AnimalAnims.getInstance().animsMap.get("calf_idle").animList.get(0);
            walking = AnimalAnims.getInstance().animsMap.get("calf_move").animList.get(0);
            age = WorldHandler.getInstance().stateTime;
            Gdx.app.log("CALF","CALF");
        }
        speakFile = "audio/sounds/mooo.mp3";
        speakFile2 = "audio/sounds/mooo2.mp3";
        setBounds(x,y,idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4);
        //setSize(idle.getKeyFrame(0).getRegionWidth()*4,idle.getKeyFrame(0).getRegionHeight()*4);
        reg = idle.getKeyFrame(0);
        setOrigin(idle.getKeyFrame(0).getRegionWidth()/2,idle.getKeyFrame(0).getRegionHeight()/2);
        define(Eufrat.AUROCH_BIT);
        MouseInputProcessor mouse = new MouseInputProcessor() {
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

}
