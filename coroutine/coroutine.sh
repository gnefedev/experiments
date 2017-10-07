#!/usr/bin/env bash
docker run --rm --cpuset-cpus=0 --name=coroutine --network host com.gnefedev/coroutine:1.0