Custom Database Queries
=======================

Introduction
------------

openBIS application server can be configured to query any relational
database server via SQL. There are three ways to use this feature in
openBIS Web application:

-   Running arbitrary SELECT statements.
-   Defining parametrized queries.
-   Running parametrized queries.

The three features correspond to three menu items of the menu
**Queries**.

The last feature can be used by any user having OBSERVER role whereas
for the first two features user needs a **query creator** role which
usually is at least POWER\_USER role and is
[configured](../../system-admin-documentation/installation/installation-and-configuration-guide.md#configure-authorization)
by administrator of the openBIS server. The idea is that power users
having the knowledge to write SQL queries define a query which can be
used by everybody without knowing much about SQL.

Multiple query databases may be configured for any openBIS Web
application. Database labels specified in the configuration file will be
shown in a combo box for database selection while defining new / editing
existing queries.

Note that only the first 100000 rows of the result set of a query are
shown. This restriction should prevent from running ill-designed queries
which consume all the memory of the server. There is also a time out of
5 minutes defined after which the query is canceled if it didn't return
any result.

How it works
------------

Database:

-   is configured as a core-plugin of type "query-databases"
-   can be assigned to a space:
    -   space == null : should be used for databases that contain data
        from multiple spaces or data which is space unrelated
    -   space != null : should be used for databases that contain data
        from one specific space only
-   can be assigned a minimal query creator role:
    -   database with space == null : by default the minimal query
        creator role is INSTANCE\_OBSERVER
    -   database with space != null : by default the minimal query
        creator role is POWER\_USER

Query:

-   can be created/updated/deleted only by a user with a database
    minimal query creator role or stronger (if database space != null
    then the user role has to be defined for that space or the user has
    to be an instance admin)
-   can be seen by:
    -   private query : a user who created it or an instance admin
    -   public query : any user
-   can be executed by:
    -   database with space == null : by users with at least
        PROJECT\_OBSERVER role (results are filtered by a
        experiment\_key/sample\_key/data\_set\_key column values which
        are expected to contain entity perm\_id; WARNING: if no such
        column is returned by a query then ALL results are returned)
    -   database with space != null : by users with at least
        SPACE\_OBSERVER role in that space (all results are returned
        without any filtering as they all belong to the space a user has
        access to)
-   can be updated/executed/deleted only by a user who can see the query
-   can contain additional parameters (e.g. ${my\_parameter}); values of
    such parameters can be set in the UI by a user right before an
    execution of a query
-   can be GENERIC (accessible only from the "Queries" top menu) or
    EXPERIMENT/SAMPLE/DATA\_SET/MATERIAL specific (accessible from the
    "Queries" top menu and from Experiment/Sample/DataSet/Material view
    respectively)
-   entity specific queries should contain '${key}' parameter which will
    be replaced by a permId of the displayed experiment/sample or by a
    code of the displayed dataset/material before the query execution
    (MATERIAL queries also have '${type}' parameter which is replaced
    with a type code of the material)
-   entity specific queries may be configured to appear only in the
    views of entities of chosen types (e.g. only for samples of types
    that match a given regexp)

Arbitrary SQL:

-   running an arbitrary SQL is treated as a creation of a query which
    is simply not stored for a future use i.e. only a user with a
    minimal query creator role or stronger can do it (if database space
    != null then the user role has to be defined for that space or the
    user has to be an instance admin)

Setup
-----

To use the custom database queries, it is necessary to define query databases. See [Installation and Administrator Guide of the openBIS Server](../../system-admin-documentation/installation/installation-and-configuration-guide.md) for an explanation on how to do this.

Running a Parametrized Query
----------------------------

1.  Choose menu item **Queries -> Run Predefined Query**. The tab
    *Predefined Query* opens.
2.  Choose a query using the query combo box. Queries specified for all
    configured databases are selected transparently using the same combo
    box which displays only query names.
3.  If the query has no parameters it will be executed immediately and
    the result is shown in tabular form. Otherwise text fields for each
    parameter appear right of the query combo box.
4.  Enter some values into the parameter fields and click on the
    **Execute** button. The query result will be shown as a table.

Features of a query result:

-   The result can be browsed, exported, sorted, and filtered as most
    tables in openBIS.
-   Values referring to permIDs of an experiment, sample, or data set
    might be shown as hyperlinks. A click on such a link opens a new tab
    with details.

Running a SELECT statement
--------------------------

This feature is only for users with *creator role*. It is useful for
exploring the database by ad hoc queries.

1.  Choose menu item **Queries -> Run Custom SQL Query**. The tab
    *Custom SQL Query* opens.
2.  Enter a SELECT statement in the text area, select database and click
    on the **Execute** button. The result appears below in tabular form.

Defining and Editing Parametrized Queries
-----------------------------------------

This feature is only for users with *creator role*.

### Define a Query

1.  Choose menu item **Queries -> Browse Query Definitions**. The tab
    *Query Definitions* opens. It shows all definitions where the user
    has access rights.
2.  Click on **Add Query Definition** for defining a new parametrized
    query. A large dialog pops up.
3.  Enter a name, database, an optional description, and a SELECT
    statement.
4.  Click on button **Test Query Definition** to execute the query. The
    result will be shown in the same dialog.
5.  Click on button **Save** to save the definition. The dialog
    disappears and the new definition appears in the table of query
    definitions.

#### Public flag

A query definition can be public or private depending on whether the
check box **public** is checked or not. A private query is visible only
by its creator. Public queries are visible by everybody. The idea is
that a power user first creates query definitions for their own
purposes. If he or she find it useful for other users they will set the
public flag.

#### Specifying Parameters

A SQL query can have parameters which are defined later by the user
running the query. A parameter is of the form `${<parameter name>`}.
Example:

