#!/bin/bash

if [[ ${TRAVIS_SCALA_VERSION} = "scripted-test" ]]; then
  sbt 'set resolvers in Global += "staging" at "https://oss.sonatype.org/content/repositories/staging/"' '++ 2.12.3' root211/publishLocal '++ 2.11.11' root211/publishLocal '++ 2.10.6' publishLocal checkScalariform &&
  sbt -J-XX:+CMSClassUnloadingEnabled -J-Xmx512M -J-Xms512M mapper-generator/scripted
else
  sbt 'set resolvers in Global += "staging" at "https://oss.sonatype.org/content/repositories/staging/"' ++${TRAVIS_SCALA_VERSION} "project root211" test:compile checkScalariform testSequential
fi

