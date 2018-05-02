#!/usr/bin/env bash
docker run -d --cpuset-cpus=13-14 --name=memcached -p 127.0.0.1:11211:11211 memcached:1.5.7