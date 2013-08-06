#!/bin/bash
now="$(date +%s%N)"

ab -g "/home/ziwei/workspace/KompicsSRE/script/result/plot.dat" -n 1000 -c 1 -p "/home/ziwei/workspace/KompicsSRE/script/asyncbody" -H "Accept: application/json" "http://localhost:8080/SRE/sics.vision.ExampleStorlet/handlerE/$now" 
