#!/bin/bash
now="$(date +%s%N)"

ab -n 1000 -c 100 -p "/home/ziwei/workspace/KompicsSRE/script/syncbody" -T 'application/json' "http://localhost:8080/SRE/syncStorlet"

