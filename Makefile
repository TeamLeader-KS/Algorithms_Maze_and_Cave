INSTALL_DIR = $(HOME)/.local/maze
JAR_NAME = A1_Maze_Java-1.0-SNAPSHOT.jar
BUILD_DIR = build
JAR_FILE = $(BUILD_DIR)/libs/$(JAR_NAME)
DIST_FILES = src build.gradle.kts settings.gradle.kts gradlew gradlew.bat gradle Makefile README.md

.PHONY: all install uninstall clean dvi dist tests

all:
	./gradlew build

install: all
	mkdir -p $(INSTALL_DIR)
	cp $(JAR_FILE) $(INSTALL_DIR)/

uninstall:
	rm -f $(INSTALL_DIR)/$(JAR_NAME)

clean:
	./gradlew clean

dvi:
	./gradlew javadoc

dist:
	tar -czf maze-dist.tar.gz $(DIST_FILES)

tests:
	./gradlew test

jacoco_report:
	./gradlew test jacocoTestReport
	open build/reports/jacoco/test/html/index.html

run:
	./gradlew run

help:
	@echo "Доступные цели:"
	@echo "  all        — собрать проект"
	@echo "  install    — установить в \$$HOME/.local/maze)"
	@echo "  uninstall  — удалить из \$$INSTALL_DIR"
	@echo "  clean      — очистить сборку"
	@echo "  dvi        — сгенерировать Javadoc"
	@echo "  dist       — создать архив с исходниками"
	@echo "  tests      — запустить тесты"
	@echo "  run        — запустить приложение"