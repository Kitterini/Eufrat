package com.kitter.eufrat.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.tools.Config;
import com.kitter.eufrat.tools.LangPack;

import java.util.Locale;


public class StartScreen implements Screen {
    boolean loading = false;
    boolean start = false;
    private Viewport viewport;
    private Stage stage;

    private Potamos game;
    TextField seed;
    TextField size;
    public StartScreen(Potamos game) {
        this.game = game;
        viewport = new FitViewport(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height,new OrthographicCamera());
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(this.stage);

        setupStartMenu();

        Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
        Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        Gdx.graphics.setCursor(cursor);
        pixmap.dispose();
    }

    @Override
    public void show() {

    }
    public void handleGameStart(){
        if(start){
            int gameSize=Integer.parseInt(size.getText());
            if(gameSize<10){
                gameSize=10;
            }
            game.setScreen(new GameScreen(game,
                    Integer.parseInt(seed.getText()),
                    gameSize));
            dispose();
        }
    }
    @Override
    public void render(float delta) {
        handleGameStart();
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();

        stage.draw();

        if(loading){
            start = true;
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
    private void setupButton(final TextButton tbutton) {
        tbutton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                tbutton.getLabel().setColor(0,1,0,1);
                String name = tbutton.getText().toString();
                if(name.equals(LangPack.data.get("NEW_GAME"))){
                    dispose();
                    setupGameMenu();
                } else if (name.equals(LangPack.data.get("START"))){
                    dispose();
                    setupLoading();
                } else if (name.equals(LangPack.data.get("LANGUAGE") + ": " + Potamos.lang)){
                    if(Potamos.lang.equals("polski")){
                        Potamos.lang = "english";
                        String langFile = "langpack_" + Potamos.lang + ".yaml";
                        LangPack.reinit(langFile);
                    } else if(Potamos.lang.equals("english")){
                        Potamos.lang = "polski";
                        String langFile = "langpack_" + Potamos.lang + ".yaml";
                        LangPack.reinit(langFile);
                    }
                    if(Potamos.screen_mode.equals("Fullscreen on")) {
                        Potamos.screen_mode = LangPack.data.get("FULLSCREEN_ON").toString();
                    }
                    else {
                        Potamos.screen_mode = LangPack.data.get("FULLSCREEN_OFF").toString();
                    }
                    if(Potamos.debug_mode.equals("Debug on")) {
                        Potamos.debug_mode = LangPack.data.get("DEBUG_ON").toString();
                    }
                    else {
                        Potamos.debug_mode = LangPack.data.get("DEBUG_OFF").toString();
                    }
                    dispose();
                    setupSettingsMenu();
                } else if (name.equals(LangPack.data.get("SETTINGS"))){
                    //no new screen should be made, but rather this existing one reinitialized
                    dispose();
                    setupSettingsMenu();
                } else if (name.equals(LangPack.data.get("FULLSCREEN_ON")) || name.equals(LangPack.data.get("FULLSCREEN_OFF")) ){
                    setupFullScreen(name);
                    setupSettingsMenu();
                } else if (name.equals(LangPack.data.get("DEBUG_OFF"))){
                    Potamos.debug_mode = LangPack.data.get("DEBUG_ON").toString();
                    dispose();
                    setupSettingsMenu();
                } else if (name.equals(LangPack.data.get("DEBUG_ON"))){
                    Potamos.debug_mode = LangPack.data.get("DEBUG_OFF").toString();
                    dispose();
                    setupSettingsMenu();
                }  else if (name.equals(LangPack.data.get("EXIT"))){
                    Gdx.app.exit();
                    dispose();
                    System.exit(0);
                } else if (name.equals(LangPack.data.get("BACK"))){
                    dispose();
                    setupStartMenu();
                }
//                switch (name) { // zamienic na ify
//                    case "New Game":
//                        dispose();
//                        setupGameMenu();
//                        break;
//                    case "Start":
//                        dispose();
//                        setupLoading();
//                        break;
//                    case "Settings":
//                        //no new screen should be made, but rather this existing one reinitialized
//                        dispose();
//                        setupSettingsMenu();
//                        break;
//                    case "Fullscreen on":
//                    case "Fullscreen off":
//                        setupFullScreen(name);
//                        setupSettingsMenu();
//                        break;
//                    case "Debug off":
//                        Potamos.debug_mode = "Debug on";
//                        dispose();
//                        setupSettingsMenu();
//                        break;
//                    case "Debug on":
//                        Potamos.debug_mode = "Debug off";
//                        dispose();
//                        setupSettingsMenu();
//                        break;
//                    case "Exit":
//                        Gdx.app.exit();
//                        dispose();
//                        System.exit(0);
//                        break;
//                    case "Back":
//                        dispose();
//                        setupStartMenu();
//                }
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                tbutton.getLabel().setColor(0,0.4f,1f,1);
                Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor_clicked.png"));
                Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
                Gdx.graphics.setCursor(cursor);
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                tbutton.getLabel().setColor(1,1,1,1);
                Pixmap pixmap = new Pixmap(Gdx.files.internal("cursor.png"));
                Cursor cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
                Gdx.graphics.setCursor(cursor);
                super.exit(event, x, y, pointer, toActor);
            }
        });
    }
    void setupFullScreen(String mode){
        if(mode.equals(LangPack.data.get("FULLSCREEN_ON"))) {
            Gdx.graphics.setWindowedMode(1280, 720);
            Potamos.screen_mode = LangPack.data.get("FULLSCREEN_OFF").toString();
            dispose();
            viewport = new FitViewport(1280, 720, new OrthographicCamera());
            stage = new Stage(viewport, game.batch);
            Gdx.input.setInputProcessor(stage);
        }
        else if(mode.equals( LangPack.data.get("FULLSCREEN_OFF"))){
            // set resolution to HD ready (1280 x 720) and set full-screen to true
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            Potamos.screen_mode =  LangPack.data.get("FULLSCREEN_ON").toString();
            dispose();
            viewport = new FitViewport(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height,new OrthographicCamera());
            stage = new Stage(viewport, game.batch);
            Gdx.input.setInputProcessor(stage);
        }
    }
    void setupStartMenu(){
        BitmapFont font = loadCustomFont(Potamos.font,1);
        BitmapFont font2 = loadCustomFont(Potamos.font2,2);
        Label.LabelStyle fontStyle = new Label.LabelStyle(font2, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.font.getData().setScale(0.9f);
        Table table = new Table();
        table.center();
        table.setFillParent(true);
        String[] gameName = Potamos.class.getName().split("\\.");
        Label titleLabel = new Label(gameName[gameName.length-1], fontStyle);

        TextButton playButton = new TextButton(LangPack.data.get("NEW_GAME").toString(), btnStyle);
        TextButton settingsButton = new TextButton(LangPack.data.get("SETTINGS").toString(), btnStyle);
        TextButton exitButton = new TextButton(LangPack.data.get("EXIT").toString(), btnStyle);

        setupButton(playButton);
        setupButton(settingsButton);
        setupButton(exitButton);

        table.add(titleLabel).expandX();
        table.row();
        table.add(playButton).expandX().padTop(20f);
        table.row();
        table.add(settingsButton).expandX().padTop(20f);
        table.row();
        table.add(exitButton).expandX().padTop(20f);
        stage.addActor(table);
    }
    void setupSettingsMenu(){
        BitmapFont font15 = loadCustomFont(game.font,1);

        Label.LabelStyle font = new Label.LabelStyle(font15, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font15;

        Table table = new Table();
        table.center();
        table.setFillParent(true);
        Label titleLabel = new Label(LangPack.data.get("SETTINGS").toString().toUpperCase(Locale.ROOT), font);
        // Label resLabel = new Label("Resolution", font);
        TextButton backButton = new TextButton(LangPack.data.get("BACK").toString(), btnStyle);
        setupButton(backButton);
        TextButton langButton = new TextButton(LangPack.data.get("LANGUAGE").toString() + ": " + Potamos.lang, btnStyle);
        setupButton(langButton);
        TextButton fullButton = new TextButton(Potamos.screen_mode, btnStyle);
        setupButton(fullButton);
        TextButton debugButton = new TextButton(Potamos.debug_mode, btnStyle);
        setupButton(debugButton);
        Skin uiSkin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        uiSkin.getFont("default-font").getData().setScale(0.5f);
        uiSkin.get(SelectBox.SelectBoxStyle.class).font = font15;
        uiSkin.get(SelectBox.SelectBoxStyle.class).listStyle.font = font15;


        //selectBox = new SelectBox(uiSkin);
        //selectBox.setItems("1920x1080","1024x768","800x600");

        table.add(titleLabel).colspan(2).expandX();
        table.row();
        //table.add(resLabel);
        //table.add(selectBox);
        //table.row();
        table.add(langButton).colspan(2).expandX().padTop(20f);
        table.row();
        table.add(fullButton).colspan(2).expandX().padTop(20f);
        table.row();
        table.add(debugButton).colspan(2).expandX().padTop(20f);
        table.row();
        table.add(backButton).expandX().colspan(2).padTop(20f);
        stage.addActor(table);
        //resChoice = selectBox.getSelected().toString();
    }
    void setupGameMenu() {
        BitmapFont font15 = loadCustomFont(Potamos.font,1);

        Label.LabelStyle font = new Label.LabelStyle(font15, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font15;

        Skin uiSkin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        //json for skin has fields for every class, so change the one needed
        uiSkin.get(TextField.TextFieldStyle.class).font = font15;

        Table outerTable = new Table();
        Table innerTable = new Table();
        ScrollPane scrollPane = new ScrollPane(innerTable);
        outerTable.center();
        outerTable.setFillParent(true);
        Label titleLabel = new Label(LangPack.data.get("GAME_OPTIONS").toString(), font);
        TextButton playButton = new TextButton(LangPack.data.get("START").toString(), btnStyle);
        setupButton(playButton);
        Label seedLabel = new Label(LangPack.data.get("SEED").toString(), font);
        seed = new TextField(Potamos.default_seed, uiSkin);
        seed.setMaxLength(8);
        Label sizeLabel = new Label(LangPack.data.get("SIZE").toString(), font);
        size = new TextField(Potamos.default_map_size, uiSkin);
        size.setMaxLength(3);
        seed.setSize(1000,100);
        TextButton backButton = new TextButton(LangPack.data.get("BACK").toString(), btnStyle);
        setupButton(backButton);
        outerTable.add(titleLabel).colspan(2).expandX();
        outerTable.row();
        innerTable.add(playButton).colspan(2).expandX().padTop(20f);
        innerTable.row();
        innerTable.add(seedLabel).expandX().padTop(20f);
        innerTable.add(seed).padLeft(Gdx.graphics.getWidth()/20f).padRight(Gdx.graphics.getWidth()/20f).padTop(20f).width(Gdx.graphics.getWidth()/2f);
        innerTable.row();
        innerTable.add(sizeLabel).expandX().padTop(20f);
        innerTable.add(size).padLeft(Gdx.graphics.getWidth()/20f).padRight(Gdx.graphics.getWidth()/20f).padTop(20f).width(Gdx.graphics.getWidth()/2f);
        innerTable.row();
        innerTable.add(backButton).expandX().colspan(2).padTop(20f);
        innerTable.row();

        outerTable.add(scrollPane).expandX();
        stage.addActor(outerTable);
    }
    void setupLoading() {
        BitmapFont font15 = loadCustomFont(game.font,1);

        Label.LabelStyle font = new Label.LabelStyle(font15, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font15;

        Table table = new Table();
        table.center();
        table.setFillParent(true);
        Label loadingLabel = new Label(LangPack.data.get("LOADING").toString() + "...", font);
        table.add(loadingLabel).expandX();

        stage.addActor(table);
        loading = true;
    }
    public static BitmapFont loadCustomFont(String fileName,float size){
        AssetManager manager=new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        params.fontFileName = fileName;    // path of .TTF file where that exist
        params.fontParameters.size = (int)(Gdx.graphics.getHeight() * size/15);
        manager.load(fileName, BitmapFont.class, params);   // fileName with extension, sameName will use to get from manager

        manager.finishLoading();  //or use update() inside render() method

        return manager.get(fileName,BitmapFont.class);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

