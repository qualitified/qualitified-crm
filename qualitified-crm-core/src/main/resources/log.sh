#!/bin/bash
tail -f  -n 100 /var/log/nuxeo/server.log &
kill `ps | grep tail | awk '{print $1;}'`