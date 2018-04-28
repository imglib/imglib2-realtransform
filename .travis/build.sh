#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_fdcfa88d6e18_key $encrypted_fdcfa88d6e18_iv
