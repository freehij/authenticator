package io.github.freehij.authenticator.util;

import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.loader.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class File {
    final String logSrc;
    final Path path;

    public File(final String path, final String logSrc) {
        this.logSrc = Authenticator.MOD_ID + "/" + logSrc;
        this.path = Paths.get(path);
        try {
            if (this.path.getParent() != null) Files.createDirectories(this.path.getParent());
            if (!Files.exists(this.path)) Files.createFile(this.path);
        } catch (IOException e) {
            this.log("Failed to create file: " + path);
        }
    }

    void log(String message) {
        Logger.info(message, logSrc);
    }

    public abstract void save();
}
