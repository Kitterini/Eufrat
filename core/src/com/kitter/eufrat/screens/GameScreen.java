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
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.Sprites.Animal;
import com.kitter.eufrat.tools.*;
import com.kitter.eufrat.WorldHandler;

public class GameScreen implements Screen {
    private static World world;
    private final float scrollPower = 10; // the bigger the slower
    private float ratio;
    private static final float STEP_TIME = 1f/60f;
    public static float GAME_SPEED = 1;
    private float accumulator = 0;
    public static int MAP_SIZE;
    public static int ANIMAL_AMOUNT ;//= 1;
    public static float VOLUME;

    private static Hud hud;
    private Potamos game;
    private Box2DDebugRenderer b2dr;

    private static OrthographicCamera gameCam;
    private Viewport gamePort;
    private static MouseInputProcessor mouse;
    private CameraController controller;
    private GestureDetector touch;
    private static InputMultiplexer imultiplexer;
    private float sortAnimalsStateTime;

    // how about the stage
    public GameScreen(Potamos game, int seed, int size) {
        MAP_SIZE = size;
        ANIMAL_AMOUNT = (int)(0.003 * MAP_SIZE*MAP_SIZE + 0.73 *MAP_SIZE - 6);
        ratio = (float)Gdx.graphics.getWidth()/(float)Gdx.graphics.getHeight();
        Gdx.app.log("RATIO", String.valueOf(ratio));
        WorldHandler.getInstance().setSeed(seed);
        Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
        Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        Gdx.graphics.setCursor(cursor);
        this.game = game;
        VOLUME = 0.15f / (float) (WorldHandler.getInstance().screenXmax - WorldHandler.getInstance().screenXmin);
        mouse = new MouseInputProcessor(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor_clicked.png"));
                Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
                Gdx.graphics.setCursor(cursor);
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
                Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
                Gdx.graphics.setCursor(cursor);
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
        gameCam.translate(Potamos.PPM * MAP_SIZE /2, Potamos.PPM* MAP_SIZE /2);
        WorldHandler.getInstance().initWorld();
        WorldHandler.getInstance().setVisibility(gameCam.position);
        hud = new Hud(game);
        sortAnimalsStateTime=0;
    }

    @Override
    public void show() {

    }

    public void update(float dt){
        WorldHandler.handleSpawningAnimals();
        controller.update();
        gameCam.update();
        hud.update(dt);
    }



    @Override
    public void render(float delta) {
        delta = delta * GAME_SPEED;
        update(delta);
        handleInput();
        if(WorldHandler.getInstance().stateTime > sortAnimalsStateTime + 0.2){
            WorldHandler.getInstance().handleAnimals();
            sortAnimalsStateTime = WorldHandler.getInstance().stateTime;
            WorldHandler.getInstance().setVisibility(gameCam.position);
        }
        stepWorld(delta);
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(gameCam.combined);
        game.batch.begin();
        WorldHandler.getInstance().draw(game.batch, delta);
        game.batch.end();
        // set batch to draw what the Hud camera sees
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
        if(Potamos.debug_mode.equals("Debug on")) {
            b2dr.render(world, gameCam.combined);
        }

    }
    private void stepWorld(float delta) {

        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;

            world.step(STEP_TIME, 2, 2);
        }
    }
    public void handleInput() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            setupFullScreen(Potamos.screen_mode);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            if(GAME_SPEED <= 8) {
                GAME_SPEED = GAME_SPEED * 2;
                Gdx.app.log("FASTER", String.valueOf(GAME_SPEED));
                WorldHandler.getInstance().adjustAnimalSpeed(2f);
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            if(GAME_SPEED >= 0.125) {
                GAME_SPEED = GAME_SPEED / 2;
                Gdx.app.log("SLOWER", String.valueOf(GAME_SPEED));
                WorldHandler.getInstance().adjustAnimalSpeed(0.5f);
            }
        }
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
        if(Gdx.input.isKeyPressed(Input.Keys.F)){
            String foods = "";
            for(Animal a : WorldHandler.animals){
                foods = foods + a.hunger + " ";
            }
            Gdx.app.log("HUNGERS",foods);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            System.exit(0);
        }

    }
    void setupFullScreen(String mode){
        if(mode.equals("Fullscreen on")) {
            Gdx.graphics.setWindowedMode(1280, 720);
            Potamos.screen_mode = "Fullscreen off";
            Gdx.input.setInputProcessor(imultiplexer);
        }
        else if(mode.equals("Fullscreen off")){
            // set resolution to HD ready (1280 x 720) and set full-screen to true
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            Potamos.screen_mode = "Fullscreen on";
            Gdx.input.setInputProcessor(imultiplexer);
        }
    }
    private void moveX(float direction){
        if(gameCam.position.x >= 0 && gameCam.position.x <= Potamos.PPM * MAP_SIZE) {
            gameCam.translate(direction * gameCam.zoom*20*ratio,0);
        }
        else if(gameCam.position.x < 0){
            gameCam.position.x=0;
        }
        else if(gameCam.position.x > Potamos.PPM * MAP_SIZE){
            gameCam.position.x = Potamos.PPM * MAP_SIZE;
        }
        WorldHandler.getInstance().setVisibility(gameCam.position);
    }

    private void moveY(float direction){
        if(gameCam.position.y >= 0 && gameCam.position.y <= Potamos.PPM* MAP_SIZE) {
            gameCam.translate(0, direction * gameCam.zoom*20);
        }
        else if(gameCam.position.y < 0){
            gameCam.position.y=0;
        }
        else if(gameCam.position.y > Potamos.PPM * MAP_SIZE){
            gameCam.position.y = Potamos.PPM* MAP_SIZE;
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
        VOLUME = 0.15f / (float) (WorldHandler.getInstance().screenXmax - WorldHandler.getInstance().screenXmin);
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
    public static Hud getHud(){return hud;}
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
