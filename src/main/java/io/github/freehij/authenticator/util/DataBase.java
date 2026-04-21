package io.github.freehij.authenticator.util;

import io.github.freehij.authenticator.data.Values;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataBase extends File {
    record DatabaseEntry(String hash, Cryptography.EncryptionType algorithm) { }

    final Map<String, DatabaseEntry> nameToHash = new HashMap<>();
    public boolean shouldSave = false;

    public DataBase(final String fileName) {
        super(fileName, "DataBase");
        if (Files.exists(path)) {
            try {
                List<String> lines = readLines(path);
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split("\t");
                    if (parts.length >= 2) {
                        String name = parts[0];
                        Cryptography.EncryptionType algorithm = Cryptography.EncryptionType.SHA256;
                        String hash;
                        if (parts.length >= 3) {
                            try {
                                algorithm = Cryptography.EncryptionType.valueOf(parts[1]);
                                hash = parts[2];
                            } catch (IllegalArgumentException e) {
                                hash = parts[1];
                            }
                        } else {
                            hash = parts[1];
                        }
                        nameToHash.put(name, new DatabaseEntry(hash, algorithm));
                    }
                }
            } catch (IOException e) {
                log("Failed to read database: " + path);
            }
        }
        save();
    }

    List<String> readLines(Path path1) throws IOException {
        try (InputStream in = Files.newInputStream(path1)) {
            try (InputStream gz = new GZIPInputStream(in)) {
                return new BufferedReader(new InputStreamReader(gz)).lines().toList();
            } catch (EOFException e) {
                return List.of();
            } catch (IOException e) {
                if (!e.getMessage().contains("Not in GZIP format")) throw e;
                try (InputStream in2 = Files.newInputStream(path1)) {
                    return new BufferedReader(new InputStreamReader(in2)).lines().toList();
                }
            }
        }
    }

    public boolean isRegistered(String name) {
        return nameToHash.containsKey(name);
    }

    public void set(String name, String password) {
        shouldSave = true;
        nameToHash.put(name, new DatabaseEntry(Cryptography.hash(name, password), Values.encryptionType));
    }

    public void remove(String name) {
        shouldSave = true;
        Sessions.eraseSession(name);
        nameToHash.remove(name);
    }

    public boolean checkPassword(String name, String password) {
        DatabaseEntry entry = nameToHash.get(name);
        return entry.hash.equals(Cryptography.hash(name, password, entry.algorithm));
    }

    @Override
    public void save() {
        if (!shouldSave) return;
        StringBuilder sb = new StringBuilder("username\talgorithm\thash\n");
        for (Map.Entry<String, DatabaseEntry> e : nameToHash.entrySet())
            sb.append(e.getKey()).append("\t").append(e.getValue().algorithm.name()).append("\t")
                    .append(e.getValue().hash).append("\n");
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