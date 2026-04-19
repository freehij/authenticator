package io.github.freehij.authenticator.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataBase extends File {
    final Map<String, String> nameToHash = new HashMap<>();
    public boolean shouldSave = false;

    public DataBase(final String fileName) {
        super(fileName, "DataBase");
        if (Files.exists(path)) {
            try {
                List<String> lines = readLines(path);
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split("\t");
                    if (parts.length == 2) nameToHash.put(parts[0], parts[1]);
                }
            } catch (IOException e) {
                log("Failed to read database: " + path);
            }
        }
        save();
    }

    List<String> readLines(Path path1) throws IOException {
        try (InputStream in = Files.newInputStream(path1)) {
            try (GZIPInputStream gz = new GZIPInputStream(in);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gz))) {
                return reader.lines().toList();
            } catch (IOException e) {
                return Files.readAllLines(path1);
            }
        }
    }

    public boolean isRegistered(String name) {
        return nameToHash.containsKey(name);
    }

    public void set(String name, String password) {
        shouldSave = true;
        nameToHash.put(name, Cryptography.hash(name, password));
    }

    public void remove(String name) {
        shouldSave = true;
        nameToHash.remove(name);
    }

    public boolean checkPassword(String name, String password) {
        return nameToHash.get(name).equals(Cryptography.hash(name, password));
    }

    @Override
    public void save() {
        StringBuilder sb = new StringBuilder("username\thash\n");
        for (Map.Entry<String, String> e : nameToHash.entrySet())
            sb.append(e.getKey()).append("\t").append(e.getValue()).append("\n");
        try (OutputStream out = Files.newOutputStream(path);
             GZIPOutputStream gz = new GZIPOutputStream(out);
             Writer writer = new OutputStreamWriter(gz)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            log("Failed to save database: " + path);
        }
    }
}
