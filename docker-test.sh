#!/bin/bash
set -e

docker-compose up -d biosamples-agents-solr

for X in "$@"
do
  docker-compose run --rm --service-ports biosamples-integration java -jar integration-4.2.1-RC2.jar --phase=$X $ARGS $@
  sleep 30 #solr is configured to commit every 5 seconds

done

docker-compose up -d biosamples-agents-solr

echo "Successfully completed"

