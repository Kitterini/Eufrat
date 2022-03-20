package com.kitter.eufrat.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.kitter.eufrat.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;

public class CameraController implements com.badlogic.gdx.input.GestureDetector.GestureListener {
    public float velX, velY;
    public boolean flinging = false;
    float initialScale = 1;

    public boolean touchDown(float x, float y, int pointer, int button) {
        flinging = false;
        initialScale = GameScreen.getGameCam().zoom;
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        flinging = true;
        velX = GameScreen.getGameCam().zoom * velocityX * 0.5f;
        velY = GameScreen.getGameCam().zoom * velocityY * 0.5f;
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        GameScreen.getGameCam().position.add(-deltaX * GameScreen.getGameCam().zoom, deltaY * GameScreen.getGameCam().zoom, 0);
        WorldHandler.getInstance().setVisibility(GameScreen.getGameCam().position);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float originalDistance, float currentDistance) {
        float ratio = originalDistance / currentDistance;
        GameScreen.getGameCam().zoom = initialScale * ratio;
        WorldHandler.getInstance().setVisibility(GameScreen.getGameCam().position);
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }


    public void update() {
        if (flinging) {
            velX *= 0.98f;
            velY *= 0.98f;
            GameScreen.getGameCam().position.add(-velX * Gdx.graphics.getDeltaTime(), velY * Gdx.graphics.getDeltaTime(), 0);
            if (Math.abs(velX) < 0.01f) velX = 0;
            if (Math.abs(velY) < 0.01f) velY = 0;
        }
    }

    @Override
    public void pinchStop() {
    }
}

