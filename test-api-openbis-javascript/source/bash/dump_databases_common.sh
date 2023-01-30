#!/bin/bash

SCRIPT_PATH="`dirname \"$0\"`"
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"

pg_dump -U postgres -O openbis_test_js_common > $SCRIPT_PATH/../../servers/common/openBIS-server/db/openbis_test_js_common.sql

pg_dump -U postgres -O pathinfo_test_js_common > $SCRIPT_PATH/../../servers/common/datastore_server/db/pathinfo_test_js_common.sql
pg_dump -U postgres -O imaging_test_js_common > $SCRIPT_PATH/../../servers/common/datastore_server/db/imaging_test_js_common.sql

pg_dump -U postgres -O pathinfo_test_js_common2 > $SCRIPT_PATH/../../servers/common/datastore_server2/db/pathinfo_test_js_common2.sql
pg_dump -U postgres -O imaging_test_js_common2 > $SCRIPT_PATH/../../servers/common/datastore_server2/db/imaging_test_js_common2.sql
