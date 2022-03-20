package com.kitter.eufrat.tools;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.Sprites.Animal;
import com.kitter.eufrat.Sprites.Auroch;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
        switch (cDef) {
            case Potamos.TILE_BIT | Potamos.AUROCH_BIT:
                break;
            case Potamos.AUROCH_BIT:
                if(((Auroch)fixA.getUserData()).sex == Potamos.Sex.MALE &&
                    ((Auroch)fixB.getUserData()).sex == Potamos.Sex.FEMALE ){
                    if (((Auroch) fixB.getUserData()).currentState != Animal.State.PREGNANT){
                        ((Auroch) fixB.getUserData()).setPregnant();
                    }
                }
                else if(((Auroch)fixA.getUserData()).sex == Potamos.Sex.FEMALE &&
                    ((Auroch)fixB.getUserData()).sex == Potamos.Sex.MALE ){
                    if (((Auroch) fixA.getUserData()).currentState != Animal.State.PREGNANT){
                        ((Auroch) fixA.getUserData()).setPregnant();
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
