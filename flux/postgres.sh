#!/usr/bin/env bash
export POSTGRES_PASSWORD=123456
export POSTGRES_USER=for_benchmark
export POSTGRES_DB=for_benchmark
docker run -d --cpuset-cpus=15 --name=postgres -p 127.0.0.1:5433:5432 \
--env POSTGRES_PASSWORD --env POSTGRES_USER --env POSTGRES_DB \
postgres:9.6