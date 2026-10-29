package org.example.io;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.domain.model.Cave;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaveReader {

    public static Cave read(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            try {
                String header = reader.readLine();
                String[] size = header.split(" ");

                int rows = Integer.parseInt(size[0]);
                int cols = Integer.parseInt(size[1]);

                Cave cave = new Cave(rows, cols);

                boolean[][] cells = readBooleanMatrix(reader, rows, cols);
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        cave.setCell(i, j, cells[i][j]);
                    }
                }

                if (reader.readLine() != null) {
                    throw new IOException("Unexpected content after end of file");
                }

                return cave;
            } catch (NullPointerException | ArrayIndexOutOfBoundsException |
                     IllegalArgumentException e) {
                throw new IOException("Invalid cave file: " + e.getMessage(), e);
            }
        }
    }

    private static boolean[][] readBooleanMatrix(BufferedReader reader, int rows, int cols) throws IOException {
        boolean[][] matrix = new boolean[rows][cols];

        for (int i = 0; i < rows; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of file at row " + i);
            }
            String[] tokens = line.split(" ");
            if (tokens.length != cols) {
                throw new IOException("Invalid row width at row " + i);
            }
            for (int j = 0; j < cols; j++) {
                String token = tokens[j];
                if (!token.equals("0") && !token.equals("1")) {
                    throw new IOException(
                            "Invalid value in file, found '" + token + "', expected '0' or '1'");
                }
                matrix[i][j] = "1".equals(token);
            }
        }
        return matrix;
    }
}