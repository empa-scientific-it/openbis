#!/bin/bash

SCRIPT_PATH="`dirname \"$0\"`"
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"

pg_dump -U postgres -O openbis_test_js_basynthec > $SCRIPT_PATH/../../servers/basynthec/openBIS-server/db/openbis_test_js_basynthec.sql
pg_dump -U postgres -O pathinfo_test_js_basynthec > $SCRIPT_PATH/../../servers/basynthec/datastore_server/db/pathinfo_test_js_basynthec.sql
