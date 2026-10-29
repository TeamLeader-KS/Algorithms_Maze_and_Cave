package org.example.domain.service;

import org.example.domain.model.Cave;

import java.nio.file.Path;

public interface CaveService {
    Cave generate(int rows, int cols, double aliveChance);
    Cave step(Cave cave, int birthLimit, int deathLimit);
    Cave load(Path path);
    void save(Cave cave, Path path);
}
