#!/bin/bash
./stop.sh
python3 ML/network_server.py &>> py.log &
sudo nodejs app.js &>> node.log &
jobs -p > .pids
