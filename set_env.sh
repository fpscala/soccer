#!/bin/bash

export POSTGRES_HOST="localhost"
export POSTGRES_PORT=5432
export POSTGRES_USER="test"
export POSTGRES_PASSWORD="123"
export POSTGRES_DATABASE="test"
export POSTGRES_POOL_SIZE=1024
export HTTP_HEADER_LOG=false
export HTTP_BODY_LOG=false
export HTTP_HOST="localhost"
export HTTP_PORT=9000
export REDIS_SERVER_URI="redis://localhost"
export ACCESS_TOKEN_SECRET_KEY=5h0pp1ng_k4rt
export JWT_SECRET_KEY=-*5h0pp1ng_k4rt*-
export JWT_TOKEN_EXPIRATION=30.minutes
export JWT_CLAIM='{"uuid": "004b4457-71c3-4439-a1b2-03820263b59c"}'
export ADMIN_USER_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjbGFpbSI6ICJleGFtcGxlLWNsYWltIn0.Vo3eldFmRM4Yb5l8DIExaSNssYoufndDvn9S6_QP9_k
export PASSWORD_SALT=06!grsnxXG0d*Pj496p6fuA*o
export APP_ENV=test