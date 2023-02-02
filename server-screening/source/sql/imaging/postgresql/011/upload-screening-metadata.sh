DB=$1
psql -U postgres -d openbis_$DB -f data.sql
