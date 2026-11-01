#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"

BUILD=build
JAR="$BUILD/text-algorithms-lab.jar"

compile() {
    rm -rf "$BUILD"
    mkdir -p "$BUILD/main" "$BUILD/test"
    javac --release 17 -encoding UTF-8 -d "$BUILD/main" $(find src/main/java -name '*.java')
    javac --release 17 -encoding UTF-8 -cp "$BUILD/main" -d "$BUILD/test" $(find src/test/java -name '*.java')
    jar --create --file "$JAR" --main-class io.github.ignat22.textlab.App -C "$BUILD/main" .
    echo "Built $JAR"
}

run_tests() {
    java -cp "$BUILD/main:$BUILD/test" io.github.ignat22.textlab.testkit.TestLauncher
}

case "${1:-all}" in
    build) compile ;;
    test)  compile; run_tests ;;
    demo)  compile; java -jar "$JAR" demo ;;
    bench) compile; java -jar "$JAR" bench ;;
    all)   compile; run_tests; java -jar "$JAR" demo; java -jar "$JAR" bench ;;
    *)     echo "usage: sh lab.sh [build|test|demo|bench|all]"; exit 2 ;;
esac
