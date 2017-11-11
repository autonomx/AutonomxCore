#!/bin/bash
cd "${0%/*}"
lsof -t -i :4444 | xargs kill
sleep 3
bash osx/hub/hub.sh &
bash osx/node/node.sh