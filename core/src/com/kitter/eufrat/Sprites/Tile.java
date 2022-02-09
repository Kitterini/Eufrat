package com.kitter.eufrat.Sprites;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.kitter.eufrat.screens.GameScreen;
import com.kitter.eufrat.tools.TileAnims;
import com.kitter.eufrat.Eufrat;
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

    public Tile(String type, Vector2 position) {

        // find out if the river is vertical or horizontal

        int chosen = (int)(Math.random() * ((TileAnims.getInstance().animsMap.get(type).animList.size() )));
        this.anim = TileAnims.getInstance().animsMap.get(type).animList.get(chosen);
        this.type = type;
        this.world = GameScreen.getWorld();
        this.position = position;
        this.size = new Vector2(Eufrat.PPM * TileAnims.getInstance().animsMap.get(type).sizeX,
                                Eufrat.PPM * TileAnims.getInstance().animsMap.get(type).sizeY);
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

    public void defineTile(){
        //body section
        BodyDef bdef = new BodyDef();
        bdef.position.set(new Vector2(getX(),getY()));
        bdef.type = BodyDef.BodyType.StaticBody;
        b2body = world.createBody(bdef);
        FixtureDef fdef = new FixtureDef();
        fdef.filter.categoryBits = Eufrat.TILE_BIT;
        fdef.filter.maskBits = Eufrat.AUROCH_BIT;

        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(0,0);
        vertices[1] = new Vector2(Eufrat.PPM,0);
        vertices[2] = new Vector2(Eufrat.PPM, Eufrat.PPM);
        vertices[3] = new Vector2(0, Eufrat.PPM);
        shape.set(vertices);

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
    }
}
