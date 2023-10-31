## Querying Project Database

In some customized versions of openBIS an additional project-specific
database is storing data from registered data sets. This database can be
queried via SQL Select statements in openBIS Web application. In order
to protect modification of this database by malicious SQL code openBIS
application server should access this database as a user which is member
of a read-only group. The name of this read-only group is project
specific.

```{note}
It is possible to configure openBIS to query multiple project-specific databases.
```


### Create Read-Only User in PostgreSQL

A new user (aka role) is created by

```sql
CREATE ROLE <read-only user> LOGIN NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
```

This new user is added to the read-only group by the following command:

```sql
GRANT <read-only group> TO <read-only user>;
```

The name of the read-only group can be obtained by having a look into
the list of all groups:

```sql
SELECT * from PG_GROUP;
```

*Note that by default openBIS creates a user* ` openbis_readonly `
*which has read-only permissions to all database objects. You can use
this user to access the openBIS meta database through the openBIS query
interface.*


### Enable Querying

To enable querying functionality for additional databases in openBIS Web  application a [core plugin](../../software-developer-documentation/server-side-extensions/core-plugins.md#core-plugins) of type query-databases has to be created. The following `plugin.properties` have to be specified:

| Property          | Description                                                                                                               |
|-------------------|---------------------------------------------------------------------------------------------------------------------------|
| label             | Label of the database. It will be used in the Web application in drop down lists for adding / editing customized queries. |
| database-driver   | JDBC Driver of the database, e.g. org.postgresql.Driver for postgresql.                                                  |
| database-url      | JDBC URL to the database containing full database name, e.g. jdbc:postgresql://localhost/database_name for postgresql     |
| database-username | Above-mentioned defined read-only user.                                                                                  |
| database-password | Password of the read-only user.                                                                                          |


### Configure Authorization for Querying

In order to configure authorization, two properties can be configured:

| Property                              | Description                                                                                                                                                                 |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <database>.data-space           | To which data-space this database belongs to (optional, i.e. a query database can be configured not to belong to one data space by leaving this configuration value empty). |
| <database>.creator-minimal-role | What role is required to be allowed to create / edit queries on this database (optional, default: INSTANCE_OBSERVER if data-space is not set, POWER_USER otherwise).       |

The given parameters data-space and creator-minimal-role are used by openBIS to enforce proper authorization.

For example, if

    data-space = CISD
    creator-minimal-role = SPACE_ADMIN

is configured, then for the query database configured with key `db1`:

- only a `SPACE_ADMIN` on data space `CISD` and an `INSTANCE_ADMIN` are allowed to create / edit queries,
- only a user who has the `OBSERVER` role in data space `CISD` is allowed to execute a query.

For query databases that do not belong to a space but that have a column with any of the [magic column names](../../user-documentation/general-admin-users/custom-database-queries.md#hyperlinks), the query result is filtered on a per-row basis according to what the user executing the query is allowed to see. In detail this means: if the user executing the query is not an instance admin, filter out all rows which belong to a data space where the user doesn't have a least the observer role. The relationship between a row and a data space is established by means of the experiment / sample / data set whose `permId` is given by one of the magical column names.

For sensitive data where authorization needs to be enforced, there are
two setups possible:

1. Configure a query database that **does not** belong to a data space
    and set the creator-minimal-role to `INSTANCE_ADMIN`. Any instance
    admin can be trusted to understand authorization issues and ensure
    that only queries are added for this query database that contain a
    proper reference to an experiment / sample / data set. This way, it
    can be ensured that only properly filtered query results are
    returned to the user running the query.
2. Configure a query database that **does** belong to a specific data
    space and set the creator-minimal-role to `POWER_USER`. The
    datastore server (or whatever server maintains the query database)
    ensures that only information related to the configured data space
    is added to the query database. Thus whatever query the power user
    writes for this database, it will only reveal information from this
    data space. As only users with `OBSERVER` role on this data space
    are allowed to execute the query, authorization is enforced properly
    without the need of filtering query results.