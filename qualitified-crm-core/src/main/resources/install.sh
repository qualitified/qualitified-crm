#!/bin/bash
#echo $1
echo 'Stopping nuxeo...'
sudo nuxeoctl stop
sudo nuxeoctl mp-remove $1 --accept=true --nodeps
sudo nuxeoctl mp-install /opt/import/$2 --accept=true --nodeps
#mv /opt/import/$2 /opt/import/$2.done
echo 'Starting nuxeo...'
sudo nuxeoctl start