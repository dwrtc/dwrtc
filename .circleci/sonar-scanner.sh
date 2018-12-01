#!/usr/bin/env bash

if [ -n "${CIRCLE_PULL_REQUEST}" ]; then
  SONAR_OPTS="-Dsonar.pullrequest.provider=github" \
    "-Dsonar.pullrequest.github.repository='${CIRCLE_PROJECT_USERNAME}/${CIRCLE_PROJECT_REPONAME}'" \
    "-Dsonar.pullrequest.github.endpoint=https://api.github.com/" \
    "-Dsonar.pullrequest.branch=${CIRCLE_BRANCH}" \
    "-Dsonar.pullrequest.key=${CIRCLE_PULL_REQUEST##*/}" \
    "-Dsonar.pullrequest.base=master"
else
  SONAR_OPTS=""
fi

./gradlew sonarqube \
 -Dsonar.projectKey="${SONAR_KEY}" \
 -Dsonar.organization="${SONAR_ORGANZATION}" \
 -Dsonar.host.url="${SONAR_HOST}" \
 -Dsonar.login="${SONAR_LOGIN_TOKEN}" \
 ${SONAR_OPTS}
