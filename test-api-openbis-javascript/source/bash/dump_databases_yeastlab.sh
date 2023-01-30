#!/bin/bash

SCRIPT_PATH="`dirname \"$0\"`"
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"

pg_dump -U postgres -O openbis_test_js_yeastlab > $SCRIPT_PATH/../../servers/yeastlab/openBIS-server/db/openbis_test_js_yeastlab.sql
pg_dump -U postgres -O pathinfo_test_js_yeastlab > $SCRIPT_PATH/../../servers/yeastlab/datastore_server/db/pathinfo_test_js_yeastlab.sql
