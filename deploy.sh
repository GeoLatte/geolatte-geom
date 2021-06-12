#! /bin/bash

#ensure we use java 8
. ~/.sdkman/bin/sdkman-init.sh
sdk env

mvn -P release clean deploy
