#!/usr/bin/env bash
export SERVER_TYPE=netty
docker run --rm --cpuset-cpus=0 --env SERVER_TYPE --network host --env= com.gnefedev/flux:1.0