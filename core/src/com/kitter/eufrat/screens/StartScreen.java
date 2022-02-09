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

import com.kitter.eufrat.Eufrat;


public class StartScreen implements Screen {
    boolean loading = false;
    boolean start = false;
    private Viewport viewport;
    private Stage stage;

    private Eufrat game;
    TextField seed;
    TextField size;
    public StartScreen(Eufrat game) {
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
                switch (name) {
                    case "New Game":
                        dispose();
                        setupGameMenu();
                        break;
                    case "Start":
                        dispose();
                        setupLoading();
                        break;
                    case "Settings":
                        //no new screen should be made, but rather this existing one reinitialized
                        dispose();
                        setupSettingsMenu();
                        break;
                    case "Fullscreen on":
                        Gdx.graphics.setWindowedMode(1280, 720);
                        Eufrat.screen_mode = "Fullscreen off";
                        dispose();
                        viewport = new FitViewport(1280, 720,new OrthographicCamera());
                        stage = new Stage(viewport, game.batch);
                        Gdx.input.setInputProcessor(stage);
                        setupSettingsMenu();
                        break;
                    case "Fullscreen off":
                        // set resolution to HD ready (1280 x 720) and set full-screen to true
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                        Eufrat.screen_mode = "Fullscreen on";
                        dispose();
                        viewport = new FitViewport(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height,new OrthographicCamera());
                        stage = new Stage(viewport, game.batch);
                        Gdx.input.setInputProcessor(stage);
                        setupSettingsMenu();
                        break;
                    case "Debug off":
                        Eufrat.debug_mode = "Debug on";
                        dispose();
                        setupSettingsMenu();
                        break;
                    case "Debug on":
                        Eufrat.debug_mode = "Debug off";
                        dispose();
                        setupSettingsMenu();
                        break;
                    case "Exit":
                        Gdx.app.exit();
                        dispose();
                        System.exit(0);
                    case "Back":
                        dispose();
                        setupStartMenu();
                }
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

    void setupStartMenu(){
        BitmapFont font15 = loadCustomFont(Eufrat.font);

        Label.LabelStyle font = new Label.LabelStyle(font15, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font15;
        btnStyle.font.getData().setScale(0.9f);
        Table table = new Table();
        table.center();
        table.setFillParent(true);
//        Gdx.app.log("SAS",Kotas.class.getName().split("\\.")[0]);
        String[] gameName = Eufrat.class.getName().split("\\.");
        Label titleLabel = new Label(gameName[gameName.length-1], font);

        TextButton playButton = new TextButton("New Game", btnStyle);
        TextButton settingsButton = new TextButton("Settings", btnStyle);
        TextButton exitButton = new TextButton("Exit", btnStyle);

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
        BitmapFont font15 = loadCustomFont(game.font);

        Label.LabelStyle font = new Label.LabelStyle(font15, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font15;

        Table table = new Table();
        table.center();
        table.setFillParent(true);
        Label titleLabel = new Label("SETTINGS", font);
        // Label resLabel = new Label("Resolution", font);
        TextButton backButton = new TextButton("Back", btnStyle);
        setupButton(backButton);

        TextButton fullButton = new TextButton(Eufrat.screen_mode, btnStyle);
        setupButton(fullButton);
        TextButton debugButton = new TextButton(Eufrat.debug_mode, btnStyle);
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
        table.add(fullButton).colspan(2).expandX().padTop(20f);
        table.row();
        table.add(debugButton).colspan(2).expandX().padTop(20f);
        table.row();
        table.add(backButton).expandX().colspan(2).padTop(20f);
        stage.addActor(table);
        //resChoice = selectBox.getSelected().toString();
    }
    void setupGameMenu() {
        BitmapFont font15 = loadCustomFont(Eufrat.font);

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
        Label titleLabel = new Label("GAME OPTIONS", font);
        TextButton playButton = new TextButton("Start", btnStyle);
        setupButton(playButton);
        Label seedLabel = new Label("Seed", font);
        seed = new TextField(Eufrat.default_seed, uiSkin);
        seed.setMaxLength(8);
        Label sizeLabel = new Label("Size", font);
        size = new TextField(Eufrat.default_map_size, uiSkin);
        size.setMaxLength(3);
        seed.setSize(1000,100);
        TextButton backButton = new TextButton("Back", btnStyle);
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
        BitmapFont font15 = loadCustomFont(game.font);

        Label.LabelStyle font = new Label.LabelStyle(font15, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font15;

        Table table = new Table();
        table.center();
        table.setFillParent(true);
        Label loadingLabel = new Label("Loading...", font);
        table.add(loadingLabel).expandX();

        stage.addActor(table);
        loading = true;
    }
    public static BitmapFont loadCustomFont(String fileName){
        AssetManager manager=new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        params.fontFileName = fileName;    // path of .TTF file where that exist
        params.fontParameters.size = Gdx.graphics.getHeight()/15;
        manager.load(fileName, BitmapFont.class, params);   // fileName with extension, sameName will use to get from manager

        manager.finishLoading();  //or use update() inside render() method

        return manager.get(fileName,BitmapFont.class);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
