#!/bin/bash

source env.sh
sbt -mem 5000 "project server" ~reStart
#sbt -mem 3000 "runServer"