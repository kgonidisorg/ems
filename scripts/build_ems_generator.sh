#!/bin/bash
docker build -f infrastructure/docker/Dockerfile.generator -t ghcr.io/kgonidisorg/ems:generator ./services