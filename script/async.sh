#!/bin/bash
now="$(date +%s%N)"

ab -g "/home/ziwei/workspace/KompicsSRE/script/result/plot.dat" -n 201 -c 1 -p "/home/ziwei/workspace/KompicsSRE/script/asyncbody" -T 'application/json' "http://localhost:8080/SRE/sics.vision.ExampleStorlet/handlerE/$now" 