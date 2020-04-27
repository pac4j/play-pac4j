#!/bin/bash

# Only invoke the deployment to Sonatype when it's not a PR and only for 9.0.x
if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "9.0.x" ]; then
  mvn deploy --settings travis/settings.xml
  echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"
fi
