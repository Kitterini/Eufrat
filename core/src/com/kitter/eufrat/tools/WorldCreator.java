package com.kitter.eufrat.tools;

import com.badlogic.gdx.math.Vector2;
import com.kitter.eufrat.Potamos;
import com.kitter.eufrat.Sprites.Tile;
import com.kitter.eufrat.WorldHandler;
import com.kitter.eufrat.screens.GameScreen;

import java.util.Random;

public class WorldCreator {
    public static Vector2 size;

    public static void create() {

        Random rand = new Random(WorldHandler.SEED);
        //sometimes copper
        for (int i = 0; i < GameScreen.MAP_SIZE; i++) {
            for (int j = 0; j < GameScreen.MAP_SIZE; j++) {
                if(rand.nextInt(100)==0) {
                    WorldHandler.worldTiles[i][j] = new Tile("copper",
                            new Vector2(i * Potamos.PPM, j * Potamos.PPM));
                }
                else{
                    WorldHandler.worldTiles[i][j] = new Tile("meadow",
                            new Vector2(i * Potamos.PPM, j * Potamos.PPM));
                }
            }
        }
        // forests
        createForest(rand);
        // find out if the river is vertical or horizontal
        int direction = rand.nextInt(2);
        int bigOnes = rand.nextInt(2)+1;
        int smallOnes = rand.nextInt(7)+3;
        for (int i =0;i<bigOnes;i++){
            createRiver(true,direction, rand);
        }
        for (int i =0;i<smallOnes;i++){
            //direction = rand.nextInt(2);
            createRiver(false, direction, rand);
        }
        createDeltas();
        createBorders();
        populateAnimals(rand);
        defineBoundaries();
    }

