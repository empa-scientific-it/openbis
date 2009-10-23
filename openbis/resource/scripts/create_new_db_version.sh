#!/bin/bash
# Author: Tomasz Pylak
# Creates new database version (all sql files) which is identical to the previous version.

SQL_DIR=../../source/sql/

PREV_NUM=`ls -1 $SQL_DIR/postgresql | grep 0 | sort | tail -n 1 | tr -d "0"`
PREV_VER=0$PREV_NUM
CUR_VER=0$(( $PREV_NUM+1 ))

function get_db_dir {
    local db_engine=$1
    local ver=$2
    echo $SQL_DIR/$db_engine/$ver
}

function copy_db_folder {
    local db_engine=$1
    
    local cur_dir=`get_db_dir $db_engine $CUR_VER`
    local prev_dir=`get_db_dir $db_engine $PREV_VER`
    mkdir $cur_dir
    for file in $prev_dir/*; do
	org_name=`basename $file`
        new_name=${org_name/$PREV_VER/$CUR_VER}
	cp $file $cur_dir/$new_name
    done
}

function copy_migration_file {
    local dir=$SQL_DIR/postgresql/migration
    echo "-- Migration from $PREV_VER to $CUR_VER" > $dir/migration-$PREV_VER-$CUR_VER.sql
}

copy_db_folder generic
copy_db_folder postgresql
copy_migration_file
echo "Sql files for the $CUR_VER version have been copied from the previous version $PREV_VER."
echo "Create a new test database after writing the migration script."
