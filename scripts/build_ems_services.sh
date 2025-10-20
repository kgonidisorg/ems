#!/bin/bash
docker build -f infrastructure/docker/Dockerfile.combined-service -t ghcr.io/kgonidisorg/ems:services ./backend