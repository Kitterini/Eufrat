package com.kitter.eufrat.tools;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.Sprites.Auroch;
import com.kitter.eufrat.Sprites.Simbakubwa;
import com.kitter.eufrat.screens.GameScreen;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
        switch (cDef) {
            case Potamos.TILE_BIT | Potamos.AUROCH_BIT:
            case Potamos.WATER_BIT | Potamos.AUROCH_BIT:
                break;
            case Potamos.AUROCH_BIT:
                if(((Auroch)fixA.getUserData()).sex == Potamos.Sex.MALE &&
                    ((Auroch)fixB.getUserData()).sex == Potamos.Sex.FEMALE ){
                    if (((Auroch) fixB.getUserData()).currentState != Potamos.State.PREGNANT &&
                        ((Auroch) fixB.getUserData()).conceiveTime + 100 < WorldHandler.getInstance().stateTime &&
                        WorldHandler.animals.size()<Potamos.ANIMAL_MAX_CAPACITY){
                        ((Auroch) fixB.getUserData()).setPregnant();
                        ((Auroch) fixA.getUserData()).currentState = Potamos.State.IDLE;
                    }
                }
                else if(((Auroch)fixA.getUserData()).sex == Potamos.Sex.FEMALE &&
                    ((Auroch)fixB.getUserData()).sex == Potamos.Sex.MALE ){
                    if (((Auroch) fixA.getUserData()).currentState != Potamos.State.PREGNANT &&
                        ((Auroch) fixA.getUserData()).conceiveTime + 100 < WorldHandler.getInstance().stateTime &&
                        WorldHandler.animals.size()<Potamos.ANIMAL_MAX_CAPACITY){
                        ((Auroch) fixA.getUserData()).setPregnant();
                        ((Auroch) fixB.getUserData()).currentState = Potamos.State.IDLE;
                    }
                }
                else if(((Auroch)fixA.getUserData()).sex == Potamos.Sex.MALE &&
                        ((Auroch)fixB.getUserData()).sex == Potamos.Sex.MALE) {
                  //  Gdx.app.log("CONTACT", "GAY SEX! ");
                }
                else if(((Auroch)fixA.getUserData()).sex == Potamos.Sex.FEMALE &&
                        ((Auroch)fixB.getUserData()).sex == Potamos.Sex.FEMALE){
                       // Gdx.app.log("CONTACT","LESBIAN SEX! ");
                }
                else{
                        //Gdx.app.log("CONTACT","CHILD DO NOT TOUCH PEDO! ");
                }


                //Gdx.app.log("CONTACT", "AUROCH-AUROCH CONTACT!");
                break;
            case Potamos.SIMBA_BIT:
                if(((Simbakubwa)fixA.getUserData()).sex == Potamos.Sex.MALE &&
                        ((Simbakubwa)fixB.getUserData()).sex == Potamos.Sex.FEMALE ){
                    Gdx.app.log("CONTACT", "SIMBA TRIED TO COPULATE! ");
                }
                else if(((Simbakubwa)fixA.getUserData()).sex == Potamos.Sex.FEMALE &&
                        ((Simbakubwa)fixB.getUserData()).sex == Potamos.Sex.MALE ){
                    Gdx.app.log("CONTACT", "SIMBA TRIED TO COPULATE! ");
                }
                else if(((Simbakubwa)fixA.getUserData()).sex == Potamos.Sex.MALE &&
                        ((Simbakubwa)fixB.getUserData()).sex == Potamos.Sex.MALE) {
                    //  Gdx.app.log("CONTACT", "GAY SEX! ");
                }
                else if(((Simbakubwa)fixA.getUserData()).sex == Potamos.Sex.FEMALE &&
                        ((Simbakubwa)fixB.getUserData()).sex == Potamos.Sex.FEMALE){
                    // Gdx.app.log("CONTACT","LESBIAN SEX! ");
                }
                else{
                    //Gdx.app.log("CONTACT","CHILD DO NOT TOUCH PEDO! ");
                }

                //Gdx.app.log("CONTACT", "AUROCH-AUROCH CONTACT!");

            case Potamos.TILE_BIT | Potamos.SIMBA_BIT:
                break;
            case Potamos.WATER_BIT | Potamos.SIMBA_BIT:
                Gdx.app.log("CONTACT", "SIMBA ON WATER");
                break;
            case Potamos.AUROCH_BIT | Potamos.SIMBA_BIT:
                Gdx.app.log("CONTACT", "SIMBA MOOER");
                if(fixA.getFilterData().categoryBits == Potamos.SIMBA_BIT){
                  if (((Simbakubwa) fixA.getUserData()).currentState != Potamos.State.SLEEP){
                        ((Simbakubwa) fixA.getUserData()).timeToEat = true;
                        ((Auroch) fixB.getUserData()).vanquished = true;
                    }
                }
                else {
                    if(((Simbakubwa) fixB.getUserData()).currentState != Potamos.State.SLEEP) {
                        ((Auroch) fixA.getUserData()).vanquished = true;
                        ((Simbakubwa)fixB.getUserData()).timeToEat = true;
                    }
                }
                break;
            case Potamos.DEAD_AUROCH_BIT | Potamos.SIMBA_BIT:
                if(fixA.getFilterData().categoryBits == Potamos.SIMBA_BIT &&
                   ((Auroch) fixB.getUserData()).deathTime < WorldHandler.getInstance().stateTime - 10 &&
                   ((Simbakubwa) fixA.getUserData()).currentState != Potamos.State.SLEEP) {
                    ((Simbakubwa) fixA.getUserData()).timeToEat = true;
                    ((Auroch) fixB.getUserData()).deathTime = -100;
                }
                else if(fixB.getFilterData().categoryBits == Potamos.SIMBA_BIT &&
                        ((Auroch) fixA.getUserData()).deathTime < WorldHandler.getInstance().stateTime - 10 &&
                        ((Simbakubwa) fixB.getUserData()).currentState != Potamos.State.SLEEP){
                    ((Auroch) fixA.getUserData()).deathTime = -100;
                    ((Simbakubwa) fixB.getUserData()).timeToEat = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
