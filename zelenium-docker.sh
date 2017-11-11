#!/bin/bash
cd "${0%/*}"
docker pull dosel/zalenium
docker run --rm -ti --name zalenium -p 4444:4444 -p 5555:5555     -e DOCKER=1.11     -v /var/run/docker.sock:/var/run/docker.sock     -v /tmp/videos:/home/seluser/videos     dosel/zalenium start