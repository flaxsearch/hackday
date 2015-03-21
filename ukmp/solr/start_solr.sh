#!/bin/bash

APP_ROOT=/home/mlp/workspace/hackday/ukmp

SOLR_ROOT=/home/mlp/apps/solr-5.0.0
CONF_ROOT=$APP_ROOT/solr/collection1/conf
DATA_DIR=/home/mlp/tmp/ukmp/solr

SOLR_HOME=$APP_ROOT/solr
LOG4J_PROPS=$CONF_ROOT/log4j.properties

cd $SOLR_ROOT
bin/solr start -s $APP_ROOT/solr

