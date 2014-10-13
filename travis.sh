#!/bin/bash

rm scalikejdbc-core/src/test/resources/jdbc.properties &&
cp -p scalikejdbc-core/src/test/resources/jdbc_$SCALIKEJDBC_DATABASE.properties scalikejdbc-core/src/test/resources/jdbc.properties &&

if [[ ${TRAVIS_SCALA_VERSION} = "2.10.4" ]]; then
  sbt ++${TRAVIS_SCALA_VERSION} test:compile test
elif [[ ${TRAVIS_SCALA_VERSION} = "scripted-test" ]]; then
  sbt '++ 2.11.2' root211/publishLocal '++ 2.10.4' publishLocal mapper-generator/scripted
elif [[ ${TRAVIS_SCALA_VERSION} = "2.11.2" ]]; then
  sbt ++${TRAVIS_SCALA_VERSION} "project root211" test:compile test
else
  exit -1
fi

