package com.kitter.eufrat.old;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
// this is a legacy class, a cleaner implementation is  available using libgdx
// I left the code for educational (maybe) purposes
// definition implementation:
//        touch = new SimpleDirectionGestureDetector(new SimpleDirectionGestureDetector.DirectionListener() {
//            @Override
//            public void onLeft(float distance) {}
//
//            @Override
//            public void onRight(float distance) {}
//
//            @Override
//            public void onUp(float distance) {}
//
//            @Override
//            public void onDown(float distance) {}
//
//            @Override
//            public void onZoom(float distance, float prevDistance) {
//                if(prevDistance>distance+10 || prevDistance<distance-10){
//                    prevDistance=distance;
//                }
//                zoom((prevDistance - distance)/20);
//            }
//        });

public class SimpleDirectionGestureDetector extends GestureDetector {
    public interface DirectionListener {
        void onLeft(float distance);

        void onRight(float distance);

        void onUp(float distance);

        void onDown(float distance);

        void onZoom(float distance, float prevDistance);
    }

    public SimpleDirectionGestureDetector(DirectionListener directionListener) {
        super(new DirectionGestureListener(directionListener));
    }

    private static class DirectionGestureListener extends GestureAdapter{
        DirectionListener directionListener;
        private float prevZoomDistance;

        public DirectionGestureListener(DirectionListener directionListener){
            this.directionListener = directionListener;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            Gdx.app.log("X", String.valueOf(velocityX));
            Gdx.app.log("Y", String.valueOf(velocityY));
//            if(Math.abs(velocityX)>Math.abs(velocityY)){
//                if(velocityX>0){
//                    directionListener.onRight(6);
//                }else{
//                    directionListener.onLeft(6);
//                }
//            }else{
//                if(velocityY>0){
//                    directionListener.onDown(6);
//                }else{
//                    directionListener.onUp(6);
//                }
//            }
            if(velocityX>0) {
                directionListener.onRight(-velocityX/200);
            }
            else {
                directionListener.onLeft(-velocityX/200);
            }
            if(velocityY>0) {
                directionListener.onDown(velocityY/200);
            }
            else {
                directionListener.onUp(velocityY/200);
            }
            return super.fling(velocityX, velocityY, button);
        }
        @Override
        public boolean zoom(float initialDistance, float distance) {
            //Gdx.app.log("DISTANCE", String.valueOf(distance)) ;
            //Gdx.app.log("initialDistance", String.valueOf(initialDistance)) ;
            Gdx.app.log("INFO", "Zoom performed");

            directionListener.onZoom(distance, prevZoomDistance);
            prevZoomDistance = distance;
            //gameCam.zoom = (initialDistance / distance) * currentZoom;
            //gameCam.update();

            return true;
        }
    }

}
