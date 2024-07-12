#!/bin/bash

set -eux

sbt -v "core/testOnly scalikejdbc.DB_MetaDataSpec"
