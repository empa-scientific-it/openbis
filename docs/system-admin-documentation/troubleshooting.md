Troubleshooting Problems
========================

## Samples with datasets and no experiments

In the openBIS UI users could detach samples with container data sets
from the experiment. This bug was fix on version S176 released on 14 of
march of 2014.

The following SQL script lists all samples with data sets but no
experiments:

```sql
##
## SELECT SAMPLES WITH DATASETS AND NO EXPERIMENTS
##
SELECT s.id, d.expe_id from samples_all s join data_all d on (d.samp_id=s.id) where s.expe_id is null ORDER by s.id

If the last query shows no output the system is fine, if not, it can be
repaired with the following update query.

##
## FIX SAMPLES WITH DATASETS AND NO EXPERIMENTS ASSIGNING EXPERIMENT FROM DATASET
##
UPDATE samples_all
SET expe_id = subquery.expe_id
FROM (
    SELECT s.id as samp_id, d.expe_id as expe_id from samples_all s join data_all d on (d.samp_id=s.id) where s.expe_id is null
) as subquery
where id = subquery.samp_id
```


## Too many open files

When putting a lot of files in a drop box you might run into the problem of '`too many open files error`'. Please consider changing the ulimit value of your host operating system to a higher value.