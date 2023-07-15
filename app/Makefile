run:
	./gradlew run
run-dist: build
	./build/install/app/bin/app

build: clear
	./gradlew build
	./gradlew installDist

clear:
	./gradlew clean

test:
	./gradlew test

check:
	./gradlew check

tasks:
	./gradlew tasks

setup:
	gradle wrapper --gradle-version 8.2.1

.PHONY: build