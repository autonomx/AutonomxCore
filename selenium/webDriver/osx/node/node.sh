#!/bin/bash
cd "${0%/*}"
java -Dphantomjs.binary.path="drivers/phantomjs/bin/phantomjs" -Dwebdriver.chrome.driver="drivers/chromedriver" -jar ../../selenium-server-standalone.jar -role node -hub http://localhost:4444/grid/register -nodeConfig node.json