```sql
select * from my_table where code = ${my table code}
```

The parameter name will appear in the text field when running the query.
Optionally, you can provide key-value pairs which are "metadata" for the
parameter name and separated by '::' from the name. These metadata keys
are defined:

|Metadata key|Explanation|Example|
|--- |--- |--- |
|type|Sets the data type of this parameter. Valid values are VARCHAR (or STRING), CHAR, INTEGER, BIGINT, FLOAT, DOUBLE, BOOLEAN, TIME, DATE or TIMESTAMP.|${code::type=VARCHAR}|
|list|Coma-separated list of allowed values for the parameter.|${color::list=red,green,blue}|
|query|A SQL query which is run to determine the allowed values for the parameter. The query is expected to return exactly one column. You should specify only fast queries here with a reasonably small number of returned rows as the UI will block until this query has returned.|${name::query=select last_name from users}|

It is possible to combine multiple keys like
this: `${estimate::type=integer::list=1,3,7,12`}.

```{warning}
**Why to provide a data type**
Providing a data type with `type=...` is not mandatory. In a future version of the software we may add additional client-side validation based on this value, but in the current version we don't do that yet. If you do *not* provide a data type, openBIS will ask the database for the type of the particular query parameter. This works fine for most databases, but not for all. Oracle is a well-known example that cannot provide this information. So if your query source is an Oracle database and you do not provide a data type, you will get an error saying` "Unsupported feature`". To fix this, you have to rovide the data type.
```

#### Array Literals for PostgreSQL data sources

For PostgreSQL, there exist neat array functions `ANY` and `ALL` (see
[PostgreSQL
documentation](http://www.postgresql.org/docs/9.2/static/functions-comparisons.html)).
In particularly `ANY` comes in handy in `WHERE` clauses to check whether
a column has one of several values. The official form for providing an
array literal as a string (which is what you have to do here) is a bit
clumsy, as you have to write for the query
`"select * from data where code = ANY(${codes}::text[])`" and then the
user running the query has to put the parameter value in curly braces
like "`{code1,code2,code3,...}`".

The custom query engine has a simplification for this construct. You can
just write: `"select * from data where code = ANY({${codes}})`" for the
query and then the user running the query will be able to skip the curly
bracket and write for the parameter value: "`code1,code2,code3,...`". A
user who doesn't know that this is an array will in particular get away
with just providing a single value like "`code1`".

Note that the most obvious way of specifying a set relationship with
`"select * from data where code in (${codes})`" does *not* work as
custom queries are not using simple text concatenation but prepared
queries to avoid a security problem known as "SQL Injection".

#### Hyperlinks

In order to create hyperlinks in the result table the column names in
the SQL statement should be one of the following **magic** words:

-   `experiment_key`

-   `sample_key`

-   `data_set_key`  
    They should denote a perm ID of specified type.  
    Example:
    ```sql
    select id, perm_id as data_set_key from data_sets
    ```
```{warning}
**Be careful with this feature**: The table is shown with the hyperlinks even if the value isn't a perm ID of specified type.
```

### Edit a Query

1.  Choose menu item **Queries -> Browse Query Definitions**. The tab
    *Query Definitions* opens.
2.  Select a query and click on button **Edit**. The same dialog as for
    defining a query pops up.

Entity Queries (Experiment, Sample, Material, Data Set)
-------------------------------------------------------

By default, all custom queries are `Generic`, which means that the user
will be able to execute them from the standard Queries menu.

Additionally it is possible to create a query containing a special
'magic' parameter, which will be automatically replaced by the entity
identifier (perm id in case of experiments and samples, code for data
sets and a pair (code, type) in case of materials). Those entity
specific queries will be visible only in entity details views (e.g.
experiment details) in a special `section` called `Queries`. One can
also limit visibility of a query to a specific entity types (e.g.
experiment of type `EXP`).

![image info](img/359.png)

### How to create/edit entity custom queries

Entity custom queries can be created and edited in the same way as
`Generic` queries (**Queries -> Browse Query Definitions**), but the
value of **`Query Type`** field should be set to Experiment, Sample,
Data Set or Material.

**`Entity Type`** (e.g. Experiment Type) should be changed if one wants
to limit the visibility of a query to a specific type (default option -
`(all)`, doesn't introduce such a restriction). The field accepts not
only values selected from the list but also typed text containing a
regular expression (e.g. Experiment Type `'EXP.*'` would mean that the
query should be visible in views of experiments of type with code
starting with `'EXP'` prefix).

Furthermore the sql should contain the 'magic' parameter **'${key}'**
(will be replaced by perm id (experiment, sample) or code (data set,
material)). In case of material custom query, additional 'magic'
parameter is required: **'${type}'** (will be replaced by material type
code).

![image info](img/382.png)

### Examples

![image info](img/502.png)

```{warning}
**Legacy Syntax:**
Older versions of openBIS required to put string parameters in ticks, like '${param}'. Current versions of openBIS don't need this anymore, so you can use ${param} without the ticks. However, the syntax with ticks is still accept for backward compatibility.
```
