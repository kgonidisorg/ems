#!/bin/bash
docker build \
  -f infrastructure/docker/Dockerfile.frontend \
  -t ghcr.io/kgonidisorg/ems:latest \
  --build-arg NEXT_PUBLIC_BASE_URL="http://localhost:8080" \
  ./frontend