#!/bin/bash
# Author: Tomasz Pylak
# Creates new database version (all sql files) which is identical to the previous version.
# Assumes that it is called from the directory where it is located.

SQL_DIR=../../source/sql/
source common.sh

copy_db_folder generic
copy_db_folder postgresql
copy_migration_file
print_finish_message
