#!/usr/bin/env bash
# Builds the jar and pushes the image. Run from the repo root: ./deploy/build-image.sh [tag]
set -euo pipefail

REGISTRY="${REGISTRY:-miguelperezcolom}"
TAG="${1:-0.1.2}"
IMAGE="$REGISTRY/swapi-service:$TAG"

cd "$(dirname "$0")/.."

# `clean` and not just `package`: a stale target/ is how a jar that no longer matches the source
# ends up inside an image.
mvn -B -ntp clean package

# amd64 explicitly: the cluster's node pool can hand out arm64, and a single-arch image on the wrong
# architecture is an unschedulable pod, not a build failure you find out about here.
docker buildx build --platform linux/amd64 -t "$IMAGE" --push .

echo
echo "Pushed $IMAGE"
echo "Point deploy/10-swapi.yaml at this tag if it is not already."
