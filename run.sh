#!/usr/bin/env bash
# NestGrid — pure Java build & run script (no Maven, no dependencies)
set -e

SRC_DIR="src/main/java"
OUT_DIR="target/classes"
MAIN="com.nestgrid.NestGridApplication"

echo "=== NestGrid Build ==="

# Compile
echo "Compiling..."
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > sources.txt
javac -source 17 -target 17 -d "$OUT_DIR" @sources.txt
rm sources.txt
echo "✓ Compiled"

# Run
echo ""
echo "Starting server on http://localhost:8080 ..."
echo "Frontend: open another terminal and run:"
echo "  cd frontend && python3 -m http.server 5500"
echo "Then open http://localhost:5500/auth.html"
echo ""
java -cp "$OUT_DIR" "$MAIN"