    private static void createRiver(boolean main, int vertical, Random rand){
        Vector2[] river = new Vector2[GameScreen.MAP_SIZE *2];
        for(int i= 0; i < river.length ; i++){
            river[i] = new Vector2();
        }
        // initialize starting and endpoint of the river
        river[0].x = 0;
        river[river.length - 1].x = GameScreen.MAP_SIZE - 1;
        river[0].y = 0;
        river[river.length - 1].y = GameScreen.MAP_SIZE - 1;

        if(vertical == 0) {
            river[0].x = rand.nextInt(GameScreen.MAP_SIZE);
            river[river.length - 1].x = rand.nextInt(GameScreen.MAP_SIZE);
        }
        else{
            river[0].y = rand.nextInt(GameScreen.MAP_SIZE);
            river[river.length - 1].y = rand.nextInt(GameScreen.MAP_SIZE);
        }
        // generate number of curves
        Vector2[] curves = new Vector2[1];
        if( main) {
            curves = new Vector2[rand.nextInt(2) + 1];
        }

        // randomize point/points for the Bezier curve
        for(int i = 0; i<curves.length;i++){
            curves[i] = new Vector2();
            curves[i].x = rand.nextInt(GameScreen.MAP_SIZE);
            curves[i].y = rand.nextInt(GameScreen.MAP_SIZE);
        }

//        Gdx.app.log("start", String.valueOf(river[0]));
//        for(int i = 0; i<curves.length;i++) {
//            Gdx.app.log("mid", String.valueOf(curves[i]));
//        }
//        Gdx.app.log("end", String.valueOf(river[river.length - 1]));
        float temp_norm;
        // calculate actual points
        for(int t= 0; t < river.length; t++){
            temp_norm = t/(float)river.length + 1/(float)river.length;
            if(curves.length == 1) {
                river[t].x = (float) (Math.pow((1 - temp_norm),2) * river[0].x + 2 * (1 - temp_norm) * temp_norm * curves[0].x + Math.pow(temp_norm,2) * river[river.length - 1].x);
                river[t].y = (float) (Math.pow((1 - temp_norm),2) * river[0].y + 2 * (1 - temp_norm) * temp_norm * curves[0].y + Math.pow(temp_norm,2) * river[river.length - 1].y);
            }
            else {
                river[t].x = (float) (Math.pow((1 - temp_norm),3) * river[0].x + 3 * Math.pow((1 - temp_norm),2) * temp_norm * curves[0].x + 3 * Math.pow((1 - temp_norm),2) * temp_norm * curves[1].x +Math.pow(temp_norm,3) * river[river.length - 1].x);
                river[t].y = (float) (Math.pow((1 - temp_norm),3) * river[0].y + 3 * Math.pow((1 - temp_norm),2) * temp_norm * curves[0].y + 3 * Math.pow((1 - temp_norm),2) * temp_norm * curves[1].y +Math.pow(temp_norm,3) * river[river.length - 1].y);
            }
        }
        // change textures from vanilla meadow to water
        boolean finished = false;
        int xmin = -1;
        int ymin = -1;
        int xmax = 1;
        int ymax = 1;

        if(main){
            xmax = 3;
            ymax = 3;
        }
        int tempX;
        int tempY;
        for (Vector2 value : river) {
            if ((int) value.x + 4 < GameScreen.MAP_SIZE && (int) value.y + 4  < GameScreen.MAP_SIZE) {
                if (!main && WorldHandler.worldTiles[(int) value.x + 4][(int) value.y + 4].type.equals("fertile")) {
                    finished = true;
                }
            }
            for (int xaxis = xmin - 3; xaxis < xmax + 3; xaxis++) {
                for (int yaxis = ymin - 3; yaxis < ymax + 3; yaxis++) {
                    tempX = (int) value.x + xaxis;
                    tempY = (int) value.y + yaxis;
                    if( tempY < GameScreen.MAP_SIZE &&
                            tempX < GameScreen.MAP_SIZE &&
                            tempY >= 0 &&
                            tempX >= 0 &&
                            !WorldHandler.worldTiles[tempX][tempY].type.equals("ferforest") &&
                            !WorldHandler.worldTiles[tempX][tempY].type.equals("fertile") &&
                            !WorldHandler.worldTiles[tempX][tempY].type.equals("waters") ){
                        if(WorldHandler.worldTiles[tempX][tempY].type.equals("meadforest")){
                            WorldHandler.worldTiles[tempX][tempY] = new Tile("ferforest",
                                    new Vector2(tempX * Potamos.PPM, tempY * Potamos.PPM));
                        }
                        else {
                            WorldHandler.worldTiles[tempX][tempY] = new Tile("fertile",
                                    new Vector2(tempX * Potamos.PPM, tempY * Potamos.PPM));
                        }
                    }
                }
            }
            if (finished) {
                for (int xaxis = xmin - 3; xaxis < xmax + 3; xaxis++) {
                    for (int yaxis = ymin - 3; yaxis < ymax + 3; yaxis++) {
                        tempX = (int) value.x + xaxis;
                        tempY = (int) value.y + yaxis;
                        if( tempY < GameScreen.MAP_SIZE &&
                                tempX  < GameScreen.MAP_SIZE &&
                                tempY>= 0 &&
                                tempX >= 0 &&
                                !WorldHandler.worldTiles[tempX][tempY].type.equals("ferforest") &&
                                !WorldHandler.worldTiles[tempX][tempY].type.equals("fertile") &&
                                !WorldHandler.worldTiles[tempX][tempY].type.equals("waters")) {
                            if(WorldHandler.worldTiles[tempX][ tempY].type.equals("meadforest")){
                                WorldHandler.worldTiles[tempX][ tempY] = new Tile("ferforest",
                                        new Vector2(tempX * Potamos.PPM, tempY * Potamos.PPM));

                            }
                            else {
                                WorldHandler.worldTiles[(int) value.x + xaxis][(int) value.y + yaxis] = new Tile("fertile",
                                        new Vector2(((int) value.x + xaxis) * Potamos.PPM, ((int) value.y + yaxis) * Potamos.PPM));
                            }

                        }
                    }
                }
                break;
            }
        }
        finished = false;
        for (Vector2 value : river) {
            if ((int) value.x + 1 < GameScreen.MAP_SIZE && (int) value.y + 1 < GameScreen.MAP_SIZE) {
                if (!main && WorldHandler.worldTiles[(int) value.x + 1][(int) value.y + 1].type.equals("waters")) {
                    finished = true;
                }
            }
            for (int xaxis = xmin; xaxis < xmax; xaxis++) {
                for (int yaxis = ymin; yaxis < ymax; yaxis++) {
                    tempX = (int) value.x + xaxis;
                    tempY = (int) value.y + yaxis;
                    if (tempY < GameScreen.MAP_SIZE &&
                            tempX < GameScreen.MAP_SIZE &&
                            tempY >= 0 &&
                            tempX >= 0 &&
                            !WorldHandler.worldTiles[tempX][tempY].type.equals("waters")){
                        WorldHandler.worldTiles[tempX][tempY] = new Tile("waters",
                                new Vector2(tempX * Potamos.PPM, tempY * Potamos.PPM));

                    }
                }
            }
            if (finished) {
                for (int xaxis = xmin - 1; xaxis < xmax + 1; xaxis++) {
                    for (int yaxis = ymin - 1; yaxis < ymax + 1; yaxis++) {
                        tempX = (int) value.x + xaxis;
                        tempY = (int) value.y + yaxis;
                        if (tempY+1 < GameScreen.MAP_SIZE &&
                                tempX+1 < GameScreen.MAP_SIZE &&
                                tempY >= 0 &&
                                tempX >= 0) {
                            WorldHandler.worldTiles[tempX + 1][tempY + 1] = new Tile("waters",
                                    new Vector2((tempX + 1) * Potamos.PPM, (tempY + 1) * Potamos.PPM));
                        }
                    }
                }
                break;
            }
        }
    }

