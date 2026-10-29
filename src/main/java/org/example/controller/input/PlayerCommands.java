package org.example.controller.input;

public enum PlayerCommands {
    NONE,
    USE_TAB,            // переключение вкладок Maze / Cave
    MOVE_LEFT,          // сдвиг слайдера влево
    MOVE_RIGHT,         // сдвиг слайдера вправо
    MOVE_UP,            // сдвиг слайдера вверх
    MOVE_DOWN,          // сдвиг слайдера вниз
    USE_SPACE,          // пробел
    USE_CONTROL_1,      // кнопка генерации
    USE_CONTROL_2,      // 2-я кнопка
    USE_CONTROL_3,      // 3-я кнопка
    USE_CLEAR,          // очистить поле
    USE_EXIT            // выход
}
