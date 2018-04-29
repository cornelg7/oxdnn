#!/bin/bash
if [ -s ".pids" ]
then
    sudo kill $(cat .pids)
    > .pids
fi
