package com.kitter.eufrat.tools;
import com.badlogic.gdx.Gdx;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class Config {
    private static Config single_instance = null;
    public static Map<String, Object> data;
    public Config(){
        Yaml yaml = new Yaml();
        InputStream inputStream  = null;
        try {
            inputStream = new FileInputStream(Gdx.files.internal("config.yaml").path());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        data = yaml.load(inputStream);
    }
    public static Config init() {
        if (single_instance == null) {
            synchronized (Config.class) {
                if (single_instance == null) {
                    single_instance = new Config();
                }
            }
        }
        return single_instance;
    }
}
