#!/bin/bash
./stop.sh
python3 ML/network_server.py &
sudo nodejs app.js &
jobs -p > .pids
