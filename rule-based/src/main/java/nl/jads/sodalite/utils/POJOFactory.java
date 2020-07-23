package nl.jads.sodalite.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import nl.jads.sodalite.dto.BlueprintMetadata;
import nl.jads.sodalite.dto.BuleprintsDataSet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class POJOFactory {

    public static BlueprintMetadata fromJson(String json) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson.fromJson(json, BlueprintMetadata.class);
    }

    public static BuleprintsDataSet fromJsonFile(String path) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        JsonReader jsonReader =
                gson.newJsonReader(new BufferedReader(new InputStreamReader(in)));
        return gson.fromJson(jsonReader, BuleprintsDataSet.class);

    }
}
