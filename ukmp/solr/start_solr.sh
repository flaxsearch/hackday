#!/bin/bash

APP_ROOT=/home/mlp/flax/hackday/ukmp

SOLR_ROOT=/home/mlp/flax/solr-4.6.0
CONF_ROOT=$APP_ROOT/solr/collection1/conf

cd $SOLR_ROOT/example

java -Dsolr.solr.home=$APP_ROOT/solr \
	-Dlog4j.configuration=file://$CONF_ROOT/log4j.properties \
	-jar start.jar &

