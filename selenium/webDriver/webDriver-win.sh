#!/bin/bash
cd "${0%/*}"
lsof -t -i :4444 | xargs kill
sleep 3
bash win/hub/hub.sh &
bash win/node/node.sh