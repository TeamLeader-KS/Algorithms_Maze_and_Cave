package org.example.domain.service;

import org.example.domain.generator.CaveGenerator;
import org.example.domain.model.Cave;
import org.example.io.CaveReader;
import org.example.io.CaveWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class CaveServiceImpl implements CaveService {

    private final CaveGenerator generator = new CaveGenerator();

    @Override
    public Cave generate(int rows, int cols, double aliveChance) {
        return generator.generate(rows, cols, aliveChance);
    }

    @Override
    public Cave step(Cave cave, int birthLimit, int deathLimit) {
        return generator.step(cave, birthLimit, deathLimit);
    }

    @Override
    public Cave load(Path path) {
        try {
            return CaveReader.read(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load cave: " + path, e);
        }
    }

    @Override
    public void save(Cave cave, Path path) {
        try {
            CaveWriter.write(cave, path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save cave: " + path, e);
        }
    }
}
