#! /bin/bash

#ensure we use java 17
. ~/.sdkman/bin/sdkman-init.sh
sdk env

mvn -P release clean deploy
