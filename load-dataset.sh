#!/usr/bin/env bash
set -e

echo "Loading official Neo4j recommendations dataset into Docker volume..."
docker compose stop neo4j
docker compose --profile load run --rm neo4j-load
docker compose up -d neo4j
echo "Dataset loaded successfully! Neo4j is running at http://localhost:7474"
