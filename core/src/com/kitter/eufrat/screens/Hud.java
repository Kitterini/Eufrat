package com.kitter.eufrat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.WorldHandler;


public class Hud implements Disposable {
    public Stage stage;
    private Viewport viewport;

    Label FPSLabel;
    Label AurochLabel;

    public Hud(Potamos game) {

        viewport = new FillViewport(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);

        BitmapFont font15 = loadCustomFont(Potamos.font);

        Table table = new Table();
        table.top();
        table.setFillParent(true);
        AurochLabel = new Label("", new Label.LabelStyle(font15, Color.WHITE));
        table.add( AurochLabel).padRight(20).colspan(2);
        FPSLabel = new Label("", new Label.LabelStyle(font15, Color.WHITE));
        table.add(FPSLabel).padRight(20);
        table.row();
        Table highlightTable = new Table();
        ScrollPane scrollPane = new ScrollPane(highlightTable);
        Label testLabel = new Label("TEST", new Label.LabelStyle(font15, Color.WHITE));
        highlightTable.add(testLabel);
        table.add(testLabel).colspan(4).right();
        //table.debug();

        stage.addActor(table);

    }

    public void update(float dt) {
        FPSLabel.setText("FPS=" + Gdx.graphics.getFramesPerSecond());
        AurochLabel.setText("Aurochs: " + WorldHandler.animals.size());
    }

    public static BitmapFont loadCustomFont(String fileName){
        AssetManager manager=new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        params.fontFileName = fileName;    // path of .TTF file where that exist
        params.fontParameters.size = Gdx.graphics.getHeight()/30;
        manager.load(fileName, BitmapFont.class, params);   // fileName with extension, sameName will use to get from manager

        manager.finishLoading();  //or use update() inside render() method

        return manager.get(fileName,BitmapFont.class);
    }
    @Override
    public void dispose() {
        stage.dispose();
    }
}