    public static void populateAnimals(Random rand){
        int x = 0;
        int y = 0;
        for(int i = 0; i < GameScreen.ANIMAL_AMOUNT; i++) {
            x = rand.nextInt(GameScreen.MAP_SIZE);
            y = rand.nextInt(GameScreen.MAP_SIZE);
            while(WorldHandler.worldTiles[x][y].type.equals("waters") || WorldHandler.worldTiles[x][y].type.equals("border")){
                x = rand.nextInt(GameScreen.MAP_SIZE);
                y = rand.nextInt(GameScreen.MAP_SIZE);
            }
            Potamos.Sex sexes[] = new Potamos.Sex[2];
            sexes[0]= Potamos.Sex.MALE;
            sexes[1]= Potamos.Sex.FEMALE;
            WorldHandler.getInstance().addAuroch(x * Potamos.PPM, y* Potamos.PPM, sexes[rand.nextInt(2)],0);
        }
    }

    public static void createDeltas(){
        // identify deltas and fill
        int prevwater;
        int deltarange = GameScreen.MAP_SIZE/5;
        for (int i = 0; i < GameScreen.MAP_SIZE; i++) {
            prevwater = 0;
            for (int j = 0; j < GameScreen.MAP_SIZE; j++) {
                if (WorldHandler.worldTiles[i][j].type.equals("waters")) {
                    if (prevwater != 0 && j - prevwater > 1 && (j - prevwater) < deltarange) {
                        int fertile_range = Math.min((j - prevwater), (deltarange - (j - prevwater)));
                        for (int k = prevwater + 1; k < prevwater+fertile_range ; k++) {
                            if (!WorldHandler.worldTiles[i][k].type.equals("ferforest") &&
                                    !WorldHandler.worldTiles[i][k].type.equals("fertile")){
                                if (WorldHandler.worldTiles[i][k].type.equals("meadforest")){
                                    WorldHandler.worldTiles[i][k] = new Tile("ferforest",
                                            new Vector2(i * Potamos.PPM, k * Potamos.PPM));
                                }
                                else{
                                    WorldHandler.worldTiles[i][k] = new Tile("fertile",
                                            new Vector2(i * Potamos.PPM, k * Potamos.PPM));
                                }
                            }
                        }
                        WorldHandler.worldTiles[i][prevwater + 1] = new Tile("coast",
                                new Vector2(i * Potamos.PPM, (prevwater + 1) * Potamos.PPM));
                        for (int k = j-1; k > j-fertile_range-1; k--) {
                            if (!WorldHandler.worldTiles[i][k].type.equals("ferforest") &&
                                    !WorldHandler.worldTiles[i][k].type.equals("fertile")) {
                                if (WorldHandler.worldTiles[i][k].type.equals("meadforest")) {
                                    WorldHandler.worldTiles[i][k] = new Tile("ferforest",
                                            new Vector2(i * Potamos.PPM, k * Potamos.PPM));
                                } else {
                                    WorldHandler.worldTiles[i][k] = new Tile("fertile",
                                            new Vector2(i * Potamos.PPM, k * Potamos.PPM));
                                }
                            }
                        }
                        WorldHandler.worldTiles[i][j-1] = new Tile("coast",
                                new Vector2(i * Potamos.PPM, (j-1) * Potamos.PPM));
                    }
                    prevwater = j;
                }
            }
        }
        for (int i = 0; i < GameScreen.MAP_SIZE; i++) {
            prevwater = 0;
            for (int j = 0; j < GameScreen.MAP_SIZE; j++) {
                if(WorldHandler.worldTiles[j][i].type.equals("waters")){
                    if(prevwater!=0 && j - prevwater > 2 && (j - prevwater) < deltarange){
                        int fertile_range = Math.min((j - prevwater), (deltarange - (j - prevwater)));
                        for (int k = prevwater + 1; k < prevwater+fertile_range ; k++) {
                            if (! WorldHandler.worldTiles[k][i].type.equals("ferforest") &&
                                    ! WorldHandler.worldTiles[k][i].type.equals("fertile")) {
                                if ( WorldHandler.worldTiles[k][i].type.equals("meadforest")) {
                                    WorldHandler.worldTiles[k][i] = new Tile("ferforest",
                                            new Vector2(k * Potamos.PPM, i * Potamos.PPM));
                                } else {
                                    WorldHandler.worldTiles[k][i] = new Tile("fertile",
                                            new Vector2(k * Potamos.PPM, i * Potamos.PPM));
                                }
                            }
                        }
                        WorldHandler.worldTiles[prevwater + 1][i] = new Tile("coast",
                                new Vector2((prevwater + 1) * Potamos.PPM, i * Potamos.PPM));
                        for (int k = j-1; k > j-fertile_range; k--) {
                            if (! WorldHandler.worldTiles[k][i].type.equals("ferforest") &&
                                    ! WorldHandler.worldTiles[k][i].type.equals("fertile")) {
                                if ( WorldHandler.worldTiles[k][i].type.equals("meadforest")) {
                                    WorldHandler.worldTiles[k][i] = new Tile("ferforest",
                                            new Vector2(k * Potamos.PPM, i * Potamos.PPM));
                                } else {
                                    WorldHandler.worldTiles[k][i] = new Tile("fertile",
                                            new Vector2(k * Potamos.PPM, i * Potamos.PPM));
                                }
                            }
                        }
                        WorldHandler.worldTiles[j-1][i] = new Tile("coast",
                                new Vector2((j-1) * Potamos.PPM, i * Potamos.PPM));
                    }
                    prevwater = j;
                }
            }
        }
    }

