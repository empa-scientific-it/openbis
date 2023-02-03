#!/bin/bash

usage()
{
  echo ""
  echo "Usage: $0 <database-name> <owner-name>"
  echo ""
  echo "Example: '$0 openbis_dev openbis'"
  exit 1
}

if [ $# -ne 2 ]
then
	usage
fi

DB_NAME=$1
DB_OWNER=$2
DIR=`dirname $0`

sed -e "s/DB_NAME/$DB_NAME/g" -e "s/DB_OWNER/$DB_OWNER/g" $DIR/init.sql.template
