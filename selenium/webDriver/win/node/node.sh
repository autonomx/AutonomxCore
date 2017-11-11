#!/bin/bash
cd "${0%/*}"
java -Dwebdriver.chrome.driver="drivers/chromedriver.exe" -jar ../../selenium-server-standalone.jar -role node -hub http://localhost:4444/grid/register -nodeConfig node.json