package org.example.domain.service;

import org.example.domain.model.Maze;

import java.nio.file.Path;
import java.util.List;

public interface MazeService {
    Maze generate(int rows, int cols);
    Maze load(Path path);
    void save(Maze maze, Path path);
    List<int[]> solve(Maze maze, int ai, int aj, int bi, int bj);
}