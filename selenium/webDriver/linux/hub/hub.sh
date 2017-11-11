#!/bin/bash
cd "${0%/*}"
java -jar ../../selenium-server-standalone.jar -role hub -hubConfig hub.json