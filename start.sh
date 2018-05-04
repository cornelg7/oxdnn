#!/bin/bash
./stop.sh
printf "\nStarted server at " >> node.log
date >> node.log
printf "\n\n" >> node.log
python3 ML/app.py &>> py.log &
sudo nodejs app.js &>> node.log &
jobs -p > .pids
