package com.kitter.eufrat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kitter.eufrat.Eufrat;
import com.kitter.eufrat.Sprites.Auroch;
import com.kitter.eufrat.tools.*;
import com.kitter.eufrat.WorldHandler;

public class GameScreen implements Screen {
    private static World world;
    private final float scrollPower = 10; // the bigger the slower
    private float ratio;
    public static int MAP_SIZE;
    public static int ANIMAL_AMOUNT ;//= 1;
    private Hud hud;
    private Eufrat game;
    private Box2DDebugRenderer b2dr;

    private static OrthographicCamera gameCam;
    private Viewport gamePort;
    private static MouseInputProcessor mouse;
    private CameraController controller;
    private GestureDetector touch;
    private static InputMultiplexer imultiplexer;
    private float sortAnimalsStateTime;

    // how about the stage
    public GameScreen(Eufrat game, int seed, int size) {
        MAP_SIZE = size;
        ANIMAL_AMOUNT = MAP_SIZE;
        ratio = (float)Gdx.graphics.getWidth()/(float)Gdx.graphics.getHeight();
        Gdx.app.log("RATIO", String.valueOf(ratio));
        WorldHandler.getInstance().setSeed(seed);
        Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
        Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        Gdx.graphics.setCursor(cursor);
        this.game = game;
        mouse = new MouseInputProcessor(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor_clicked.png"));
                Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
                Gdx.graphics.setCursor(cursor);
                Gdx.app.log("CLICK","Trzymam");
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
                Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
                Gdx.graphics.setCursor(cursor);
                Gdx.app.log("CLICK","Puszczam");
                return false;
            }
        };
        controller = new CameraController();
        touch = new GestureDetector(20, 40, 0.5f, 2, 0.15f, controller);
        imultiplexer = new InputMultiplexer();
        imultiplexer.addProcessor(mouse);
        imultiplexer.addProcessor(touch);
        Gdx.input.setInputProcessor(imultiplexer);

        world = new World(new Vector2(0,0), true);
        world.setContactListener(new WorldContactListener());
        b2dr = new Box2DDebugRenderer();
        b2dr.SHAPE_STATIC.set(1,0,0,1);
        b2dr.SHAPE_AWAKE.set(0,0,1,1);
        b2dr.SHAPE_NOT_AWAKE.set(0,0,1,1);
        gameCam = new OrthographicCamera();
        gamePort = new FitViewport(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height, gameCam);
        gameCam.position.set(gameCam.viewportWidth, gameCam.viewportHeight, 0);
        gameCam.update();
        // set camera to the middle of the world
        gameCam.translate(Eufrat.PPM * MAP_SIZE /2, Eufrat.PPM* MAP_SIZE /2);
        WorldHandler.getInstance().initWorld();
        WorldHandler.getInstance().setVisibility(gameCam.position);
        hud = new Hud(game);
        sortAnimalsStateTime=0;
    }

    @Override
    public void show() {

    }

    public void update(float dt){
        handleSpawningAnimals();
        controller.update();
        gameCam.update();
        hud.update(dt);
    }

    public void handleSpawningAnimals(){
        if(!WorldHandler.animalsToSpawn.isEmpty()){
            Gdx.app.log("Spawn", "spawning");
            AnimalDef adef = WorldHandler.animalsToSpawn.poll();
            if(adef.type == Auroch.class){
                WorldHandler.getInstance().addAuroch(adef.position.x, adef.position.y, adef.sex);
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        handleInput(delta);
        if(WorldHandler.getInstance().stateTime > sortAnimalsStateTime + 0.2){
            WorldHandler.getInstance().sortAnimals();
            sortAnimalsStateTime = WorldHandler.getInstance().stateTime;
        }

        world.step(1/60f, 2,2);
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(gameCam.combined);
        game.batch.begin();
        WorldHandler.getInstance().draw(game.batch, delta);
        game.batch.end();
        // set batch to draw what the Hud camera sees
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
        if(Eufrat.debug_mode.equals("Debug on")) {
            b2dr.render(world, gameCam.combined);
        }

    }
    public void handleInput(float dt) {
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveY(1f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S )){
            moveY(-1f);

        }
        if(Gdx.input.isKeyPressed(Input.Keys.D) ){
            moveX(1f);

        }
        if(Gdx.input.isKeyPressed(Input.Keys.A) ){
            moveX(-1f);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.Q) || mouse.getScroll() == -1){
            zoom(-1);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.E) || mouse.getScroll() == 1){
            //if(worldScale < 40) {
                zoom(1);
            //}
        }
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            System.exit(0);
        }

    }

    private void moveX(float direction){
        if(gameCam.position.x >= 0 && gameCam.position.x <= Eufrat.PPM * MAP_SIZE) {
            gameCam.translate(direction * gameCam.zoom*20*ratio,0);
        }
        else if(gameCam.position.x < 0){
            gameCam.position.x=0;
        }
        else if(gameCam.position.x > Eufrat.PPM * MAP_SIZE){
            gameCam.position.x = Eufrat.PPM * MAP_SIZE;
        }
        WorldHandler.getInstance().setVisibility(gameCam.position);
    }

    private void moveY(float direction){
        if(gameCam.position.y >= 0 && gameCam.position.y <= Eufrat.PPM* MAP_SIZE) {
            gameCam.translate(0, direction * gameCam.zoom*20);
        }
        else if(gameCam.position.y < 0){
            gameCam.position.y=0;
        }
        else if(gameCam.position.y > Eufrat.PPM * MAP_SIZE){
            gameCam.position.y = Eufrat.PPM* MAP_SIZE;
        }
        WorldHandler.getInstance().setVisibility(gameCam.position);
    }

    private void zoom(float direction){
        if(direction > 0) {
            gameCam.zoom = (gameCam.zoom*(1+(direction/scrollPower)));
        }
       else if(direction < 0) {
            gameCam.zoom = (gameCam.zoom*(1+(direction/scrollPower)));
        }
        WorldHandler.getInstance().setVisibility(gameCam.position);
        mouse.stopScroll();
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width,height);
    }

    public static World getWorld(){
        return world;
    }
    public static InputMultiplexer getImultiplexer(){
        return imultiplexer;
    }
    public static OrthographicCamera getGameCam(){ return gameCam; }
    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        b2dr.dispose();
        world.dispose();
        hud.dispose();
    }
}
