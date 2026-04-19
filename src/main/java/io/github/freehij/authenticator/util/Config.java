package io.github.freehij.authenticator.util;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Config extends File {
    final Map<String, String> data = new HashMap<>();

    public Config(final String fileName) {
        super(fileName, "Config");
        try (JsonReader reader = JsonProviders.createReader(Files.newBufferedReader(path))) {
            Map<String, Object> map = reader.readMap(JsonReader::readUntyped);
            if (map != null) map.forEach((key, value) -> data.put(key, String.valueOf(value)));
        } catch (IOException e) {
            log("Failed to load config: " + path);
        }
    }

    public String get(String key, String defaultValue) {
        if (!data.containsKey(key)) data.put(key, defaultValue);
        return data.get(key);
    }

    public int get(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    @Override
    public void save() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter w = JsonProviders.createWriter(outputStream)) {
                w.writeMap(data, JsonWriter::writeString);
            }
            String compact = outputStream.toString();
            String pretty = compact.replace("{", "{\n\t").replace(",", ",\n\t").replace("}", "\n}");
            Files.writeString(path, pretty);
        } catch (IOException e) {
            log("Failed to save config: " + path);
        }
    }
}