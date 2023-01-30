#!/bin/bash

SCRIPT_PATH="`dirname \"$0\"`"
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"

pg_dump -U postgres -O openbis_test_js_dsu > $SCRIPT_PATH/../../servers/dsu/openBIS-server/db/openbis_test_js_dsu.sql
pg_dump -U postgres -O pathinfo_test_js_dsu > $SCRIPT_PATH/../../servers/dsu/datastore_server/db/pathinfo_test_js_dsu.sql
