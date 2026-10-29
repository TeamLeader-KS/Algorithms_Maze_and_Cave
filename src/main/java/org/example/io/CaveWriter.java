package org.example.io;

import org.example.domain.model.Cave;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CaveWriter {

    private CaveWriter() {}

    public static void write(Cave cave, Path path) throws IOException {
        if (cave == null) {
            throw new IllegalArgumentException("Cave must not be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            int rows = cave.getRows();
            int cols = cave.getCols();

            writer.write(rows + " " + cols);
            writer.newLine();

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    writer.write(cave.isAlive(i, j) ? "1" : "0");
                    if (j < cols - 1) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
            }
        }
    }
}