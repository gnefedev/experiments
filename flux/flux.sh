#!/usr/bin/env bash
#export SERVER_TYPE=jetty
#export SERVER_TYPE=jettyAsync
export SERVER_TYPE=netty
docker run --rm --cpuset-cpus=0-1 --env SERVER_TYPE --network host com.gnefedev/flux:1.0