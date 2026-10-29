package org.example.domain.service;

import org.example.domain.generator.EllerMazeGenerator;
import org.example.domain.model.Maze;
import org.example.domain.solver.MazeSolver;
import org.example.io.MazeReader;
import org.example.io.MazeWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

public class MazeServiceImpl implements MazeService {

    private final EllerMazeGenerator generator = new EllerMazeGenerator();

    @Override
    public Maze generate(int rows, int cols) {
        if (rows <= 0 || rows > Maze.MAX_SIZE) {
            throw new IllegalArgumentException("Rows must be in [1, " + Maze.MAX_SIZE + "]");
        }
        if (cols <= 0 || cols > Maze.MAX_SIZE) {
            throw new IllegalArgumentException("Cols must be in [1, " + Maze.MAX_SIZE + "]");
        }
        return generator.generate(rows, cols);
    }

    @Override
    public Maze load(Path path) {
        try {
            return MazeReader.read(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load maze: " + path, e);
        }
    }

    @Override
    public void save(Maze maze, Path path) {
        try {
            MazeWriter.write(maze, path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save maze: " + path, e);
        }
    }

    @Override
    public List<int[]> solve(Maze maze, int ai, int aj, int bi, int bj) {
        return new MazeSolver(maze).solve(ai, aj, bi, bj);
    }
}
