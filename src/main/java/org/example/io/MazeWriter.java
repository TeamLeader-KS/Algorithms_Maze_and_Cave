package org.example.io;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.domain.model.Maze;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MazeWriter {

    public static void write(Maze maze, Path path) throws IOException {
        if (maze == null) {
            throw new IllegalArgumentException("Maze must not be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            int rows = maze.getRows();
            int cols = maze.getCols();

            writer.write(rows + " " + cols);
            writer.newLine();

            writeMatrix(writer, rows, cols, maze, true);
            writer.newLine();
            writeMatrix(writer, rows, cols, maze, false);
        }
    }

    private static void writeMatrix(BufferedWriter writer, int rows, int cols,
                                    Maze maze, boolean vertical) throws IOException {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (vertical) {
                    writer.write(maze.hasRightWall(i, j) ? "1" : "0");
                } else {
                    writer.write(maze.hasBottomWall(i, j) ? "1" : "0");
                }

                if (j < cols - 1) {
                    writer.write(" ");
                }
            }
            writer.newLine();
        }
    }
}