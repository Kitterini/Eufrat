package com.kitter.eufrat.tools;
import com.badlogic.gdx.Gdx;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class LangPack {
    private static LangPack single_instance = null;
    public static Map<String, Object> data;
    public LangPack(String pack){
        Yaml yaml = new Yaml();
        InputStream inputStream  = null;
        try {
            inputStream = new FileInputStream(Gdx.files.internal(pack).path());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        data = yaml.load(inputStream);
    }
    public static void reinit(String pack){
        single_instance = new LangPack(pack);
    }
    public static LangPack init(String pack) {
        if (single_instance == null) {
            synchronized (Config.class) {
                if (single_instance == null) {
                    single_instance = new LangPack(pack);
                }
            }
        }
        return single_instance;
    }
}

