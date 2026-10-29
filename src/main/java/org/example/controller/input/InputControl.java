package org.example.controller.input;

import javafx.scene.input.KeyEvent;

public class InputControl {

    public PlayerCommands toCommand(KeyEvent event) {
        return switch (event.getCode()) {
            case TAB -> PlayerCommands.USE_TAB;
            case LEFT -> PlayerCommands.MOVE_LEFT;
            case RIGHT -> PlayerCommands.MOVE_RIGHT;
            case UP -> PlayerCommands.MOVE_UP;
            case DOWN -> PlayerCommands.MOVE_DOWN;
            case SPACE -> PlayerCommands.USE_SPACE;
            case DIGIT1 -> PlayerCommands.USE_CONTROL_1;
            case DIGIT2 -> PlayerCommands.USE_CONTROL_2;
            case DIGIT3 -> PlayerCommands.USE_CONTROL_3;
            case ESCAPE -> PlayerCommands.USE_EXIT;
            case DIGIT0 -> PlayerCommands.USE_CLEAR;
            default -> PlayerCommands.NONE;
        };
    }
}