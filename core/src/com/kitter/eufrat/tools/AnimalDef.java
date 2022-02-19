package com.kitter.eufrat.tools;



import com.badlogic.gdx.math.Vector2;
import com.kitter.eufrat.Potamos;

public class AnimalDef {
    public Vector2 position;
    public Class<?> type;
    public Potamos.Sex sex;
    public AnimalDef( Class<?> type, Vector2 position, Potamos.Sex gender){
        this.type = type;
        this.position = position;
        this.sex = gender;
    }
}


