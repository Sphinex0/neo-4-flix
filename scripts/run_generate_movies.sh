#!/bin/bash
set -e

# go to the project root (one folder up from scripts/)
cd "$(dirname "$0")/.."

# load every variable from .env into this shell
set -a
source .env
set +a

scripts/venv/bin/python scripts/generate_movies.py
