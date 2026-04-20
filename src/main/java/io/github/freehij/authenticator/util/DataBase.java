package io.github.freehij.authenticator.util;

import io.github.freehij.authenticator.value.Values;

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
            try {
                return new BufferedReader(new InputStreamReader(new GZIPInputStream(in))).lines().toList();
            } catch (IOException e) {
                if (e.getMessage().contains("Not in GZIP format")) {
                    try (InputStream in2 = Files.newInputStream(path1)) {
                        return new BufferedReader(new InputStreamReader(in2)).lines().toList();
                    }
                }
                throw e;
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
        Sessions.eraseSession(name);
        nameToHash.remove(name);
    }

    public boolean checkPassword(String name, String password) {
        return nameToHash.get(name).equals(Cryptography.hash(name, password));
    }

    @Override
    public void save() {
        if (!shouldSave) return;
        StringBuilder sb = new StringBuilder("username\thash\n");
        for (Map.Entry<String, String> e : nameToHash.entrySet())
            sb.append(e.getKey()).append("\t").append(e.getValue()).append("\n");
        try (OutputStream out = Files.newOutputStream(path);
             Writer writer = Values.compressDatabase ?
                     new OutputStreamWriter(new GZIPOutputStream(out)) :
                     new OutputStreamWriter(out)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            log("Failed to save database: " + path);
        }
        shouldSave = false;
    }
}
