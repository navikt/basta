#!/bin/bash

BASE_DIR=`dirname $0`
KARMA_LOC=`which karma`
NPM_DIR=`dirname $KARMA_LOC`
echo " Using NPM directory " $NPM_DIR
echo "Starting Karma Server (http://karma-runner.github.io)"
echo "-------------------------------------------------------------------"

karma start $BASE_DIR/war/src/test/resources/karma-e2e.config.js $*
