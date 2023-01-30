#!/bin/bash

SCRIPT_PATH="`dirname \"$0\"`"
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"

pg_dump -U postgres -O openbis_test_js_novartis > $SCRIPT_PATH/../../servers/novartis/openBIS-server/db/openbis_test_js_novartis.sql
pg_dump -U postgres -O pathinfo_test_js_novartis > $SCRIPT_PATH/../../servers/novartis/datastore_server/db/pathinfo_test_js_novartis.sql
