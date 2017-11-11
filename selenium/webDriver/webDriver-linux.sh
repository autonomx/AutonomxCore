#!/bin/bash
cd "${0%/*}"
lsof -t -i :4444 | xargs kill
sleep 3
bash linux/hub/hub.sh &
bash linux/node/node.sh