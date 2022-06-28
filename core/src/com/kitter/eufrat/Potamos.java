package com.kitter.eufrat;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kitter.eufrat.screens.StartScreen;
import com.kitter.eufrat.tools.Config;
import com.kitter.eufrat.tools.LangPack;

import java.util.Map;


public class Potamos extends Game {
	public static final float PPM = 320/4f;
	public static final short NOTHING_BIT = 0;
	public static final short TILE_BIT = 1;
	public static final short WATER_BIT =2;
	public static final short AUROCH_BIT = 4;
	public static final short DEAD_AUROCH_BIT = 8;
	public static final short SIMBA_BIT = 16;
	public static final short DEAD_SIMBA_BIT = 32;
	public static final short ANIMALS_BIT = SIMBA_BIT | AUROCH_BIT;
	public enum Sex{MALE, FEMALE, CHILD};
	public static Map<String, Integer> AurochVal;
	public static Map<String, Integer> SimbaVal;
	public static int AUROCH_MEAL_SIZE;
	public static int ANIMAL_DECAY_TIME;
	public static int TILE_MAX_CAPACITY;
	public static int ANIMAL_MAX_CAPACITY;
	public static String lang;
	public static String font;
	public static String font2;
	public static String screen_mode;
	public static String debug_mode;
	public static String default_map_size;
	public static String default_seed;
	public SpriteBatch batch;
	public static AssetManager manager;
	Texture img;
	
	@Override
	public void create () {
		loadConfig();
		loadLang();

		Gdx.app.log("ScreenRes", Gdx.graphics.getDisplayMode().toString());
		manager = new AssetManager();
		manager.load("audio/sounds/mooo.mp3", Sound.class);
		manager.load("audio/sounds/mooo2.mp3", Sound.class);
		manager.load("audio/sounds/miau.mp3", Sound.class);
		manager.load("audio/sounds/miau2.mp3", Sound.class);
		manager.load("audio/sounds/nom-nom-nom.mp3", Sound.class);
		manager.finishLoading();
		if(screen_mode.equals( LangPack.data.get("FULLSCREEN_ON"))){
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
		else if(screen_mode.equals(LangPack.data.get("FULLSCREEN_OFF"))){
			Gdx.graphics.setWindowedMode(1280, 720);
		}
		batch = new SpriteBatch();

		setScreen(new StartScreen(this));
	}

	@Override
	public void resize(int width, int height) {

	}
	
	@Override
	public void dispose () {
		super.dispose();
		manager.unload("`audio/sounds/moooo.mp3");
		manager.unload("audio/sounds/moooo2.mp3");
		manager.unload("audio/sounds/miau.mp3");
		manager.unload("audio/sounds/miau2.mp3");
		manager.unload("audio/sounds/nom-nom-nom.mp3");
		manager.dispose();
		batch.dispose();
		img.dispose();
	}
	void loadLang(){
		try {
			String langFile = "langpack_" + lang + ".yaml";
			LangPack.init(langFile);
			if(screen_mode.equals("Fullscreen on")) {
				screen_mode = LangPack.data.get("FULLSCREEN_ON").toString();
			}
			else {
				screen_mode = LangPack.data.get("FULLSCREEN_OFF").toString();
			}
			if(debug_mode.equals("Debug on")) {
				debug_mode = LangPack.data.get("DEBUG_ON").toString();
			}
			else {
				debug_mode = LangPack.data.get("DEBUG_OFF").toString();
			}
		}
		catch (Exception e){
			Gdx.app.log("LangPack","LangPack incomplete or missing");
			System.exit(1);
		}


	}
	void loadConfig(){
		try {
			Config.init();
			ANIMAL_DECAY_TIME = (int) Config.data.get("ANIMAL_DECAY_TIME");
			TILE_MAX_CAPACITY = (int) Config.data.get("TILE_MAX_CAPACITY");
			ANIMAL_MAX_CAPACITY = (int) Config.data.get("ANIMAL_MAX_CAPACITY");

			font = Config.data.get("MAIN_FONT").toString();
			font2 = Config.data.get("ADDITIONAL_FONT").toString();
			screen_mode = Config.data.get("SCREEN_MODE").toString();
			debug_mode = Config.data.get("DEBUG_MODE").toString();
			default_map_size = Config.data.get("DEFAULT_MAP_SIZE").toString();
			default_seed = Config.data.get("DEFAULT_SEED").toString();
			lang = Config.data.get("LANGUAGE").toString();
			AurochVal = (Map<String, Integer>) Config.data.get("Auroch");
			SimbaVal = (Map<String, Integer>) Config.data.get("Simbakubwa");
		}
		catch (Exception e){
			Gdx.app.log("Config","Config incomplete or missing");
			System.exit(1);
		}
	}
	public enum State{IDLE, WALKING, SPEAK, COURT, PREGNANT, SEEKING_FOOD, EATING, DEAD, SLEEP, RUN}
}
