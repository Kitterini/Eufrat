package com.kitter.eufrat;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kitter.eufrat.screens.StartScreen;

public class Potamos extends Game {
	public static final float PPM = 320/4f;
	public static final short NOTHING_BIT = 0;
	public static final short TILE_BIT = 1;
	public static final short AUROCH_BIT = 2;
	public static final short DEAD_AUROCH_BIT = 4;
	public enum Sex{MALE, FEMALE, CHILD};
	public static final int AUROCH_ADOLECENCE_TIME = 300;
	public static final int AUROCH_LIFE_TIME = 1500;
	public static final int AUROCH_MEAL_SIZE = 25;
	public static final int AUROCH_DECAY_TIME = 200;
	public static final String font = "Gothic.ttf";
	public static final String font2 = "MECHANI_.ttf";
	public static String screen_mode = "Fullscreen off";
	public static String debug_mode = "Debug off";
	public static String default_map_size = "100";
	public static String default_seed = "213";
	public SpriteBatch batch;
	public static AssetManager manager;
	Texture img;
	
	@Override
	public void create () {
		Gdx.app.log("ScreenRes",Gdx.graphics.getDisplayMode().toString());
		manager = new AssetManager();
		manager.load("audio/sounds/mooo.mp3", Sound.class);
		manager.load("audio/sounds/mooo2.mp3", Sound.class);
		manager.load("audio/sounds/nom-nom-nom.mp3", Sound.class);
		manager.finishLoading();
		if(screen_mode.equals("Fullscreen on")){
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
		else if(screen_mode.equals("Fullscreen off")){
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
		manager.unload("assets/audio/sounds/mooo.wav");
		manager.unload("assets/audio/sounds/mooo2.wav");
		manager.dispose();
		batch.dispose();
		img.dispose();
	}

	public enum State{IDLE, WALKING, SPEAK, COURT, PREGNANT, SEEKING_FOOD, EATING, DEAD}
}
