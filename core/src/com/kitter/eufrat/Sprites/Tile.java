package com.kitter.eufrat.Sprites;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.TileAnims;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.WorldHandler;

public class Tile extends Sprite {
    public Vector2 position;
    public Vector2 size;
    public String type;
    public World world;
    Animation<TextureRegion> anim;
    TextureRegion reg;
    boolean flipped;
    public Body b2body;
    public int capacity;

    public Tile(String type, Vector2 position) {

        // find out if the river is vertical or horizontal
        int chosen = (int)(Math.random() * ((TileAnims.getInstance().animsMap.get(type).animList.size() )));
        this.anim = TileAnims.getInstance().animsMap.get(type).animList.get(chosen);
        this.type = type;
        this.world = GameScreen.getWorld();
        this.position = position;
        this.size = new Vector2(Potamos.PPM * TileAnims.getInstance().animsMap.get(type).sizeX,
                                Potamos.PPM * TileAnims.getInstance().animsMap.get(type).sizeY);
        capacity=100;
        //setSize(size.x, size.y);
        setBounds(position.x, position.y,size.x, size.y);
        ///setPosition(position.x, position.y);

        flipped = false;
        reg = this.anim.getKeyFrame(0);
    }
    @Override
    public void draw(Batch batch, float delta) {
        //super.draw(batch, 1);

        reg = anim.getKeyFrame(WorldHandler.getInstance().stateTime, true);
        if(flipped && !reg.isFlipX()){
            reg.flip(true,false);
        }
        else if(!flipped && reg.isFlipX()){
            reg.flip(true,false);
        }
        batch.draw(reg,getX(),getY(),getWidth()/2,getHeight()/2,getWidth(),getHeight(),getScaleX(),getScaleY(),getRotation());
    }
    public void flipTile(){
        flipped = !flipped;
    }

    public int decreaseCapacity(){
        if(capacity>Potamos.AUROCH_MEAL){
            capacity-=Potamos.AUROCH_MEAL;
            Gdx.app.log("EATING", String.valueOf(capacity));
            int index = (int)(capacity/100f*TileAnims.getInstance().animsMap.get(type).depletionList.size());
            this.anim = TileAnims.getInstance().animsMap.get(type).depletionList.get(index);
            return Potamos.AUROCH_MEAL;
        }
        else if(capacity>0){
            int foodleft = capacity;
            capacity-=capacity;
            int index = (int)(capacity/100f*TileAnims.getInstance().animsMap.get(type).depletionList.size());
            this.anim = TileAnims.getInstance().animsMap.get(type).depletionList.get(index);
            Gdx.app.log("EATING", String.valueOf(capacity));
            return foodleft;
        }
        else {
            Gdx.app.log("ERROR", "NO FOOD");
            return 0;
        }
    }
    public void defineTile(){
        //body section
        BodyDef bdef = new BodyDef();
        bdef.position.set(new Vector2(getX(),getY()));
        bdef.type = BodyDef.BodyType.StaticBody;
        b2body = world.createBody(bdef);
        FixtureDef fdef = new FixtureDef();
        fdef.filter.categoryBits = Potamos.TILE_BIT;
        fdef.filter.maskBits = Potamos.AUROCH_BIT;

        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(0,0);
        vertices[1] = new Vector2(Potamos.PPM,0);
        vertices[2] = new Vector2(Potamos.PPM, Potamos.PPM);
        vertices[3] = new Vector2(0, Potamos.PPM);
        shape.set(vertices);

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
    }
}