    public static void createForest(Random rand){
        int forest_num = rand.nextInt(5) + (GameScreen.MAP_SIZE/10);
        int forest_size;
        int forestX;
        int forestY;
        int direction;

        for(int i = 0; i<=forest_num; i++){
            forest_size = rand.nextInt(GameScreen.MAP_SIZE*6);
            forestX = rand.nextInt(GameScreen.MAP_SIZE);
            forestY = rand.nextInt(GameScreen.MAP_SIZE);

            for(int j = 0; j < forest_size; j++){
                if(!WorldHandler.worldTiles[forestX][forestY].type.equals("meadforest")){
                    WorldHandler.worldTiles[forestX][forestY] = new Tile("meadforest",
                            new Vector2(forestX * Potamos.PPM, forestY * Potamos.PPM));
                }
                else{
                    if(forest_size<GameScreen.MAP_SIZE+100) {
                        forest_size++;
                    }
                }
                direction = rand.nextInt(4);
                if(direction==0){
                    if(forestX<GameScreen.MAP_SIZE-1) {
                        forestX++;
                    }
                }
                else if(direction==1){
                    if(forestY<GameScreen.MAP_SIZE-1) {
                        forestY++;
                    }
                }
                else if(direction==2){
                    if(forestX>0) {
                        forestX--;
                    }
                }
                else {
                    if(forestY>0) {
                        forestY--;
                    }
                }
            }
        }
    }

    public static void createBorders(){
        for(int i = 0; i < GameScreen.MAP_SIZE; i++){
            WorldHandler.worldTiles[0][i]=new Tile("border",
                    new Vector2(0, i * Potamos.PPM));
            WorldHandler.worldTiles[GameScreen.MAP_SIZE-1][i]=new Tile("border",
                    new Vector2((GameScreen.MAP_SIZE-1) * Potamos.PPM, i * Potamos.PPM));

            WorldHandler.worldTiles[i][0]=new Tile("border",
                    new Vector2(i * Potamos.PPM,  0));
            WorldHandler.worldTiles[i][GameScreen.MAP_SIZE-1]=new Tile("border",
                    new Vector2(i * Potamos.PPM, (GameScreen.MAP_SIZE-1) * Potamos.PPM));

        }
        for(int i = 0; i < GameScreen.MAP_SIZE; i++) {
            WorldHandler.worldTiles[GameScreen.MAP_SIZE-1][i].flipTile();
        }
    }

    public static void defineBoundaries(){
        for(int i = 0; i < GameScreen.MAP_SIZE; i++) {
            for(int j = 0; j < GameScreen.MAP_SIZE; j++) {
                if(WorldHandler.worldTiles[i][j].type.equals("border") ||
                    WorldHandler.worldTiles[i][j].type.equals("waters")){
                    WorldHandler.worldTiles[i][j].defineTile();
                }
            }
        }
    }
}
