# Java / Javascript (V3 API) - openBIS V3 API

## I. Architecture

Open BIS consists of two main components: an Application Server and one
or more Data Store Servers. The Application Server manages the system’s
meta data, while the Data Store Server(s) manage the file store(s). Each
Data Store Server manages its own file store. Here we will refer to the
Application Server as the "AS" and the Data Store Server as the "DSS."

### One AS, one or more DSS

Why is there only one Application Server but multiple Data Store
Servers? It is possible to have only one Data Store Server, but in a
complex project there might be many labs using the same OpenBIS instance
and therefore sharing the same meta data. Each lab might have its own
Data Store Server to make file management easier and more efficient. The
Data Store Servers are on different Java virtual machines, which enables
the files to be processed faster. It is also more efficient when the
physical location of the Data Store Server is closer to the lab that is
using it. Another reason is that the meta data tends to be relatively
small in size, whereas the files occupy a large amount of space in the
system. 

![image info](img/139.png)

### The Java API

The Java V3 API consists of two interfaces:

-   ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerAPI
-   ch.ethz.sis.openbis.generic.dssapi.v3.IDatastoreServerAPI

Please check our JavaDoc for more
details: <https://openbis.ch/javadoc/20.10.x/javadoc-api-v3/index.html>

All V3 API jars are packed in openBIS-API-V3-<VERSION>.zip which
is part of openBIS-clients-and-APIs-<VERSION>.zip (the latest version can be downloaded at [Sprint Releases](#) > Clients and APIs)

### The Javascript API

The Javascript V3 API consists of a module hosted at
<OPENBIS\_URL>/resources/api/v3/openbis.js, for instance
<http://localhost/openbis>/ resources/api/v3/openbis.js. Please check
the openbis.js file itself for more details.

## II. API Features

### Current Features - AS

The current implementation of the V3 openBIS API contains the following
features:

-   Creation:  Create spaces, projects, experiments and experiment
    types, samples and sample types, materials and material types,
    vocabulary terms, tags
-   Associations: Associate spaces, project, experiments, samples,
    datasets, materials to each other
-   Tags: Add/Remove/Set tags for experiments, samples, datasets and
    materials
-   Properties: Set properties for experiments, samples, datasets and
    materials
-   Search: Search & get spaces, project, experiments, samples,
    datasets, materials, vocabulary terms, tags
-   Update: Update spaces, project, experiments, samples, datasets,
    materials, vocabulary terms, tags
-    Deletion: Delete spaces, project, experiments, samples, datasets,
    materials, vocabulary terms, tags
-   Authentication: Login as user, login as another user, login as an
    anonymous user
-   Transactional features: performing multiple operations in one
    transaction (with executeOperations method)
-   Queries: create/update/get/search/delete/execute queries
-   Generating codes/permids

### Current Features - DSS

-   Search data set files
-   Download data set files

### Missing/Planned Features

The current implementation of the V3 openBIS API does not yet include
the following features:

-   Management features: Managing data stores
-   Search features: Searching experiments having samples/datasets,
    searching datasets (oldest, deleted, for archiving etc.)
-   Update features: Updating datasets share id, size, status, storage
    confirmation, post registration status

## III. Accessing the API 

In order to use V3 API you have to know the url of an openBIS instance
you want to connect to. Moreover, before calling any of the API methods
you have to login to the system to receive a sessionToken. All the login
methods are part of the AS API. Once you successfully authenticate in
openBIS you can invoke other methods of the API (at both AS and DSS). In
each call you have to provide your sessionToken. When you have finished
working with the API you should call logout method to release all the
resources related with your session.

 

Note: If the openBIS instance you are connecting to uses SSL and does
not have a real certificate (it is using the self-signed certificate
that comes with openBIS), you need to tell the java client to use the
trust store that comes with openBIS. This can be done by setting the
property [javax.net](http://javax.net).ssl.trustStore. Example:

 

**Using openBIS trust store in Java clients**

```bash
    java -Djavax.net.ssl.trustStore=/home/openbis/openbis/servers/openBIS-server/jetty/etc/openBIS.keystore -jar the-client.jar
```

Connecting in Java

**V3ConnectionExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3ConnectionExample
    {

        private static final String URL = "http://localhost:8888/openbis/openbis" + IApplicationServerApi.SERVICE_URL;

        private static final int TIMEOUT = 10000;

        public static void main(String[] args)
        {
            // get a reference to AS API
            IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);

            // login to obtain a session token
            String sessionToken = v3.login("admin", "password");

            // invoke other API methods using the session token, for instance search for spaces
            SearchResult<Space> spaces = v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
            System.out.println("Number of spaces: " + spaces.getObjects().size());

            // logout to release the resources related with the session
            v3.logout(sessionToken);
        }


    }
```

### Connecting in Javascript

We have put a lot of effort to make the use of the API in Javascript and
Java almost identical. The DTOs which are a big part of the API are
exactly the same in both languages. The methods you can invoke via the
Javascript and Java APIs are also exactly the same. This makes the
switch from Javascript to Java or the other way round very easy. Because
of some major differences between Javascript and Java development still
some things had to be done a bit differently. But even then we tried to
be conceptually consistent.

**V3ConnectionExample.html**

```html
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <title>V3ConnectionExample</title>
        <!-- 
            These two js files, i.e. config.js and require.js are RequireJS configuration and RequireJS library itself.
            Please check http://requirejs.org/ for more details on how RequireJS makes loading dependencies in Javascript easier.
        -->
        <script type="text/javascript" src="http://localhost:8888/openbis/resources/api/v3/config.js"></script>
        <script type="text/javascript" src="http://localhost:8888/openbis/resources/api/v3/require.js"></script>
    </head>
    <body>
        <script>


            // With "require" call we asynchronously load "openbis", "SpaceSearchCriteria" and "SpaceFetchOptions" classes that we will need for our example.
            // The function that is passed as a second parameter of the require call is a callback that gets executed once requested classes are loaded.
            // In Javascript we work with exactly the same classes as in Java. For instance, "ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria"
            // Java class and "as/dto/space/search/SpaceSearchCriteria" Javascript class have exactly the same methods. In order to find a Javascript class name please
            // check our Javadoc (https://openbis.ch/javadoc/20.10.x/javadoc-api-v3/index.html). The Javascript class name is defined in @JsonObject annotation of each V3 API Java DTO.


            require([ "openbis", "as/dto/space/search/SpaceSearchCriteria", "as/dto/space/fetchoptions/SpaceFetchOptions" ], function(openbis, SpaceSearchCriteria, SpaceFetchOptions) {

                // get a reference to AS API
                var v3 = new openbis();

                // login to obtain a session token (the token it is automatically stored in openbis object and will be used for all subsequent API calls)
                v3.login("admin", "password").done(function() {

                    // invoke other API methods, for instance search for spaces
                    v3.searchSpaces(new SpaceSearchCriteria(), new SpaceFetchOptions()).done(function(result) {

                        alert("Number of spaces: " + result.getObjects().length);

                        // logout to release the resources related with the session
                        v3.logout();
                    });
                });
            });
        </script>
    </body>
    </html>
```
  

##   IV. AS Methods  

The sections below describe how to use different methods of the V3 API.
Each section describes a group of similar methods. For instance, we have
one section that describes creation of entities. Even though the API
provides us methods for creation of spaces, projects, experiments,
samples and materials, vocabulary terms, tags we only concentrate here
on creation of samples. Samples are the most complex entity kind. Once
you understand how creation of samples works you will also know how to
create other kinds of entities as all creation methods follow the same
patterns. The same applies for other methods like updating of entities,
searching or getting entities. We will introduce them using the sample
example.

Each section will be split into Java and Javascript subsections. We want
to keep Java and Javascript code examples close to each other so that
you can easily see what are the similarities and differences in the API
usage between these two languages.

NOTE: The following code examples assume that we have already got a
reference to the V3 API and we have already authenticated to get a
session token. Moreover in Javascript example we do not include the html
page template to make them shorter and more readable. Please
check "Accessing the API" section for examples on how to get a reference
to V3 API, authenticate or build a simple html page.

### Login

OpenBIS provides the following login methods:

-   login(user, password) - login as a given user
-   loginAs(user, password, asUser) - login on behalf of a different
    user (e.g. I am an admin but I would like to see only things user
    "x" would normally see)
-   loginAsAnonymousUser() - login as an anonymous user configured in AS
    service.properties

All login methods return a session token if the provided parameters were
correct. In case a given user does not exist or the provided password
was incorrect the login methods return null.

#### Example

**V3LoginExample.java**

```java
    public class V3LoginExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created (please check "Accessing the API" section for more details)
     
            // login as a specific user
            String sessionToken = v3.login("admin", "password");
            System.out.println(sessionToken);

            // login on behalf of a different user (I am an admin but I would like to see only things that some other user would normally see)
            sessionToken = v3.loginAs("admin", "password", "someotheruser");
            System.out.println(sessionToken);

            // login as an anonymous user (anonymous user has to be configured in service.properties first)
            sessionToken = v3.loginAsAnonymousUser();
            System.out.println(sessionToken);
        }
    }
```


**V3LoginExample.html**

```html
    <script>
     
        // we assume here that v3 object has been already created (please check "Accessing the API" section for more details)
     
        // login as a specific user
        v3.login("admin", "password").done(function(sessionToken) {
            alert(sessionToken);

            // login on behalf of a different user (I am an admin but I would like to see only things that some other user would normally see)
            v3.loginAs("admin", "password", "someotheruser").done(function(sessionToken) {
                alert(sessionToken);

                // login as an anonymous user (anonymous user has to be configured in service.properties first)
                v3.loginAsAnonymousUser().done(function(sessionToken) {
                    alert(sessionToken);
                });
            });
        });
    </script>
```

### Personal Access Tokens

A personal access token (in short: PAT) can be thought of as a longer lived session token which can be used for integrating openBIS with external systems. If you would like to learn more about the idea behind PATs please read: [Personal Access Tokens](https://openbis.readthedocs.io/en/latest/software-developer-documentation/apis/personal-access-tokens.html#personal-access-tokens)).

Example of how to create and use a PAT:

```java
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.Map;

    import org.apache.commons.lang.time.DateUtils;

    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;

    public class V3PersonalAccessTokenExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
            creation.setSessionName("test session");
            creation.setValidFromDate(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
            creation.setValidToDate(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY));

            // create and get the new PAT
            List<PersonalAccessTokenPermId> ids = v3api.createPersonalAccessTokens(sessionToken, Arrays.asList(creation));
            Map<IPersonalAccessTokenId, PersonalAccessToken> map = v3api.getPersonalAccessTokens(sessionToken, ids, new PersonalAccessTokenFetchOptions());
            PersonalAccessToken pat = map.get(ids.get(0));

            // use the new PAT to list spaces
            v3api.searchSpaces(pat.getHash(), new SpaceSearchCriteria(), new SpaceFetchOptions());
        }
    }
```
### Session Information

OpenBIS provides a method to obtain the session information for an
already log in user:

#### Example


**V3CreationExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;

    public class V3SessionInformationExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
            SessionInformation sessionInformation = v3.getSessionInformation(sessionToken);
            System.out.println("User Name: " + sessionInformation.getUserName());
            System.out.println("Home Group: " + sessionInformation.getHomeGroupCode());
            System.out.println("Person: " + sessionInformation.getPerson());
            System.out.println("Creator Person: " + sessionInformation.getCreatorPerson());
        }
    }
```

### Creating entities

The methods for creating entities in V3 API are called: createSpaces,
createProjects, createExperiments, createSamples, createMaterials,
createVocabularyTerms, createTags. They all allow to create one or more
entities at once by passing one or more entity creation objects (i.e.
SpaceCreation, ProjectCreation, ExperimentCreation, SampleCreation,
MaterialCreation, VocabularyTermCreation, TagCreation). All these
methods return as a result a list of the new created entity perm ids.

NOTE: Creating data sets via V3 API is not available yet. The new V3
dropboxes are planned but not implemented yet. Please use V2 dropboxes
until V3 version is out.

#### Example

**V3CreationExample.java**

```java
    import java.util.List;
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
    public class V3CreationExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            // you can also pass more than one creation object to create multiple entities at once
            List<SamplePermId> permIds = v3.createSamples(sessionToken, Arrays.asList(sample));
            System.out.println("Perm ids: " + permIds);    
        }
    }
```

**V3CreationExample.html**

```html
    <script>
            require([ "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier" ], 
                function(SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier) {
     
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                // you can also pass more than one creation object to create multiple entities at once
                v3.createSamples([ sample ]).done(function(permIds) {
                    alert("Perm ids: " + JSON.stringify(permIds));
                });
        });
    </script>
```

#### Properties example

**V3CreationWithPropertiesExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3CreationWithPropertiesExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            // examples of value formats that should be used for different types of properties
            sample.setProperty("MY_VARCHAR", "this is a description");
            sample.setProperty("MY_INTEGER", "123");
            sample.setProperty("MY_REAL", "123.45");
            sample.setProperty("MY_BOOLEAN", "true");
            sample.setProperty("MY_MATERIAL", "MY_MATERIAL_CODE (MY_MATERIAL_TYPE_CODE)");
            sample.setProperty("MY_VOCABULARY", "MY_TERM_CODE");

            v3.createSamples(sessionToken, Arrays.asList(sample));
        }
    }
```


**V3CreationWithPropertiesExample.html**

```html
    <script>
        require([ "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier" ],
            function(SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                // examples of value formats that should be used for different types of properties
                sample.setProperty("MY_VARCHAR", "this is a description");
                sample.setProperty("MY_INTEGER", "123");
                sample.setProperty("MY_REAL", "123.45");
                sample.setProperty("MY_BOOLEAN", "true");
                sample.setProperty("MY_MATERIAL", "MY_MATERIAL_CODE (MY_MATERIAL_TYPE_CODE)");
                sample.setProperty("MY_VOCABULARY", "MY_TERM_CODE");

                v3.createSamples([ sample ]).done(function(permIds) {
                    alert("Perm ids: " + JSON.stringify(permIds));
                });
            });
        });
    </script>
```

#### Different ids example


**V3CreationWithDifferentIdsExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3CreationWithDifferentIdsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            // as an experiment id we can use any class that implements IExperimentId interface. For instance, experiment identifier:
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            // or experiment perm id:
            sample.setExperimentId(new ExperimentPermId("20160115170718361-98668"));

            v3.createSamples(sessionToken, Arrays.asList(sample));
        }
    }
```

**V3CreationWithDifferentIdsExample.html**

```html
    <script>
        require([ "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/experiment/id/ExperimentPermId" ], 
            function(SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, ExperimentPermId) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                // as an experiment id we can use any class that implements IExperimentId interface. For instance, experiment identifier:
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                // or experiment perm id:
                sample.setExperimentId(new ExperimentPermId("20160115170718361-98668"));

                v3.createSamples([ sample ]).done(function(permIds) {
                    alert("Perm ids: " + JSON.stringify(permIds));
                });
            });
    </script>
```
#### Parent child example

The following example creates parent and child samples for a sample type
which allow automatic code generation:

**V3CreationParentAndChildExample**

```java
    import java.util.Arrays;
    import java.util.List;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3CreationParentAndChildExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation parentSample = new SampleCreation();
            parentSample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            parentSample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            parentSample.setCreationId(new CreationId("parent"));
     
            SampleCreation childSample = new SampleCreation();
            childSample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            childSample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            childSample.setParentIds(Arrays.asList(parentSample.getCreationId()));
            
            List<SamplePermId> permIds = v3.createSamples(sessionToken, Arrays.asList(parentSample, childSample));
            System.out.println("Perm ids: " + permIds);
        }
    }
```

**V3CreationParentAndChildExample.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/common/id/CreationId" ], 
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, CreationId) {

    // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var parentSample = new SampleCreation();
                parentSample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                parentSample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                parentSample.setCreationId(new CreationId("parent"));
                var childSample = new SampleCreation();
                childSample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                childSample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                childSample.setParentIds([parentSample.getCreationId()]);
                v3.createSamples([ parentSample, childSample ]).done(function(permIds) {
                    alert("Perm ids: " + JSON.stringify(permIds));
                });
            });
    </script>
```

### Updating entities

The methods for updating entities in V3 API are called: updateSpaces,
updateProjects, updateExperiments, updateSamples, updateDataSets,
updateMaterials, updateVocabularyTerms, updateTags. They all allow to
update one or more entities at once by passing one or more entity update
objects (i.e. SpaceUpdate, ProjectUpdate, ExperimentUpdate,
SampleUpdate, MaterialUpdate, VocabularyTermUpdate, TagUpdate). With
update objects you can update entities without fetching their state
first, i.e. the update objects contain only changes - not the full state
of entities. All update objects require an id of an entity that will be
updated. Please note that some of the entity fields cannot be changed
once an entity is created, for instance sample code becomes immutable
after creation.

#### Example

**V3UpdateExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;

    public class V3UpdateExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            // here we update a sample and attach it to a different experiment
            SampleUpdate sample = new SampleUpdate();
            sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_OTHER_EXPERIMENT_CODE"));

            // you can also pass more than one update object to update multiple entities at once
            v3.updateSamples(sessionToken, Arrays.asList(sample));
            System.out.println("Updated");
        }
    }
```

**V3UpdateExample.html**

```html
    <script>
        require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SampleIdentifier", "as/dto/experiment/id/ExperimentIdentifier" ], 
            function(SampleUpdate, SampleIdentifier, ExperimentIdentifier) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                // here we update a sample and attach it to a different experiment
                var sample = new SampleUpdate();
                sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_OTHER_EXPERIMENT_CODE"));

                // you can also pass more than one update object to update multiple entities at once
                v3.updateSamples([ sample ]).done(function() {
                    alert("Updated");
                });
            });
    </script>
```

#### Properties example

**V3UpdateWithPropertiesExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;

    public class V3UpdateWithPropertiesExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            SampleUpdate sample = new SampleUpdate();
            sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));

            // examples of value formats that should be used for different types of properties
            sample.setProperty("MY_VARCHAR", "this is a description");
            sample.setProperty("MY_INTEGER", "123");
            sample.setProperty("MY_REAL", "123.45");
            sample.setProperty("MY_BOOLEAN", "true");
            sample.setProperty("MY_MATERIAL", "MY_MATERIAL_CODE (MY_MATERIAL_TYPE_CODE)");
            sample.setProperty("MY_VOCABULARY", "MY_TERM_CODE");

            v3.updateSamples(sessionToken, Arrays.asList(sample));

            System.out.println("Updated");
        }
    }
```

**V3UpdateWithPropertiesExample.html**

```html
    <script>
        require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SampleIdentifier" ], function(SampleUpdate, SampleIdentifier) {

            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            var sample = new SampleUpdate();
            sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));

            // examples of value formats that should be used for different types of properties
            sample.setProperty("MY_VARCHAR", "this is a description");
            sample.setProperty("MY_INTEGER", "123");
            sample.setProperty("MY_REAL", "123.45");
            sample.setProperty("MY_BOOLEAN", "true");
            sample.setProperty("MY_MATERIAL", "MY_MATERIAL_CODE (MY_MATERIAL_TYPE_CODE)");
            sample.setProperty("MY_VOCABULARY", "MY_TERM_CODE");

            v3.updateSamples([ sample ]).done(function() {
                alert("Updated");
            });
        });
    </script>
```

#### Parents example

**V3UpdateWithParentsExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;

    public class V3UpdateWithParentsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            // Let's assume the sample we are about to update has the following parents:
            // - MY_PARENT_CODE_1
            // - MY_PARENT_CODE_2

            SampleUpdate sample = new SampleUpdate();
            sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));

            // We can add and remove parents from the existing list. For instance, here we are adding: MY_PARENT_CODE_3 and removing: MY_PARENT_CODE_1.
            // The list of parents after such change would be: [MY_PARENT_CODE_2, MY_PARENT_CODE_3]. Please note that we don't have to fetch the existing
            // list of parents, we are just defining what changes should be made to this list on the server side. Updating lists of children or contained
            // samples works exactly the same.

            sample.getParentIds().add(new SampleIdentifier("/MY_SPACE_CODE/MY_PARENT_CODE_3"));
            sample.getParentIds().remove(new SampleIdentifier("/MY_SPACE_CODE/MY_PARENT_CODE_1"));

            // Instead of adding and removing parents we can also set the list of parents to a completely new value.
            sample.getParentIds().set(new SampleIdentifier("/MY_SPACE_CODE/MY_PARENT_CODE_2"), new SampleIdentifier("/MY_SPACE_CODE/MY_PARENT_CODE_3"));

            v3.updateSamples(sessionToken, Arrays.asList(sample));
     
            System.out.println("Updated");
        }
    }
```

**V3UpdateWithParentsExample.html**

```html
    <script>
        require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SampleIdentifier" ], 
            function(SampleUpdate, SampleIdentifier) {
                
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                // Let's assume the sample we are about to update has the following parents:
                // - MY_PARENT_CODE_1
                // - MY_PARENT_CODE_2

                var sample = new SampleUpdate();
                sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));

                // We can add and remove parents from the existing list. For instance, here we are adding: MY_PARENT_CODE_3 and removing: MY_PARENT_CODE_1.
                // The list of parents after such change would be: [MY_PARENT_CODE_2, MY_PARENT_CODE_3]. Please note that we don't have to fetch the existing
                // list of parents, we are just defining what changes should be made to this list on the server side. Updating lists of children or contained
                // samples works exactly the same.

                sample.getParentIds().add(new SampleIdentifier("/MY_SPACE/MY_PARENT_CODE_3"));
                sample.getParentIds().remove(new SampleIdentifier("/MY_SPACE/MY_PARENT_CODE_1"));

                // Instead of adding and removing parents we can also set the list of parents to a completely new value.
                sample.getParentIds().set(new SampleIdentifier("MY_SPACE/MY_PARENT_CODE_2"), new SampleIdentifier("MY_SPACE/MY_PARENT_CODE_3"));

                v3.updateSamples([ sample ]).done(function() {
                    alert("Updated");
                });
            });
    </script>
```
### Getting authorization rights for entities

If the user isn't allowed to create or update an entity an exception is
thrown. But often a client application wants to know in advance whether
such operations are allowed or not. With the API method `getRights()`
authorizations rights for specified entities can be requested. Currently
only creation and update authorization rights for projects, experiments,
samples and data sets (only update right) are returned.

In order to check whether an entity can be created or not a dummy
identifier has to be provided when calling `getRights()`. This
identifier should be a wellformed identifier which specifies the entity
to which such a new entity belongs. For example, calling `getRights()`
with `new ExperimentIdentifier("/MY-SPACE/PROJECT1/DUMMY")` would return
rights containing `CREATE` if the user is allowed to create an
experiment in the project `/MY-SPACE/PROJECT1`.

### Freezing entities

An entity (Space, Project, Experiment, Sample, Data Set) can be frozen.
There are two types of frozen: *Core* and *surface*. A frozen core means
that certain attributes of the entity can not be changed but still
connections between entities can be added or removed. A frozen surface
implies a frozen core and frozen connections of particular types. To
freeze an entity it has to be updated by invoking at least one freeze
method on the update object. Example:

     SampleUpdate sample = new SampleUpdate();
     sample.setSampleId(new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE"));
     sample.freezeForChildren();
     v3.updateSamples(sessionToken, Arrays.asList(sample));

```{warning}
Freezing can not be reverted.
```

The timestamp of freezing, the types of freezing, the user and the
identifier of the frozen entity will be stored in the database as a
freezing event.

The following tables show all freezing possibilities and what is actual
frozen.

#### Space

|Freezing method|Description|
|--- |--- |
|freeze|The specified space can not be deleted.
The description can not be set or changed.|
|freezeForProjects|Same as freeze() plus no projects can be added to or removed from the specified space.|
|freezeForSamples|Same as freeze() plus no samples can be added to or removed from the specified space.|

#### Project

|Freezing method|Description|
|--- |--- |
|freeze|The specified project can not be deleted.
The description can not be set or changed.
No attachments can be added or removed.|
|freezeForExperiments|Same as freeze() plus no experiments can be added to or removed from the specified project.|
|freezeForSamples|Same as freeze() plus no samples can be added to or removed from the specified project.|

#### Experiment

|Freezing method|Description|
|--- |--- |
|freeze|The specified experiment can not be deleted.
No properties can be added, removed or modified.
No attachments can be added or removed.|
|freezeForSamples|Same as freeze() plus no samples can be added to or removed from the specified experiment.|
|freezeForDataSets|Same as freeze() plus no data sets can be added to or removed from the specified experiment.|

#### Sample

|Freezing method|Description|
|--- |--- |
|freeze|The specified sample can not be deleted. 
No properties can be added, removed or modified.
No attachments can be added or removed.|
|freezeForComponents|Same as freeze() plus no component samples can be added to or removed from the specified sample.|
|freezeForChildren|Same as freeze() plus no child samples can be added to or removed from the specified sample.|
|freezeForParents|Same as freeze() plus no parent samples can be added to or removed from the specified sample.|
|freezeForDataSets|Same as freeze() plus no data sets can be added to or removed from the specified sample.|

#### Data Set

|Freezing method|Description|
|--- |--- |
|freeze|The specified data set can not be deleted.
No properties can be added, removed or modified.
Content copies can be still added or removed for frozen link data sets.|
|freezeForChildren|Same as freeze() plus no child data sets can be added to or removed from the specified data set.|
|freezeForParents|Same as freeze() plus no parent data sets can be added to or removed from the specified data set.|
|freezeForComponents|Same as freeze() plus no component data sets can be added to or removed from the specified data set.|
|freezeForContainers|Same as freeze() plus no container data sets can be added to or removed from the specified data set.|

### Searching entities

The methods for searching entities in V3 API are called: `searchSpaces`,
`searchProjects`, `searchExperiments`, `searchSamples`,
`searchDataSets`,
`searchMaterials`, `searchVocabularyTerms, searchTags`, `searchGlobally`.

They all take criteria and fetch options objects as an input. The
criteria object allows you to specify what entities you are looking for.
For instance, only entities from a given space, entities of a given
type, entities with a property X that equals Y and much much more.

The fetch options object allows you to tell the API which parts of the
entities found should be fetched and returned as a result of the method
call. For instance, you can tell the API to return the results only with
properties because this is all what you will need for your processing.
This gives you a very fine grained control over how much data you
actually fetch from the server. The less you ask for via fetch options
the less data the API has to load from the database and the less data it
will have to transfer over the network. Therefore by default, the fetch
options object is empty, i.e. it tells the API only to fetch the basic
information about a given entity, i.e. its id, attributes and creation
and registration dates. If you want to fetch anything more then you have
to let the API know via fetch options which parts you are also
interested in.

Another functionality that the fetch options object provides is
pagination (see FetchOptions.from(Integer) and
FetchOptions.count(Integer) methods). With pagination a user can control
if a search method shall return all found results or just a given
subrange. This is especially useful for handling very large numbers of
results e.g. when we want to build a UI to present them. In such a
situation, we can perform the search that returns only the first batch
of results (e.g. the first 100) for the UI to be responsive and ask for
another batch only if a user requests that (e.g. via clicking on a next
page button in the UI). The pagination is available in all the search
methods including the global search (i.e. searchGlobally method). A code
example on how to use the pagination methods is presented below.

Apart from the pagination the fetch options also provides the means to
sort the results (see FetchOptions.sortBy() method). What fields can be
used for sorting depends on the search method and the returned objects.
Results can be sorted ascending or descending. Sorting by multiple
fields is also possible (e.g. first sort by type and then by
identifier). A code example on how to use sorting is presented below.

#### Example

**V3SearchExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;

    public class V3SearchExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            // search for samples that are in space with code MY_SPACE_CODE and are of sample type with code MY_SAMPLE_TYPE_CODE
            SampleSearchCriteria criteria = new SampleSearchCriteria();
            criteria.withSpace().withCode().thatEquals("MY_SPACE_CODE");
            criteria.withType().withCode().thatEquals("MY_SAMPLE_TYPE_CODE");

            // tell the API to fetch properties for each returned sample
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            fetchOptions.withProperties();

            SearchResult<Sample> result = v3.searchSamples(sessionToken, criteria, fetchOptions);

            for (Sample sample : result.getObjects())
            {
                // because we asked for properties via fetch options we can access them here, otherwise NotFetchedException would be thrown by getProperties method
                System.out.println("Sample " + sample.getIdentifier() + " has properties: " + sample.getProperties());
            }
        }
    }
```

**V3SearchExample.html**

```html
    <script>
        require([ "as/dto/sample/search/SampleSearchCriteria", "as/dto/sample/fetchoptions/SampleFetchOptions" ], 
            function(SampleSearchCriteria, SampleFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                // search for samples that are in space with code MY_SPACE_CODE and are of sample type with code MY_SAMPLE_TYPE_CODE
                var criteria = new SampleSearchCriteria();
                criteria.withSpace().withCode().thatEquals("MY_SPACE_CODE");
                criteria.withType().withCode().thatEquals("MY_SAMPLE_TYPE_CODE");

                // tell the API to fetch properties for each returned sample
                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withProperties();

                v3.searchSamples(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(sample) {
                        // because we asked for properties via fetch options we can access them here, otherwise NotFetchedException would be thrown by getProperties method
                        alert("Sample " + sample.getIdentifier() + " has properties: " + JSON.stringify(sample.getProperties()));
                    });
                });
            });
    </script>
```

#### Example with pagination and sorting

**V3SearchWithPaginationAndSortingExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;

    public class V3SearchWithPaginationAndSortingExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleSearchCriteria criteria = new SampleSearchCriteria();
            SampleFetchOptions fetchOptions = new SampleFetchOptions();

            // get the first 100 results
            fetchOptions.from(0);
            fetchOptions.count(100);

            // sort the results first by a type (ascending) and then by an identifier (descending)
            fetchOptions.sortBy().type().asc();
            fetchOptions.sortBy().identifier().desc();

            SearchResult<Sample> result = v3.searchSamples(sessionToken, criteria, fetchOptions);

            // because of the pagination the list contains only the first 100 objects (or even less if there are fewer results found)
            System.out.println(result.getObjects());

            // returns the number of all found results (i.e. potentially more than 100)
            System.out.println(result.getTotalCount());
        }
    }
```

**V3SearchWithPaginationAndSortingExample.html**

```html
    <script>
        require([ "as/dto/sample/search/SampleSearchCriteria", "as/dto/sample/fetchoptions/SampleFetchOptions" ], 
            function(SampleSearchCriteria, SampleFetchOptions) {
     
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                var criteria = new SampleSearchCriteria();
                var fetchOptions = new SampleFetchOptions();

                // get the first 100 results
                fetchOptions.from(0);
                fetchOptions.count(100);

                // sort the results first by a type (ascending) and then by an identifier (descending)
                fetchOptions.sortBy().type().asc();
                fetchOptions.sortBy().identifier().desc();

                v3.searchSamples(criteria, fetchOptions).done(function(result) {
                    // because of pagination the list contains only the first 100 objects (or even less if there are fewer results found)
                    console.log(result.getObjects());

                    // returns the number of all found results (i.e. potentially more than 100)
                    console.log(result.getTotalCount());
                });
            });
    </script>
```

####  Example with OR operator

By default all specified search criteria have to be fulfilled. If only
one criteria needs to be fulfilled use `criteria.withOrOperator()` as in
the following example:

**V3SearchWithOrOperatorExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;

    public class V3SearchWithOrOperatorExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            // search for samples that are either in space with code MY_SPACE_CODE or of sample type with code MY_SAMPLE_TYPE_CODE
            SampleSearchCriteria criteria = new SampleSearchCriteria();
            criteria.withOrOperator();
            criteria.withSpace().withCode().thatEquals("MY_SPACE_CODE");
            criteria.withType().withCode().thatEquals("MY_SAMPLE_TYPE_CODE");
            
            // tell the API to fetch the type for each returned sample
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            fetchOptions.withType();

            SearchResult<Sample> result = v3.searchSamples(sessionToken, criteria, fetchOptions);

            for (Sample sample : result.getObjects())
            {
                System.out.println("Sample " + sample.getIdentifier() + " [" + sample.getType().getCode() + "]");
            }
        }
    }
```

**V3SearchWithOrOperatorExample.html**

```html
    <script>
        require([ "as/dto/sample/search/SampleSearchCriteria", "as/dto/sample/fetchoptions/SampleFetchOptions" ], 
            function(SampleSearchCriteria, SampleFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                // search for samples that are in space with code MY_SPACE_CODE and are of sample type with code MY_SAMPLE_TYPE_CODE
                var criteria = new SampleSearchCriteria();
                criteria.withOrOperator();
                criteria.withSpace().withCode().thatEquals("MY_SPACE_CODE");
                criteria.withType().withCode().thatEquals("MY_SAMPLE_TYPE_CODE");

                // tell the API to fetch type for each returned sample
                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withType();

                v3.searchSamples(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(sample) {
                        alert("Sample " + sample.getIdentifier() + " [" + sample.getType().getCode() + "]");
                    });
                });
            });
    </script>
```

#### Example with nested logical operators

The following code finds samples with perm ID that ends with "6" AND
(with code that contains "-" OR that starts with "C") AND (with
experiment OR of type whose code starts with "MASTER").

**V3SearchWithNestedLogicalOperatorsExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;

    public class V3SearchWithRecursiveFetchOptionsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
            SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
            criteria.withPermId().thatEndsWith("6");

            SampleSearchCriteria subcriteria1 = criteria.withSubcriteria().withOrOperator();
            subcriteria1.withCode().thatContains("-");
            subcriteria1.withCode().thatStartsWith("C");

            SampleSearchCriteria subcriteria2 = criteria.withSubcriteria().withOrOperator();
            subcriteria2.withExperiment();
            subcriteria2.withType().withCode().thatStartsWith("MASTER");

            // tell the API to fetch all descendents for each returned sample
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            SearchResult<Sample> result = v3.searchSamples(sessionToken, criteria, fetchOptions);

            for (Sample sample : result.getObjects())
            {
                System.out.println("Sample " + sample.getIdentifier() + " [" + sample.getType().getCode() + "]");
            }
        }
    }
```

**V3SearchWithNestedLogicalOperatorsExample.html**

```html
    <script>
        require([ "as/dto/sample/search/SampleSearchCriteria", "as/dto/sample/fetchoptions/SampleFetchOptions" ], 
            function(SampleSearchCriteria, SampleFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
                var criteria = new SampleSearchCriteria().withAndOperator();
                criteria.withPermId().thatEndsWith("6");

                var subcriteria1 = criteria.withSubcriteria().withOrOperator();
                subcriteria1.withCode().thatContains("-");
                subcriteria1.withCode().thatStartsWith("C");

                var subcriteria2 = criteria.withSubcriteria().withOrOperator();
                subcriteria2.withExperiment();
                subcriteria2.withType().withCode().thatStartsWith("MASTER");

                // tell the API to fetch type for each returned sample
                var fetchOptions = new SampleFetchOptions();

                v3.searchSamples(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(sample) {
                        alert("Sample " + sample.getIdentifier() + " [" + sample.getType().getCode() + "]");
                    });
                });
            });
    </script>
```

#### Example with recursive fetch options

In order to get all descendent/acsendents of a sample fetch options can
be used recursively by
using `fetchOptions.withChildrenUsing(fetchOptions) `as in the following
example:

**V3SearchWithRecursiveFetchOptionsExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;

    public class V3SearchWithRecursiveFetchOptionsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            SampleSearchCriteria criteria = new SampleSearchCriteria();
            criteria.withType().withCode().thatEquals("MY_SAMPLE_TYPE_CODE");
            
            // tell the API to fetch all descendents for each returned sample
            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            fetchOptions.withChildrenUsing(fetchOptions);

            SearchResult<Sample> result = v3.searchSamples(sessionToken, criteria, fetchOptions);

            for (Sample sample : result.getObjects())
            {
                System.out.println("Sample " + renderWithDescendants(sample));
            }
        }
        
        private static String renderWithDescendants(Sample sample)
        {
            StringBuilder builder = new StringBuilder();
            for (Sample child : sample.getChildren())
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(renderWithDescendants(child));
            }
            if (builder.length() == 0)
            {
                return sample.getCode();
            }
            return sample.getCode() + " -> (" + builder.toString() + ")";
        }
    }
```

**V3SearchWithRecursiveFetchOptionsExample.html**

```html
    <script>
        require([ "as/dto/sample/search/SampleSearchCriteria", "as/dto/sample/fetchoptions/SampleFetchOptions" ], 
            function(SampleSearchCriteria, SampleFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var criteria = new SampleSearchCriteria();
                criteria.withType().withCode().thatEquals("MY_SAMPLE_TYPE_CODE");

                // tell the API to fetch all descendents for each returned sample
                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withChildrenUsing(fetchOptions);

                v3.searchSamples(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(sample) {
                        alert("Sample " + renderWithDescendants(sample));
                    });
                });
                    
                function renderWithDescendants(sample) {
                    var children = sample.getChildren();
                    var list = "";
                    for (var i = 0; i < children.length; i++) {
                        if (list.length > 0) {
                            list += ", ";
                        }
                        list += renderWithDescendants(children[i]);
                    }
                    if (children.length == 0) {
                        return sample.getCode();
                    }
                    return sample.getCode() + " -> (" + list + ")"
                }
        });
    </script>
```

#### Global search

There are two kinds or global search:

-   Using thatContains() and thatContainsExactly() methods of
    GlobalSearchTextCriteria. This type of search performs the substring
    search in any field of any entity.
-   Using thatMatches() method of GlobalSearchTextCriteria. This type of
    search performs lexical match using English dictionaly. If a
    matching string is not a word it is matched as a whole (i.e. code
    will match code only if a whole code string is provided).

Global search searches for experiments, samples, data sets and materials
by specifying a text snippet (or complete words) to be found in any type
of meta data (entity attribute or property). Example:  

**V3GlobalSearchExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;

    public class V3GlobalSearchExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            // search for any text matching 'default' but only among samples
            GlobalSearchCriteria criteria = new GlobalSearchCriteria();
            criteria.withObjectKind().thatIn(GlobalSearchObjectKind.SAMPLE);
            criteria.withText().thatMatches("default");

            // Fetch also the sample type
            GlobalSearchObjectFetchOptions fetchOptions = new GlobalSearchObjectFetchOptions();
            fetchOptions.withSample().withType();

            SearchResult<GlobalSearchObject> result = v3.searchGlobally(sessionToken, criteria, fetchOptions);

            for (GlobalSearchObject object : result.getObjects())
            {
                System.out.println(object.getObjectKind() + ": " + object.getObjectIdentifier() + " ["
                        + object.getSample().getType().getCode()
                        + "], score:" + object.getScore() + ", match:" + object.getMatch());
            }
        }
    }
```
  

**V3GlobalSearchExample.html**

```html
    <script>
        require([ "as/dto/global/search/GlobalSearchCriteria", "as/dto/global/search/GlobalSearchObjectKind", "as/dto/global/fetchoptions/GlobalSearchObjectFetchOptions" ], 
            function(GlobalSearchCriteria, GlobalSearchObjectKind, GlobalSearchObjectFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                // search for any text matching 'default' but only among samples
                var criteria = new GlobalSearchCriteria();
                criteria.withObjectKind().thatIn([GlobalSearchObjectKind.SAMPLE]);
                criteria.withText().thatMatches("default");

                // Fetch also the sample type
                var fetchOptions = new GlobalSearchObjectFetchOptions();
                fetchOptions.withSample().withType();

                v3.searchGlobally(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(object) {
                        alert(object.getObjectKind() + ": " + object.getObjectIdentifier() + " ["
                                + object.getSample().getType().getCode()
                                + "], score:" + object.getScore() + ", match:" + object.getMatch());
                    });
                });
            });
    </script>
```
  

### Getting entities

The methods for getting entities in V3 API are called: getSpaces,
getProjects, getExperiments, getSamples, getDataSets, getMaterials,
getVocabularyTerms, getTags. They all take a list of entity ids and
fetch options as an input (please check "Searching entities" section for
more details on the fetch options). They return a map where the passed
entity ids become the keys and values are the entities found for these
ids. If no entity was found for a given id or entity exists but you
don't have access to it then there is no entry for such an id in the
returned map.

#### Example

**V3GetExample.java**

```java
    import java.util.Arrays;
    import java.util.Map;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

    public class V3GetExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            ISampleId id1 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE");
            ISampleId id2 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE_2");
            ISampleId id3 = new SamplePermId("20160115170726679-98669"); // perm id of sample /MY_SPACE_CODE/MY_SAMPLE_CODE
            ISampleId id4 = new SamplePermId("20160118115737079-98672"); // perm id of sample /MY_SPACE_CODE/MY_SAMPLE_CODE_3
            ISampleId id5 = new SamplePermId("I_DONT_EXIST");

            SampleFetchOptions fetchOptions = new SampleFetchOptions();
            fetchOptions.withProperties();

            Map<ISampleId, Sample> map = v3.getSamples(sessionToken, Arrays.asList(id1, id2, id3, id4, id5), fetchOptions);

            map.get(id1); // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE
            map.get(id2); // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE_2
            map.get(id3); // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE
            map.get(id4); // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE_3
            map.get(id5); // returns null
        }
    }
```

**V3GetExample.html**

```html
    <script>
        require([ "as/dto/sample/id/SampleIdentifier", "as/dto/sample/id/SamplePermId", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
            function(SampleIdentifier, SamplePermId, SampleFetchOptions) {
     
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var id1 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE");
                var id2 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE_2");
                var id3 = new SamplePermId("20160115170726679-98669"); // perm id of sample /MY_SPACE_CODE/MY_SAMPLE_CODE
                var id4 = new SamplePermId("20160118115737079-98672"); // perm id of sample   /MY_SPACE_CODE/MY_SAMPLE_CODE_3
                var id5 = new SamplePermId("I_DONT_EXIST");

                var fetchOptions = new SampleFetchOptions();
                fetchOptions.withProperties();


                v3.getSamples([ id1, id2, id3, id4, id5 ], fetchOptions).done(function(map) {
                    map[id1]; // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE
                    map[id2]; // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE_2
                    map[id3]; // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE
                    map[id4]; // returns sample /MY_SPACE_CODE/MY_SAMPLE_CODE_3
                    map[id5]; // returns null
                });
            });
    </script>
```

### Deleting entities

The methods for deleting entities in V3 API are called: deleteSpaces,
deleteProjects, deleteExperiments, deleteSamples, deleteDataSets,
deleteMaterials, deleteVocabularyTerms, deleteTags. The delete methods
for spaces, projects, materials, vocabulary terms, tags perform a
permanent deletion (there is no trash can for these entities - deletion
cannot be reverted). The delete methods for experiments, samples and
data sets perform a logical deletion (move entities to the trash can)
and return a deletion id. This deletion id can be used for either
confirming the logical deletion to remove the entities permanently or
reverting the logical deletion to take the entities out from the trash
can.

#### Example

**V3DeleteExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;

    public class V3DeleteExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            ISampleId id1 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE");
            ISampleId id2 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE_2");

            SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
            deletionOptions.setReason("Testing logical deletion");

            // logical deletion (move objects to the trash can)
            IDeletionId deletionId = v3.deleteSamples(sessionToken, Arrays.asList(id1, id2), deletionOptions);

            // you can use the deletion id to confirm the deletion (permanently delete objects)
            v3.confirmDeletions(sessionToken, Arrays.asList(deletionId));

            // you can use the deletion id to revert the deletion (get the objects out from the trash can)
            v3.revertDeletions(sessionToken, Arrays.asList(deletionId));
        }
    }
```

**V3DeleteExample.html**

```html
    <script>
        require([ "as/dto/sample/id/SampleIdentifier", "as/dto/sample/delete/SampleDeletionOptions" ], 
            function(SampleIdentifier, SampleDeletionOptions) {
     
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var id1 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE");
                var id2 = new SampleIdentifier("/MY_SPACE_CODE/MY_SAMPLE_CODE_2");

                var deletionOptions = new SampleDeletionOptions();
                deletionOptions.setReason("Testing logical deletion");

                // logical deletion (move objects to the trash can)
                v3.deleteSamples([ id1, id2 ], deletionOptions).done(function(deletionId) {

                    // you can use the deletion id to confirm the deletion (permanently delete objects)
                    v3.confirmDeletions([ deletionId ]);

                    // you can use the deletion id to revert the deletion (get the objects out from the trash can)
                    v3.revertDeletions([ deletionId ]);
                });
            });
    </script>
```

### Searching entity types

The following search methods allows to search for entity types including
all assigned property
types: `searchDataSetTypes`, `searchExperimentTypes`, `searchMaterialTypes`
and `searchSampleTypes`. Here is an example which will search for all
sample types and assigned property types:

**V3SearchTypesExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;

    public class V3SearchTypesExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
            SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
            fetchOptions.withPropertyAssignments().withPropertyType();
            
            SearchResult<SampleType> result = v3.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
            
            for (SampleType sampleType : result.getObjects())
            {
                System.out.println(sampleType.getCode());
                for (PropertyAssignment assignment : sampleType.getPropertyAssignments())
                {
                    System.out.println("  " + assignment.getPropertyType().getCode() + (assignment.isMandatory() ? "*" : ""));
                }
            }
        }
    }
```

**V3SearchTypesExample.html**

```html
    <script>
        require([ "as/dto/sample/search/SampleTypeSearchCriteria", "as/dto/sample/fetchoptions/SampleTypeFetchOptions" ], 
            function(SampleTypeSearchCriteria, SampleTypeFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                // here we are interested only in the last updates of samples and projects
                var criteria = new SampleTypeSearchCriteria();
                var fetchOptions = new SampleTypeFetchOptions();
                fetchOptions.withPropertyAssignments().withPropertyType();

                v3.searchSampleTypes(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(sampleType) {
                        var msg = sampleType.getCode();
                        var assignments = sampleType.getPropertyAssignments();
                        for (var i = 0; i < assignments.length; i++) {
                            msg += "\n  " + assignments[i].getPropertyType().getCode();
                        }
                        alert(msg);
                    });
                });
            });
    </script>
```

### Modifications

The API allows to ask for the latest modification (UPDATE or
CREATE\_OR\_DELETE) for groups of objects of various kinds (see
class `ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind`for
a complete list). This feature of the openBIS API helps GUI clients to
update views automatically. Here is an example which asks for the latest
project and sample update:

**V3SearchObjectKindModificationsExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;

    public class V3SearchObjectKindModificationsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
            // here we are interested only in the last updates of samples and projects
            ObjectKindModificationSearchCriteria criteria = new ObjectKindModificationSearchCriteria();
            criteria.withObjectKind().thatIn(ObjectKind.PROJECT, ObjectKind.SAMPLE);
            criteria.withOperationKind().thatIn(OperationKind.UPDATE);
            
            ObjectKindModificationFetchOptions fetchOptions = new ObjectKindModificationFetchOptions();
            SearchResult<ObjectKindModification> result = v3.searchObjectKindModifications(sessionToken, criteria, fetchOptions);
            
            for (ObjectKindModification modification : result.getObjects())
            {
                System.out.println("The last " + modification.getOperationKind() + " of an entity of kind " 
                        + modification.getObjectKind() + " occured at " + modification.getLastModificationTimeStamp());
            }
        }
    }
```

**V3SearchObjectKindModificationsExample.html**

```html
    <script>
        require([ "as/dto/objectkindmodification/search/ObjectKindModificationSearchCriteria", 
                "as/dto/objectkindmodification/ObjectKind", "as/dto/objectkindmodification/OperationKind",
                "as/dto/objectkindmodification/fetchoptions/ObjectKindModificationFetchOptions" ], 
            function(ObjectKindModificationSearchCriteria, ObjectKind, OperationKind, ObjectKindModificationFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                // here we are interested only in the last updates of samples and projects
                var criteria = new ObjectKindModificationSearchCriteria();
                criteria.withObjectKind().thatIn([ObjectKind.PROJECT, ObjectKind.SAMPLE]);
                criteria.withOperationKind().thatIn([OperationKind.UPDATE]);

                var fetchOptions = new ObjectKindModificationFetchOptions();

                v3.searchObjectKindModifications(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(modification) {
                        alert("The last " + modification.getOperationKind() + " of an entity of kind " 
                                + modification.getObjectKind() + " occured at " + modification.getLastModificationTimeStamp());
                    });
                });
            });
    </script>
```

### Custom AS Services

In order to extend openBIS API new custom services can be established by core plugins of type `services` (see [Custom Application Server Services](https://openbis.readthedocs.io/en/latest/software-developer-documentation/server-side-extensions/as-services.html#custom-application-server-services)). The API offers a method to search for a service and to execute a service.

#### Search for custom services

As with any other search method `searchCustomASServices()` needs a search criteria `CustomASServiceSearchCriteria` and fetch options `CustomASServiceFetchOptions`. The following example returns all available custom AS services.

##### Example 

**V3SearchCustomASServicesExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;

    public class V3SearchCustomASServicesExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            CustomASServiceSearchCriteria criteria = new CustomASServiceSearchCriteria();
            CustomASServiceFetchOptions fetchOptions = new CustomASServiceFetchOptions();
            SearchResult<CustomASService> result = v3.searchCustomASServices(sessionToken, criteria, fetchOptions);
            for (CustomASService service : result.getObjects())
            {
                System.out.println(service.getCode() + ": " + service.getLabel() + " (" + service.getDescription() + ")");
            }
        }
    }
```

**V3SearchCustomASServicesExample.html**

```html
    <script>
        require([ "as/dto/service/search/CustomASServiceSearchCriteria", "as/dto/service/fetchoptions/CustomASServiceFetchOptions" ], 
            function(CustomASServiceSearchCriteria, CustomASServiceFetchOptions) {
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var criteria = new CustomASServiceSearchCriteria();
                var fetchOptions = new CustomASServiceFetchOptions();
                v3.searchCustomASServices(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(service) {
                        alert(service.getCode() + ": " + service.getLabel() + " (" + service.getDescription() + ")");
                    });
                });
            });
    </script>
```

#### Execute a custom service

In order to execute a custom AS service its code is needed. In addition
a set of key-value pairs can be provided. The key has to be a string
whereas the value can be any object. Note, that in case of Java the
object has be an instance of class which Java serializable. The
key-value pairs are added to `CustomASServiceExecutionOptions` object by
invoking `withParameter()` for each pair.

The result can be any object (again it has to be Java serializable in
the Java case). In a Java client the result will usually be casted for
further processing.

##### Example 

**V3ExecuteCustomASServiceExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;

    public class V3ExecuteCustomASServiceExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
            
            CustomASServiceCode id = new CustomASServiceCode("example-service");
            CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions().withParameter("space-code", "TEST");
            Object result = v3.executeCustomASService(sessionToken, id, options);

            System.out.println("Result: " + result);
        }
    }
```

**V3ExecuteCustomASServiceExample.html**

```html
    <script>
        require([ "as/dto/service/id/CustomASServiceCode", "as/dto/service/CustomASServiceExecutionOptions" ], 
            function(CustomASServiceCode, CustomASServiceExecutionOptions) {
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
                var id = new CustomASServiceCode("example-service");
                var options = new CustomASServiceExecutionOptions().withParameter("space-code", "TEST");
                v3.executeCustomASService(id, options).done(function(result) {
                    alert(result);
                });
            });
    </script>
```

### Archiving / unarchiving data sets

The API provides the following methods for handling the data set
archiving: archiveDataSets and unarchiveDataSets. Both methods schedule
the operation to be executed asynchronously, i.e. once
archiveDataSets/unarchiveDataSets method call finishes the requested
data sets are only scheduled for the archiving/unarchiving but are not
in the archive/store yet.

#### Archiving data sets  

##### Example 

**V3ArchiveDataSetsExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3ArchiveDataSetsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            IDataSetId id1 = new DataSetPermId("20160524154020607-2266");
            IDataSetId id2 = new DataSetPermId("20160524154020607-2267");

            DataSetArchiveOptions options = new DataSetArchiveOptions();

            // With removeFromDataStore flag set to true data sets are moved to the archive.
            // With removeFromDataStore flag set to false data sets are copied to the archive.
            // Default value is true (move to the archive).
            options.setRemoveFromDataStore(false);

            // Schedules archiving of the specified data sets. Archiving itself is executed asynchronously.
            v3.archiveDataSets(sessionToken, Arrays.asList(id1, id2), options);

            System.out.println("Archiving scheduled");
        }
    }
```

**V3ArchiveDataSetsExample.html**

```html
    <script>
        require([ "openbis", "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/archive/DataSetArchiveOptions" ], 
            function(openbis, DataSetPermId, DataSetArchiveOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                var id1 = new DataSetPermId("20160524154020607-2266")
                var id2 = new DataSetPermId("20160524154020607-2267")

                var options = new DataSetArchiveOptions();

                // With removeFromDataStore flag set to true data sets are moved to the archive.
                // With removeFromDataStore flag set to false data sets are copied to the archive.
                // Default value is true (move to the archive).
                options.setRemoveFromDataStore(false);
     
                // Schedules archiving of the specified data sets. Archiving itself is executed asynchronously.
                v3.archiveDataSets([ id1, id2 ], options).done(function() {
                    alert("Archiving scheduled");
                });
            });
        });
    </script>
```

#### Unarchiving data sets

##### Example 

**V3UnarchiveDataSetsExample.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3UnarchiveDataSetsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            IDataSetId id1 = new DataSetPermId("20160524154020607-2266");
            IDataSetId id2 = new DataSetPermId("20160524154020607-2267");

            DataSetUnarchiveOptions options = new DataSetUnarchiveOptions();

            // Schedules unarchiving of the specified data sets. Unarchiving itself is executed asynchronously.
            v3.unarchiveDataSets(sessionToken, Arrays.asList(id1, id2), options);

            System.out.println("Unarchiving scheduled");
        }
    }
```

**V3UnarchiveDataSetsExample.html**

```html
    <script>
        require([ "openbis", "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/unarchive/DataSetUnarchiveOptions" ], 
            function(openbis, DataSetPermId, DataSetUnarchiveOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var id1 = new DataSetPermId("20160524154020607-2266")
                var id2 = new DataSetPermId("20160524154020607-2267")

                var options = new DataSetUnarchiveOptions();

                // Schedules unarchiving of the specified data sets. Unarchiving itself is executed asynchronously.
                v3.unarchiveDataSets([ id1, id2 ], options).done(function() {
                    alert("Unarchiving scheduled");
                });
            });
        });
    </script>
```

###  Executing Operations 

The V3 API provides you with methods that allow you to create, update,
get, search and delete entities, archive and unarchive datasets, execute
custom services and much more. With these methods you can
programmatically access most of the openBIS features to build your own
webapps, dropboxes or services. Even though these methods are quite
different, there are some things that they all have in common:

-   each method is executed in its own separate transaction
-   each method is executed synchronously

Let's think about what it really means. Separate transactions make two
(even subsequent) method calls completely unrelated. For instance, when
you make a call to create experiments and then another call to create
samples, then even if the sample creation fails the experiments, that
had been already created, would remain in the system. Most of the time
this is exactly what we want but not always. There are times when we
would like to create either both experiments and samples or nothing if
something is wrong. A good example would be an import of some file that
contains both experiments and samples. We would like to be able to
import the file, fail if it is wrong, correct the file and import it
again. With separate transactions we would end up with some things
already created after the first failed import and we wouldn't be able to
reimport the corrected file again as some things would be already in the
system.

Synchronous method execution is also something what we expect most of
the time. You call a method and it returns once all the work is done.
For instance, when we call a method to create samples we know that once
the method finishes all the samples have been created in the
system. This makes perfect sense when we need to execute operations that
depend on each other, e.g. we can create data sets and attach them to
samples only after the samples had been created. Just as with the
separate transactions, there are cases when synchronous method execution
is limiting. Let's use the file import example again. What would happen
if a file we wanted to import contained hundreds of thousands of
entities? The import would probably take a very long time. Our
synchronous method call would not return until all the entities have
been created which means we would also block a script/program that makes
this method call for a very long time. We could of course create a
separate thread in our script/program to overcome this problem but that
would add up more complexity. It would be also nice to notify a user
once such an operation finishes or fails, e.g. by sending an email.
Unfortunately that would mean we have to keep our script/program running
until the operation finishes or fails to send such an email. What about
a progress information for running executions or a history of previous
operations and their results? That would be nice but it would increase
the complexity of our script/program even more.

Therefore, if you want to:

-   execute multiple operations in a single transaction
-   execute operations asynchronously
-   monitor progress of operations
-   receive notifications about finished/failed operations
-   keep history of operations and their results

you should use:

-   executeOperations method to execute your operations
-   getOperationExecutions and searchOperationExecutions methods to
    retrieve information about operation executions (e.g. progress,
    results or errors)
-   updateOperationExecutions and deleteOperationExecutions methods to
    control what information should be still kept for a given operation
    execution and what information can be already removed

More details on each of these methods in presented in the sections
below. Please note that all of the described methods are available in
both Javascript and Java.

#### Method executeOperations

This method can be used to execute one or many operations either
synchronously or asynchronously. Operations are always executed in a
single transaction (a failure of a single operation triggers a rollback
of all the operations). The executeOperations method can be used to
execute any of the IApplicationServerApi methods (except for
login/logout and executeOperations itself), i.e. for each
IApplicationServerApi method there is a corresponding operation class
(class that implements IOperation interface). For instance,
IApplicationServerApi.createSpaces method is represented by
CreateSpacesOperation class, IApplicationServerApi.updateSpaces method
by UpdateSpacesOperation class etc.

##### **Asynchronous operation execution**

An asynchronous executeOperations invocation only schedules operations
for the execution and then immediately returns. Results of the scheduled
operations can be retrieved later with getOperationExecutions or
searchOperationExecutions methods.

Because the operations are scheduled to be executed later (in a separate
thread) a regular try/catch block around executeOperations method will
only catch exceptions related with scheduling the operations for the
execution, but NOT the exceptions thrown by the operations during the
execution. To check for errors that occurred during the execution please
use getOperationExecutions and searchOperationExecutions methods once
the execution finishes.

In order to execute operations asynchronously, executeOperations has to
be used with AsynchronousOperationExecutionOptions. With such options,
the method returns AsynchronousOperationExecutionResults object.
AsynchronousOperationExecutionResults object contains automatically
generated executionId that can be used for retrieving additional
information about the execution, fetching the results or errors.

During its life an asynchronous execution goes through the following
states:

-   NEW - execution has been just created with executeOperations method
-   SCHEDULED - execution has been added to a thread pool queue and is
    waiting for a free thread
-   RUNNING - execution has been picked from a thread pool queue by a
    free thread and is currently executing
-   FINISHED/FAILED - if execution finishes successfully then execution
    state is changed to FINISHED, if anything goes wrong it is changed
    to FAILED

  

**V3ExecuteOperationsAsynchronous.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3ExecuteOperationsAsynchronous
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);

            AsynchronousOperationExecutionResults results = (AsynchronousOperationExecutionResults) v3.executeOperations(sessionToken, 
                    Arrays.asList(operation), new AsynchronousOperationExecutionOptions());

            System.out.println("Execution id: " + results.getExecutionId());
        }
    }
```

**V3ExecuteOperationsAsynchronous.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/AsynchronousOperationExecutionOptions" ],
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, AsynchronousOperationExecutionOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);

                v3.executeOperations([ operation ], new AsynchronousOperationExecutionOptions()).done(function(results) {

                    console.log("Execution id: " + results.getExecutionId());
                });
            });
    </script>
```

##### **Synchronous operation execution**

A synchronous executeOperations invocation immediately executes all the
operations. Any exceptions thrown by the executed operations can be
caught with a regular try/catch block around executeOperations method.

In order to execute operations synchronously, executeOperations has to
be used with SynchronousOperationExecutionOptions. With such options,
the method returns SynchronousOperationExecutionResults object.
SynchronousOperationExecutionResults object contains the results for all
the executed operations.

In contrast to the asynchronous version, the synchronous call requires
executionId to be explicitly set in SynchronousOperationExecutionOptions
for the additional information to be gathered about the execution.

During its life a synchronous execution goes through the following
states:

-   NEW - execution has been just created with executeOperations method
-   RUNNING - execution is being executed by the same thread as
    executeOperations method
-   FINISHED/FAILED - if execution finishes successfully then execution
    state is changed to FINISHED, if anything goes wrong it is changed
    to FAILED

  

**V3ExecuteOperationsSynchronous.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionResults;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperationResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3ExecuteOperationsSynchronous
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);

            SynchronousOperationExecutionResults results = (SynchronousOperationExecutionResults) v3.executeOperations(sessionToken, 
                    Arrays.asList(operation), new SynchronousOperationExecutionOptions());

            CreateSamplesOperationResult result = (CreateSamplesOperationResult) results.getResults().get(0);

            System.out.println("Sample id: " + result.getObjectIds());
        }
    }
```

**V3ExecuteOperationsSynchronous.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/SynchronousOperationExecutionOptions" ],        
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, SynchronousOperationExecutionOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);

                v3.executeOperations([ operation ], new SynchronousOperationExecutionOptions()).done(function(results) {

                    var result = results.getResults()[0];
                    console.log("Sample id: " + result.getObjectIds());
                });
            });
    </script>
```

##### **Notifications**

The executeOperations method can notify about finished or failed
operation executions. At the moment the only supported notification
method is email (OperationExecutionEmailNotification).

For successfully finished executions an email contains:

-   execution id
-   execution description
-   list of operation summaries and operation results

For failed executions an email contains:

-   execution id
-   execution description
-   list of operation summaries
-   error

  

**V3ExecuteOperationsEmailNotification.java**

```java
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionEmailNotification;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3ExecuteOperationsEmailNotification
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);

            AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();
            options.setNotification(new OperationExecutionEmailNotification("my@email1.com", "my@email2.com"));

            AsynchronousOperationExecutionResults results = (AsynchronousOperationExecutionResults) v3.executeOperations(sessionToken, 
                    Arrays.asList(operation), options);

            System.out.println("Execution id: " + results.getExecutionId());
        }
    }
```

**V3ExecuteOperationsEmailNotification.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/AsynchronousOperationExecutionOptions", "as/dto/operation/OperationExecutionEmailNotification" ], 
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, AsynchronousOperationExecutionOptions, OperationExecutionEmailNotification) {
                
                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);

                var options = new AsynchronousOperationExecutionOptions();
                options.setNotification(new OperationExecutionEmailNotification([ "my@email1.com", "my@email2.com" ]));

                v3.executeOperations([ operation ], options).done(function(results) {

                    console.log("Execution id: " + results.getExecutionId());
                });
            });
    </script>
```

#### Method getOperationExecutions / searchOperationExecutions

Operation execution information can be fetched by an owner of an
execution (i.e. a person that called executeOperations method) or an
admin. Both getOperationExecutions and searchOperationExecutions methods
work similar to the other get/search methods in the V3 API.

The operation execution information that both methods return can be
divided into 3 categories:

-   basic information (code, state, owner, description, creationDate,
    startDate, finishDate etc.)
-   summary information (summary of operations, progress, error,
    results)
-   detailed information (details of operations, progress, error,
    results)

Each category can have a different availability time (i.e. time for how
long a given information is stored in the system). The availability
times can be set via the executeOperations method options (both
SynchronousOperationExecutionOptions and
AsynchronousOperationExecutionOptions):

-   basic information (setAvailabilityTime)
-   summary information (setSummaryAvailabilityTime)
-   detailed information (setDetailsAvailabilityTime)

If the times are not explicitly set, then the following defaults are
used:

-   basic information (1 year)
-   summary information (1 month)
-   detailed information (1 day)

The current availability of each category can be checked with
getAvailability, getSummaryAvailability, getDetailsAvailability methods
of OperationExecution class. The availability can have one of the
following values:

-   AVAILABLE - an information is available and can be fetched
-   DELETE\_PENDING - an explicit request to delete the information has
    been made with updateOperationExecutions or
    deleteOperationExecutions method
-   DELETED - an explicit request to delete the information has been
    processed and the information has been deleted
-   TIME\_OUT\_PENDING - an availability time has expired, the
    information has been scheduled to be removed
-   TIMED\_OUT - an availability time has expired, the information has
    been removed

Update of availability values and deletion of operation execution
related information are done with two separate V3 maintenance tasks
(please check service.properties for their configuration). 

**V3GetOperationExecutionsAsynchronous.java**

```java
    import java.util.Arrays;
    import java.util.Map;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3GetOperationExecutionsAsynchronous
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);

            // Asynchronous execution: information about an asynchronous operation execution is always gathered, the executionId
            // is also always automatically generated and returned with AsynchronousOperationExecutionResults.

            AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();

            // Both synchronous and asynchronous executions: default availability times can be overwritten using the options object.
            // Availability times should be specified in seconds.

            options.setAvailabilityTime(30 * 24 * 60 * 60); // one month
            options.setSummaryAvailabilityTime(24 * 60 * 60); // one day
            options.setDetailsAvailabilityTime(60 * 60); // one hour

            // Execute operation

            AsynchronousOperationExecutionResults results =
                    (AsynchronousOperationExecutionResults) v3.executeOperations(sessionToken, Arrays.asList(operation), options);

            // It is an asynchronous execution. It might be still waiting for a free thread,
            // it may be already executing or it may have already finished. It does not matter.
            // We can already fetch the information about it.

            // Specify what information to fetch about the execution

            OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
            fo.withSummary();
            fo.withSummary().withOperations();
            fo.withSummary().withProgress();
            fo.withSummary().withResults();
            fo.withSummary().withError();
            fo.withDetails();
            fo.withDetails().withOperations();
            fo.withDetails().withProgress();
            fo.withDetails().withResults();
            fo.withDetails().withError();

            // Get information about the execution

            Map<IOperationExecutionId, OperationExecution> executions =
                    v3.getOperationExecutions(sessionToken, Arrays.asList(results.getExecutionId()), fo);

            OperationExecution execution = executions.get(results.getExecutionId());

            // Summary contains String representation of operations, progress, results and error

            String summaryOperation = execution.getSummary().getOperations().get(0);
            System.out.println("Summary.operation: " + summaryOperation);
            System.out.println("Summary.progress: " + execution.getSummary().getProgress());
            System.out.println("Summary.results: " + execution.getSummary().getResults());
            System.out.println("Summary.error: " + execution.getSummary().getError());

            // Details contain object representation of operations, progress, results and error

            CreateSamplesOperation detailsOperation = (CreateSamplesOperation) execution.getDetails().getOperations().get(0);
            System.out.println("Details.operation: " + detailsOperation);
            System.out.println("Details.progress: " + execution.getSummary().getProgress());
            System.out.println("Details.results: " + execution.getSummary().getResults());
            System.out.println("Details.error: " + execution.getSummary().getError());
        }    
    }
```

**V3GetOperationExecutionsAsynchronous.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/AsynchronousOperationExecutionOptions", "as/dto/operation/fetchoptions/OperationExecutionFetchOptions",   "as/dto/operation/id/OperationExecutionPermId" ], 
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, AsynchronousOperationExecutionOptions, OperationExecutionFetchOptions, OperationExecutionPermId) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);

                // Asynchronous execution: information about an asynchronous operation execution is always gathered, the executionId
                // is also always automatically generated and returned with AsynchronousOperationExecutionResults.

                var options = new AsynchronousOperationExecutionOptions();

                // Both synchronous and asynchronous executions: default availability times can be overwritten using the options object.
                // Availability times should be specified in seconds.

                options.setAvailabilityTime(30 * 24 * 60 * 60); // one month
                options.setSummaryAvailabilityTime(24 * 60 * 60); // one day
                options.setDetailsAvailabilityTime(60 * 60); // one hour

                // Execute operation

                v3.executeOperations([ operation ], options).done(function(results) {

                    // It is an asynchronous execution. It might be still waiting for a free thread,
                    // it may be already executing or it may have already finished. It does not matter.
                    // We can already fetch the information about it.

                    // Specify what information to fetch about the execution

                    var fo = new OperationExecutionFetchOptions();
                    fo.withSummary();
                    fo.withSummary().withOperations();
                    fo.withSummary().withProgress();
                    fo.withSummary().withResults();
                    fo.withSummary().withError();

                    fo.withDetails();
                    fo.withDetails().withOperations();
                    fo.withDetails().withProgress();
                    fo.withDetails().withResults();
                    fo.withDetails().withError();

                    // Get information about the execution

                    v3.getOperationExecutions([ results.getExecutionId() ], fo).done(function(executions) {

                        var execution = executions[results.getExecutionId()];

                        // Summary contains String representation of operations, progress, results and error

                        var summaryOperation = execution.getSummary().getOperations()[0];
                        console.log("Summary.operation: " + summaryOperation);
                        console.log("Summary.progress: " + execution.getSummary().getProgress());
                        console.log("Summary.results: " + execution.getSummary().getResults());
                        console.log("Summary.error: " + execution.getSummary().getError());

                        // Details contain object representation of operations, progress, results and error

                        var detailsOperation = execution.getDetails().getOperations()[0];
                        console.log("Details.operation: " + detailsOperation);
                        console.log("Details.progress: " + execution.getSummary().getProgress());
                        console.log("Details.results: " + execution.getSummary().getResults());
                        console.log("Details.error: " + execution.getSummary().getError());
                    });
                });
            });
    </script>
```

**V3GetOperationExecutionsSynchronous.java**

```java
    import java.util.Arrays;
    import java.util.Map;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3GetOperationExecutionsSynchronous
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE_7");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);

            // Synchronous execution: to gather information about a synchronous operation execution, the executionId has to
            // be explicitly set in the options object. OperationExecutionPermId created with no-argument constructor automatically
            // generates a random permId value.

            SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
            options.setExecutionId(new OperationExecutionPermId());

            // Both synchronous and asynchronous executions: default availability times can be overwritten using the options object.
            // Availability times should be specified in seconds.

            options.setAvailabilityTime(30 * 24 * 60 * 60); // one month
            options.setSummaryAvailabilityTime(24 * 60 * 60); // one day
            options.setDetailsAvailabilityTime(60 * 60); // one hour

            // Execute operation

            v3.executeOperations(sessionToken, Arrays.asList(operation), options);

            // Specify what information to fetch about the execution

            OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
            fo.withSummary();
            fo.withSummary().withOperations();
            fo.withSummary().withProgress();
            fo.withSummary().withResults();
            fo.withSummary().withError();
            fo.withDetails();
            fo.withDetails().withOperations();
            fo.withDetails().withProgress();
            fo.withDetails().withResults();
            fo.withDetails().withError();

            // Get information about the execution

            Map<IOperationExecutionId, OperationExecution> executions =
                    v3.getOperationExecutions(sessionToken, Arrays.asList(options.getExecutionId()), fo);

            OperationExecution execution = executions.get(options.getExecutionId());

            // Summary contains String representation of operations, progress, results and error

            String summaryOperation = execution.getSummary().getOperations().get(0);
            System.out.println("Summary.operation: " + summaryOperation);
            System.out.println("Summary.progress: " + execution.getSummary().getProgress());
            System.out.println("Summary.results: " + execution.getSummary().getResults());
            System.out.println("Summary.error: " + execution.getSummary().getError());

            // Details contain object representation of operations, progress, results and error

            CreateSamplesOperation detailsOperation = (CreateSamplesOperation) execution.getDetails().getOperations().get(0);
            System.out.println("Details.operation: " + detailsOperation);
            System.out.println("Details.progress: " + execution.getSummary().getProgress());
            System.out.println("Details.results: " + execution.getSummary().getResults());
            System.out.println("Details.error: " + execution.getSummary().getError());
        }
    }
```

**V3GetOperationExecutionsSynchronous.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/SynchronousOperationExecutionOptions", "as/dto/operation/fetchoptions/OperationExecutionFetchOptions",    "as/dto/operation/id/OperationExecutionPermId" ], 
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, SynchronousOperationExecutionOptions, OperationExecutionFetchOptions, OperationExecutionPermId) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
                var sample = new SampleCreation();

                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);

                // Synchronous execution: to gather information about a synchronous operation execution, the executionId has to
                // be explicitly set in the options object. OperationExecutionPermId created with no-argument constructor automatically
                // generates a random permId value.

                var options = new SynchronousOperationExecutionOptions();
                options.setExecutionId(new OperationExecutionPermId());

                // Both synchronous and asynchronous executions: default availability times can be overwritten using the options object.
                // Availability times should be specified in seconds.

                options.setAvailabilityTime(30 * 24 * 60 * 60); // one month
                options.setSummaryAvailabilityTime(24 * 60 * 60); // one day
                options.setDetailsAvailabilityTime(60 * 60); // one hour

                // Execute operation

                v3.executeOperations([ operation ], options).done(function() {

                    // Specify what information to fetch about the execution

                    var fo = new OperationExecutionFetchOptions();
                    fo.withSummary();
                    fo.withSummary().withOperations();
                    fo.withSummary().withProgress();
                    fo.withSummary().withResults();
                    fo.withSummary().withError();

                    fo.withDetails();
                    fo.withDetails().withOperations();
                    fo.withDetails().withProgress();
                    fo.withDetails().withResults();
                    fo.withDetails().withError();

                    // Get information about the execution

                    v3.getOperationExecutions([ options.getExecutionId() ], fo).done(function(executions) {

                        var execution = executions[options.getExecutionId()];

                        // Summary contains String representation of operations, progress, results and error

                        var summaryOperation = execution.getSummary().getOperations()[0];
                        console.log("Summary.operation: " + summaryOperation);
                        console.log("Summary.progress: " + execution.getSummary().getProgress());
                        console.log("Summary.results: " + execution.getSummary().getResults());
                        console.log("Summary.error: " + execution.getSummary().getError());

                        // Details contain object representation of operations, progress, results and error

                        var detailsOperation = execution.getDetails().getOperations()[0];
                        console.log("Details.operation: " + detailsOperation);
                        console.log("Details.progress: " + execution.getSummary().getProgress());
                        console.log("Details.results: " + execution.getSummary().getResults());
                        console.log("Details.error: " + execution.getSummary().getError());
                    });
                });
            });
    </script>
```

####  Method updateOperationExecutions / deleteOperationExecutions

The updateOperationExecutions and deleteOperationExecutions methods can
be used to explicitly delete some part of information or delete all the
information about a given operation execution before a corresponding
availability time expires.

**V3UpdateOperationExecutions.java**

```java
    import java.util.Arrays;
    import java.util.Map;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3UpdateOperationExecutions
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);
            AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();

            // Execute operation

            AsynchronousOperationExecutionResults results =
                    (AsynchronousOperationExecutionResults) v3.executeOperations(sessionToken, Arrays.asList(operation), options);

            // You can explicitly request a deletion of summary or details. Here we want to delete details.

            OperationExecutionUpdate update = new OperationExecutionUpdate();
            update.setExecutionId(results.getExecutionId());
            update.deleteDetails();

            v3.updateOperationExecutions(sessionToken, Arrays.asList(update));

            // Let's check the execution information

            OperationExecutionFetchOptions fo = new OperationExecutionFetchOptions();
            fo.withSummary();
            fo.withDetails();

            Map<IOperationExecutionId, OperationExecution> executions =
                    v3.getOperationExecutions(sessionToken, Arrays.asList(results.getExecutionId()), fo);

            OperationExecution execution = executions.get(results.getExecutionId());

            // Summary availability is AVAILABLE. Details availability is either DELETE_PENDING or DELETED
            // depending on whether a maintenance task has already processed the deletion request.

            System.out.println("Summary: " + execution.getSummary());
            System.out.println("Summary.availability: " + execution.getSummaryAvailability());
            System.out.println("Details: " + execution.getDetails());
            System.out.println("Details.availability: " + execution.getDetailsAvailability());
        }
    }
```

**V3UpdateOperationExecutions.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/AsynchronousOperationExecutionOptions", "as/dto/operation/update/OperationExecutionUpdate", "as/dto/operation/fetchoptions/OperationExecutionFetchOptions" ], 
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, AsynchronousOperationExecutionOptions, OperationExecutionUpdate, OperationExecutionFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);
                var options = new AsynchronousOperationExecutionOptions();

                // Execute operation

                v3.executeOperations([ operation ], options).done(function(results) {

                    // You can explicitly request a deletion of summary or details. Here we want to delete details.

                    var update = new OperationExecutionUpdate();
                    update.setExecutionId(results.getExecutionId());
                    update.deleteDetails();

                    v3.updateOperationExecutions([ update ]).done(function() {

                        // Let's check the execution information

                        var fo = new OperationExecutionFetchOptions();
                        fo.withSummary();
                        fo.withDetails();

                        v3.getOperationExecutions([ results.getExecutionId() ], fo).done(function(executions) {

                            var execution = executions[results.getExecutionId()];

                            // Summary availability is AVAILABLE. Details availability is either DELETE_PENDING or DELETED
                            // depending on whether a maintenance task has already processed the deletion request.

                            console.log("Summary: " + execution.getSummary());
                            console.log("Summary.availability: " + execution.getSummaryAvailability());
                            console.log("Details: " + execution.getDetails());
                            console.log("Details.availability: " + execution.getDetailsAvailability());
                        });
                    });
                });
            });
    </script>
```

**V3DeleteOperationExecutions.java**

```java
    import java.util.Arrays;
    import java.util.Map;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.AsynchronousOperationExecutionResults;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

    public class V3DeleteOperationExecutions
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            SampleCreation sample = new SampleCreation();
            sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
            sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
            sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
            sample.setCode("MY_SAMPLE_CODE");

            CreateSamplesOperation operation = new CreateSamplesOperation(sample);
            AsynchronousOperationExecutionOptions options = new AsynchronousOperationExecutionOptions();

            // Execute operation

            AsynchronousOperationExecutionResults results =
                    (AsynchronousOperationExecutionResults) v3.executeOperations(sessionToken, Arrays.asList(operation), options);

            // Explicitly request a deletion of all the information about the execution

            OperationExecutionDeletionOptions deletionOptions = new OperationExecutionDeletionOptions();
            deletionOptions.setReason("test reason");

            v3.deleteOperationExecutions(sessionToken, Arrays.asList(results.getExecutionId()), deletionOptions);

            // Let's check whether the execution information is still available


            Map<IOperationExecutionId, OperationExecution> executions =
                    v3.getOperationExecutions(sessionToken, Arrays.asList(results.getExecutionId()), new OperationExecutionFetchOptions());

            OperationExecution execution = executions.get(results.getExecutionId());

            // Depending on whether a maintenance task has already processed the deletion request
            // the execution will be either null or the returned execution availability will be DELETE_PENDING.

            System.out.println("Availability: " + (execution != null ? execution.getAvailability() : null));
        }
    }
```

**V3DeleteOperationExecutions.html**

```html
    <script>
        require([ "openbis", "as/dto/sample/create/SampleCreation", "as/dto/entitytype/id/EntityTypePermId", "as/dto/space/id/SpacePermId", "as/dto/experiment/id/ExperimentIdentifier", "as/dto/sample/create/CreateSamplesOperation", "as/dto/operation/AsynchronousOperationExecutionOptions", "as/dto/operation/delete/OperationExecutionDeletionOptions", "as/dto/operation/fetchoptions/OperationExecutionFetchOptions" ], 
            function(openbis, SampleCreation, EntityTypePermId, SpacePermId, ExperimentIdentifier, CreateSamplesOperation, AsynchronousOperationExecutionOptions, OperationExecutionDeletionOptions, OperationExecutionFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var sample = new SampleCreation();
                sample.setTypeId(new EntityTypePermId("MY_SAMPLE_TYPE_CODE"));
                sample.setSpaceId(new SpacePermId("MY_SPACE_CODE"));
                sample.setExperimentId(new ExperimentIdentifier("/MY_SPACE_CODE/MY_PROJECT_CODE/MY_EXPERIMENT_CODE"));
                sample.setCode("MY_SAMPLE_CODE");

                var operation = new CreateSamplesOperation([ sample ]);
                var options = new AsynchronousOperationExecutionOptions();

                // Execute operation

                v3.executeOperations([ operation ], options).done(function(results) {

                    // Explicitly request a deletion of all the information about the execution

                    var deletionOptions = new OperationExecutionDeletionOptions();
                    deletionOptions.setReason("test reason");

                    v3.deleteOperationExecutions([ results.getExecutionId() ], deletionOptions).done(function() {

                        // Let's check whether the execution information is still available

                        v3.getOperationExecutions([ results.getExecutionId() ], new OperationExecutionFetchOptions()).done(function(executions) {

                            var execution = executions[results.getExecutionId()];

                            // Depending on whether a maintenance task has already processed the deletion request
                            // the execution will be either null or the returned execution availability will be DELETE_PENDING.

                            console.log("Availability: " + (execution != null ? execution.getAvailability() : null));

                        });
                    });
                });
            });
    </script>
```

#### Configuration

Many aspects of the operation execution behavior can be configured via
service.properties file.  
More details on what exactly can be configured can be found in the file
itself.

### Semantic Annotations 

If terms like: semantic web, RDF, OWL are new to you, then it is highly
recommended to read the following tutorial first:
<http://www.linkeddatatools.com/semantic-web-basics>.

In short: semantic annotations allow you to define a meaning for openBIS
sample types, property types and sample property assignments by the
means of ontology terms. This, together with standards like "Dublin
Core" (<http://dublincore.org/>) can help you integrate openBIS with
other systems and exchange data between them with a well defined meaning
easily.

To describe a meaning of a single sample type, property type or sample
property assignment a collection of semantic annotations can be used.
Therefore, for instance, you can use one annotation to describe a
general meaning of a property and another one to describe a unit that is
used for its values.

In order to make the openBIS configuration easier to maintain sample
property assignments inherit semantic annotations from a corresponding
property type. This inheritance works only for sample property
assignments without any semantic annotations, i.e. if there is at least
one semantic annotation defined at a sample property assignment level
then nothing gets inherited from the property type level anymore. The
inheritance makes it possible to define a meaning of a property once, at
the property type level, and override it, only if needed, at sample
property assignment level.

V3 API provides the following methods to manipulate the semantic
annotations:

-   createSemanticAnnotations
-   updateSemanticAnnotations
-   deleteSemanticAnnotations
-   getSemanticAnnotations
-   searchSemanticAnnotations

These methods work similar to the other create/update/delete/get/search
V3 API counterparts.

Moreover, once semantic annotations are defined, it is possible to
search for samples and sample types that have a given semantic
annotation. To do it, one has to use searchSamples and searchSampleTypes
methods and specify appropriate withType().withSemanticAnnotations()
condition in SampleSearchCriteria or withSemanticAnnotations() condition
in SampleTypeSearchCriteria. 

### Web App Settings

The web app settings functionality is a user specific key-value map
where a user specific configuration can be stored. The settings are
persistent, i.e. they can live longer than a user session that created
them. Web app settings of a given user can be read/updated only by that
user or by an instance admin.

  

**WebAppSettingsExample.java**

```java
    import java.util.Arrays;
    import java.util.Map;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSetting;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.create.WebAppSettingCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.fetchoptions.WebAppSettingsFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.update.WebAppSettingsUpdateValue;

    public class WebAppSettingsExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

            PersonUpdate update = new PersonUpdate();
            // update the currently logged in user
            update.setUserId(new Me());

            // add "setting1a" and "setting1b" to "app1" (other settings for "app1" will remain unchanged)
            WebAppSettingsUpdateValue app1 = update.getWebAppSettings("app1");
            app1.add(new WebAppSettingCreation("setting1a", "value1a"));
            app1.add(new WebAppSettingCreation("setting1b", "value1b"));

            // set "setting2a", "setting2b" and "setting2c" for "app2" (other settings for "app2" will be removed)
            WebAppSettingsUpdateValue app2 = update.getWebAppSettings("app2");
            app2.set(new WebAppSettingCreation("setting2a", "value2a"), new WebAppSettingCreation("setting2b", "value2b"),
                    new WebAppSettingCreation("setting2c", "value2c"));

            // remove "setting3a" from "app3" (other settings for "app3" will remain unchanged)
            WebAppSettingsUpdateValue app3 = update.getWebAppSettings("app3");
            app3.remove("setting3a");

            v3.updatePersons(sessionToken, Arrays.asList(update));

            // option 1 : fetch a person with all settings of all web apps
            PersonFetchOptions personFo1 = new PersonFetchOptions();
            personFo1.withAllWebAppSettings();

            // option 2 : fetch a person with either all or chosen settings of chosen web apps
            PersonFetchOptions personFo2 = new PersonFetchOptions();

            // option 2a : fetch "app1" with all settings
            WebAppSettingsFetchOptions app1Fo = personFo2.withWebAppSettings("app1");
            app1Fo.withAllSettings();

            // option 2b : fetch "app2" with chosen settings
            WebAppSettingsFetchOptions app2Fo = personFo2.withWebAppSettings("app2");
            app2Fo.withSetting("setting2a");
            app2Fo.withSetting("setting2b");

            Map<IPersonId, Person> persons = v3.getPersons(sessionToken, Arrays.asList(new Me()), personFo2);
            Person person = persons.values().iterator().next();

            // get "setting1a" for "app1"
            WebAppSetting setting1a = person.getWebAppSettings("app1").getSetting("setting1a");
            System.out.println(setting1a.getValue());

            // get all fetched settings for "app2"
            Map<String, WebAppSetting> settings2 = person.getWebAppSettings("app2").getSettings();
            System.out.println(settings2);
        }
    }
```

**WebAppSettingsExample.html**

```html
    <script>
        require([ "jquery", "openbis", "as/dto/person/update/PersonUpdate", "as/dto/person/id/Me", "as/dto/webapp/create/WebAppSettingCreation", "as/dto/person/fetchoptions/PersonFetchOptions" ], 
            function($, openbis, PersonUpdate, Me, WebAppSettingCreation, PersonFetchOptions) {
                $(document).ready(function() {

                    // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                    var update = new PersonUpdate();
                    // update the currently logged in user
                    update.setUserId(new Me());

                    // add "setting1a" and "setting1b" to "app1" (other settings for "app1" will remain unchanged)
                    var app1 = update.getWebAppSettings("app1");
                    app1.add(new WebAppSettingCreation("setting1a", "value1a"));
                    app1.add(new WebAppSettingCreation("setting1b", "value1b"));

                    // set "setting2a", "setting2b" and "setting2c" for "app2" (other settings for "app2" will be removed)
                    var app2 = update.getWebAppSettings("app2");
                    app2.set([ new WebAppSettingCreation("setting2a", "value2a"), new WebAppSettingCreation("setting2b", "value2b"), new WebAppSettingCreation("setting2c", "value2c") ]);

                    // remove "setting3a" from "app3" (other settings for "app3" will remain unchanged)
                    var app3 = update.getWebAppSettings("app3");
                    app3.remove("setting3a");

                    v3.updatePersons([ update ]).done(function() {

                        // option 1 : fetch a person with all settings of all web apps
                        var personFo1 = new PersonFetchOptions();
                        personFo1.withAllWebAppSettings();

                        // option 2 : fetch a person with either all or chosen settings of chosen web apps
                        var personFo2 = new PersonFetchOptions();

                        // option 2a : fetch "app1" with all settings
                        var app1Fo = personFo2.withWebAppSettings("app1");
                        app1Fo.withAllSettings();

                        // option 2b : fetch "app2" with chosen settings
                        var app2Fo = personFo2.withWebAppSettings("app2");
                        app2Fo.withSetting("setting2a");
                        app2Fo.withSetting("setting2b");

                        v3.getPersons([ new Me() ], personFo2).done(function(persons) {
        
                            var person = persons[new Me()];

                            // get "setting1a" for "app1"
                            var setting1a = person.getWebAppSettings("app1").getSetting("setting1a");
                            console.log(setting1a.getValue());

                            // get all fetched settings for "app2"
                            var settings2 = person.getWebAppSettings("app2").getSettings();
                            console.log(settings2);
                        });
                    });
                });
            });
    </script>
```

### Imports

The imports that are normally accesible via "Import" menu in the generic
openBIS UI can be also used programatically from within a V3 custom AS
service. Such an import process consists of two steps:

-   uploading a file to /openbis/upload servlet to be temporarily stored
    under a specific user session key (more information on the upload
    servlet can be found [here](/pages/viewpage.action?pageId=80699317))
-   importing the uploaded file using one
    of ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.IImportService
    methods accessible from within a V3 custom AS service

Currently available import methods:

-   String createExperiments(String sessionToken, String uploadKey,
    String experimentTypeCode, boolean async, String userEmail)

-   String updateExperiments(String sessionToken, String uploadKey,
    String experimentTypeCode, boolean async, String userEmail)

-   String createSamples(String sessionToken, String uploadKey, String
    sampleTypeCode, String defaultSpaceIdentifier, String
    spaceIdentifierOverride, String experimentIdentifierOverride,
    boolean updateExisting, boolean async, String userEmail)

-   String updateSamples(String sessionToken, String uploadKey, String
    sampleTypeCode, String defaultSpaceIdentifier, String
    spaceIdentifierOverride, String experimentIdentifierOverride,
    boolean async, String userEmail)

-   String updateDataSets(String sessionToken, String uploadKey, String
    dataSetTypeCode, boolean async, String userEmail)

-   String createMaterials(String sessionToken, String uploadKey, String
    materialTypeCode, boolean updateExisting, boolean async, String
    userEmail)

-   String updateMaterials(String sessionToken, String uploadKey, String
    materialTypeCode, boolean ignoreUnregistered, boolean async, String
    userEmail)

-   String generalImport(String sessionToken, String uploadKey, String
    defaultSpaceIdentifier, boolean updateExisting,
    boolean async, String userEmail) - import of samples and materials
    from an Excel file

-   String customImport(String sessionToken, String uploadKey, String
    customImportCode, boolean async, String userEmail) - import
    delegated to a dropbox

Parameters:

|Parameter|Type|Methods|Description|
|--- |--- |--- |--- |
|sessionToken|String|ALL|openBIS session token; to get a session token of a currently logged in user inside a custom AS service context.getSessionToken() method shall be used.|
|uploadKey|String|ALL|A key the file to be imported has been uploaded to (see the 1st step of the import process described above).|
|async|boolean|ALL|A flag that controls whether the import should be performed synchronously (i.e. in the current thread) or asynchronously (i.e. in a separate thread). For asynchronous imports an email with either an execution result or error is sent to the specified email address (see userEmail parameter).|
|userEmail|String|ALL|An email address where an execution result or error should be sent to (only for asynchronous imports - see async parameter).|
|experimentTypeCode|String|createExperiments, updateExperiments|A type of experiments to be created/updated.|
|sampleTypeCode|String|createSamples, updateSamples|A type of samples to be created/updated.|
|dataSetTypeCode|String|updateDataSets|A type of data sets to be updated.|
|materialTypeCode|String|createMaterials, updateMaterials|A type of materials to be created/updated.|
|customImportCode|String|customImport|A code of a custom import the import process should be delegated to. A custom import sends the uploaded file to a dropbox. Inside a dropbox the uploaded file can be accessed via transaction.getIncoming() method.|
|defaultSpaceIdentifier|String|createSamples, updateSamples, generalImport|A default space identifier. If null then identifiers of samples to be created/updated are expected to be specified in the uploaded file. If not null then:
codes of samples to be created are automatically generated and the samples are created in the requested default space
identifiers of samples to be updated can omit the space part (the requested default space will be automatically added)|
|spaceIdentifierOverride|String|createSamples, updateSamples|A space identifier to be used instead of the ones defined in the uploaded file.|
|experimentIdentifierOverride|String|createSamples, updateSamples|An experiment identifier to be used instead of the ones defined in the uploaded file.|
|updateExisting|boolean|createSamples, createMaterials, generalImport|A flag that controlls whether in case of an attempt to create an already existing entity an update should be performed or such a creation should fail.|
|ignoreUnregistered|boolean|updateMaterials|A flag that controlls whether in case of an attempt to update a nonexistent entity such update should be silently ignored or it should fail.|

File formats:

The TSV examples below assume experiment/sample/dataset/material type
used contains exactly one property called "DESCRIPTION".

|Method|Template|
|--- |--- |
|createExperiments|create-experiments-import-template.tsv|
|updateExperiments|update-experiments-import-template.tsv|
|createSamples|create-samples-import-template.tsv|
|updateSamples|update-samples-import-template.tsv|
|updateDataSets|update-data-sets-import-template.tsv|
|createMaterials|create-materials-import-template.tsv|
|updateMaterials|update-materials-import-template.tsv|
|generalImport||
|customImport|any kind of file|

Return values:

All methods return a message with a short summary of the performed
operation, e.g. a synchronous createSamples method call could return a
message like "Registration of 1 sample(s) is complete." while the
asynchronous version could return a message like "When the import is
complete the confirmation or failure report will be sent by email.".

An example webapp to upload a file with samples and a custom AS service
to import that file is presented below.

**ImportSamplesWebAppExample.html**

```html
    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="utf-8">
    <title>Samples import</title>

    <script type="text/javascript" src="/openbis-test/resources/api/v3/config.js"></script>
    <script type="text/javascript" src="/openbis-test/resources/api/v3/require.js"></script>

    </head>
    <body>
        <script>
            require([ "jquery", "openbis", "as/dto/service/id/CustomASServiceCode", "as/dto/service/CustomASServiceExecutionOptions" ], function($, openbis, CustomASServiceCode, CustomASServiceExecutionOptions) {
                $(document).ready(function() {
     
                    // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
     
                    var uploadFrame = $("#uploadFrame");
                    uploadFrame.load(function() {
                        alert("Upload finished")
                    });


                    var uploadForm = $("#uploadForm");
                    uploadForm.find("input[name=sessionID]").val(sessionToken);


                    var importForm = $("#importForm");
                    importForm.submit(function(e) {
                        e.preventDefault();
            
                        var sampleType = importForm.find("input[name=sampleType]").val();
                        var serviceId = new CustomASServiceCode("import-service");
                        var serviceOptions = new CustomASServiceExecutionOptions();
                        serviceOptions.withParameter("sampleType", sampleType);

                        facade.executeCustomASService(serviceId, serviceOptions).done(function(result) {
                            alert("Import successful: " + result);
                        }).fail(function(error) {
                            alert("Import failed: " + error.message);
                        });

                        return false;
                    });
                });
            });
        </script>

        <iframe id="uploadFrame" name="uploadFrame" style="display: none"></iframe>

        <h1>Step 1 : upload samples file</h1>
        <form id="uploadForm" method="post" action="/openbis/upload" enctype="multipart/form-data" target="uploadFrame">
            <input type="file" name="importWebappUploadKey" multiple="multiple">
            <input type="hidden" name="sessionID">
            <input type="hidden" name="sessionKeysNumber" value="1">
            <input type="hidden" name="sessionKey_0" value="importWebappUploadKey">
            <input type="submit">
        </form>

        <h1>Step 2 : import samples file</h1>
        <form id="importForm">
            <label>Sample Type</label>
            <input type="text" name="sampleType">
            <input type="submit">
        </form>

    </body>
    </html>
```

**ImportSamplesServiceExample.py**

```python
    def process(context, parameters):
        sampleType = parameters.get("sampleType")
        return context.getImportService().createSamples(context.getSessionToken(), "importWebappUploadKey", sampleType, None, None, None, False, False, None);

### Generate identifiers
```

V3 API provides 2 methods for generating unique identifiers:

-   createPermIdStrings - generates globally unique identifiers that
    consist of a timestamp and a sequence generated number (e.g.
    "20180531170854641-944"); this method uses one global sequence.
-   createCodes - generates identifiers that are unique for a given
    entity kind and consist of a prefix and a sequence generated number
    (e.g. "MY-PREFIX-147"); this method uses a dedicated sequence for
    each entity kind.

**GenerateIdentifiersExample.java**

```java
    import java.util.List;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

    public class GenerateIdentifiersExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
            List<String> permIds = v3.createPermIdStrings(sessionToken, 2);
            List<String> codes = v3.createCodes(sessionToken, "MY-PREFIX-", EntityKind.SAMPLE, 3);

            System.out.println(permIds); // example output: [20180531170854641-944, 20180531170854641-945]
            System.out.println(codes); // example output: [MY-PREFIX-782, MY-PREFIX-783, MY-PREFIX-784]
        }
    }
```

**GenerateIdentifiersExample.html**

```html
    <script>
        require([ "jquery", "openbis", "as/dto/entitytype/EntityKind" ], function($, openbis, EntityKind) {
                $(document).ready(function() {

                    // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)
                    v3.createPermIdStrings(2).then(function(permIds) {
                        console.log(permIds); // example output: [20180531170854641-944, 20180531170854641-945]
                    });

                    v3.createCodes("MY-PREFIX-", EntityKind.SAMPLE, 3).then(function(codes) {
                        console.log(codes); // example output: [MY-PREFIX-782, MY-PREFIX-783, MY-PREFIX-784]
                    });
                });
            });
    </script>
```

## V. DSS Methods

### Search files

The searchFiles method can be used to search for data set files at a
single data store (Java version) or at multiple data stores at the same
time (Javascript version).

Similar to the other V3 search methods it takes as parameters a
sessionToken, search criteria and fetch options and returns a search
result object.

When searching across multiple data stores the results from each data
store are combined together and returned back as a single regular search
result object as if it was returned by only one data store.

#### Example 

**V3SearchDataSetFilesExample.java**

```java
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;


    public class V3SearchDataSetFilesExample
    {
        public static void main(String[] args)
        {
            // we assume here that v3 objects for both AS and DSS have been already created and we have already called login on AS to get the sessionToken (please check "Accessing the API" section for more details)

            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();

            DataSetSearchCriteria dataSetCriteria = criteria.withDataSet().withOrOperator();
            dataSetCriteria.withCode().thatEquals("MY_DATA_SET_CODE_1");
            dataSetCriteria.withCode().thatEquals("MY_DATA_SET_CODE_2");

            // Searches for files at at a single data store
     
            SearchResult<DataSetFile> result = dssV3.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());

            for (DataSetFile file : result.getObjects())
            {
                System.out.println("DataSet: " + file.getDataSetPermId() + " has file: " + file.getPath());
            }
        }
    }
```
 

**V3SearchDataSetFilesAtAllDataStoresExample.html**

```html
    <script>
        require([ "openbis", "dss/dto/datasetfile/search/DataSetFileSearchCriteria", "dss/dto/datasetfile/fetchoptions/DataSetFileFetchOptions" ], 
            function(DataSetFileSearchCriteria, DataSetFileFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var criteria = new DataSetFileSearchCriteria();

                var dataSetCriteria = criteria.withDataSet().withOrOperator();
                dataSetCriteria.withCode().thatEquals("MY_DATA_SET_CODE_1");
                dataSetCriteria.withCode().thatEquals("MY_DATA_SET_CODE_2");

                var fetchOptions = new DataSetFileFetchOptions();

                // getDataStoreFacade() call (without any parameters) returns a facade object that uses all available data stores,
                // e.g. calling searchFiles on such a facade searches for files at all available data stores
     
                v3.getDataStoreFacade().searchFiles(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(file) {
                        console.log("DataSet: " + file.getDataSetPermId() + " has file: " + file.getPath());
                    });
                });
            });
    </script>
```

**V3SearchDataSetFilesAtChosenDataStoresExample.html**

```html
    <script>
        require([ "openbis", "dss/dto/datasetfile/search/DataSetFileSearchCriteria", "dss/dto/datasetfile/fetchoptions/DataSetFileFetchOptions" ], 
            function(DataSetFileSearchCriteria, DataSetFileFetchOptions) {

                // we assume here that v3 object has been already created and we have already called login (please check "Accessing the API" section for more details)

                var criteria = new DataSetFileSearchCriteria();

                var dataSetCriteria = criteria.withDataSet().withOrOperator();
                dataSetCriteria.withCode().thatEquals("MY_DATA_SET_CODE_1");
                dataSetCriteria.withCode().thatEquals("MY_DATA_SET_CODE_2");
           
                var fetchOptions = new DataSetFileFetchOptions();

                // getDataStoreFacade("DSS1","DSS2") returns a facade object that uses only "DSS1" and "DSS2" data stores, 
                // e.g. calling searchFiles on such a facade searches for files only at these two data stores even if there 
                // are more datastores available

                v3.getDataStoreFacade("DSS1", "DSS2").searchFiles(criteria, fetchOptions).done(function(result) {
                    result.getObjects().forEach(function(file) {
                        console.log("DataSet: " + file.getDataSetPermId() + " has file: " + file.getPath());
                    });
                });
            });
    </script>
```

###  Downloading files, folders, and datasets

Datasets that are created in Open BIS can be accessed by V3 API in a
number of different ways. It's possible to download individual files,
folders, and entire datasets as illustrated in the following examples.
To get started, it is necessary to reference both the AS API
(IApplicationServerApi) and the DSS API (IDataStoreServerAPI), and login
and get a session token object.

The API provides two methods for downloading:

-   Simple downloading: A single InputStream is returned which contains
    all files and file meta data.
-   Fast downloading: A FastDownloadSession object is returned which is
    used by a helper class to download files in parallel streams in
    chunks. It is based on the [SIS File Transfer Protocol](#).

### Simple Downloading

By setting the DataSetFileDownloadOptions it's possible to change how
data is downloaded - data can be downloaded file by file, or by folder,
by an entire dataset in a recursive manner. It is also possible to
search for datasets by defining the appropriate search criteria
(DataSetFileSearchCriteria). 

In order to download content via the V3 DSS API, the dataset needs to
already be inside Open BIS. It is necessary to know the dataset code at
the very minimum. It is helpful to also know the file path to the file
desired to download.

#### Download a single file located inside a dataset

Here is how to download a single file and print out the contents, when
the dataset code and the file path are known. Here a search is not
necessary since the file path and dataset code are known.

##### A note about recursion

Note that when only downloading one file, it is better to set the
recursive flag to false in DataSetFileDownloadOptions, although it makes
no difference in the results returned. The recursive flag really only
matters when downloading entire datasets or directories - if it is true,
then the entire tree of contents will be downloaded, if false, then the
single path requested will be downloaded. If that path is just a
directory then the returned result will consist of just meta data about
the directory.

**Download a single file**

```java
    import java.io.InputStream;
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3DSSExample1
    {
        // DATASET EXAMPLE STRUCTURE
        // The dataset consists of a root folder with 2 files and a subfolder with 1 file       
        // root:
        //   - file1.txt
        //   - file2.txt
        //   - subfolder:
        //      - file3.txt
      
        public static void main(String[] args)
        {
            String AS_URL = "https://localhost:8443/openbis/openbis";
            String DSS_URL = "https://localhost:8444/datastore_server";

            // Reference the DSS
            IDataStoreServerApi dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            DSS_URL + IDataStoreServerApi.SERVICE_URL, 10000);

            // Reference the AS and login & get a session token
            IApplicationServerApi as = HttpInvokerUtils
                    .createServiceStub(IApplicationServerApi.class, AS_URL
                            + IApplicationServerApi.SERVICE_URL, 10000);

            String sessionToken = as.login("admin", "password");

            // Download a single file with a path and a dataset code
            DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
            options.setRecursive(false);
            IDataSetFileId fileToDownload = new DataSetFilePermId(
                                                new DataSetPermId("20161205154857065-25"),
                                                "root/subfolder/file3.txt");

            // Download the files into a stream and read them with the file reader
            // Here there is only one file, but we need to put it in an array anyway
            InputStream stream = dss.downloadFiles(sessionToken,
                                                   Arrays.asList(fileToDownload),
                                                   options);

            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
            DataSetFileDownload file = null;

            // Print out the contents
            while ((file = reader.read()) != null)
            {
                System.out.println("Downloaded " + file.getDataSetFile().getPath() + " " + file.getDataSetFile().getFileLength());
                System.out.println("-----FILE CONTENTS-----");
                System.out.println(file.getInputStream());
            }
        }
    }
```

#### Download a folder located inside a dataset

The example below demonstrates how to download a folder and all its
contents, when the dataset code and the folder path are known. The goal
here is to download the directory called "subfolder" and the file
"file3.txt" which will return two objects, one representing the metadata
of the directory, and the other representing both the meta data of
file3.txt and the file contents. Note that setting recursive flag to
true will return both the subfolder directory object AND file3.txt,
while setting recursive flag to false will return just the meta data of
the directory object.

**Download a folder**

```java
    import java.io.InputStream;
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3DSSExample2
    {
        // DATASET EXAMPLE STRUCTURE 
        // The dataset consists of a root folder with 2 files and a subfolder with 1 file
        // root:
        //   - file1.txt
        //   - file2.txt
        //   - subfolder:
        //      - file3.txt
       
        public static void main(String[] args)
        {
            String AS_URL = "https://localhost:8443/openbis/openbis";
            String DSS_URL = "https://localhost:8444/datastore_server";
          
            // Reference the DSS
            IDataStoreServerApi dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            DSS_URL + IDataStoreServerApi.SERVICE_URL, 10000);

            // Reference the AS and login & get a session token
            IApplicationServerApi as = HttpInvokerUtils
                    .createServiceStub(IApplicationServerApi.class, AS_URL
                            + IApplicationServerApi.SERVICE_URL, 10000);

            String sessionToken = as.login("admin", "password");
            // Download a single folder (containing a file inside) with a path and a data set code
            DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
            IDataSetFileId fileToDownload = new DataSetFilePermId(new DataSetPermId("20161205154857065-25"),
                                                                  "root/subfolder");

            // Setting recursive flag to true will return both the subfolder directory object AND file3.txt
            options.setRecursive(true);

            // Setting recursive flag to false will return just the meta data of the directory object
            //options.setRecursive(false);     

            // Read the contents and print them out
            InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(fileToDownload), options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
            DataSetFileDownload file = null;
            while ((file = reader.read()) != null)
            {
                System.out.println("Downloaded " + file.getDataSetFile().getPath() + " " + file.getDataSetFile().getFileLength());
                System.out.println("-----FILE CONTENTS-----");
                System.out.println(file.getInputStream());
            }      
        }
    }
```

#### Search for a dataset and download all its contents, file by file

Here is an example that demonstrates how to search for datasets and
download the contents file by file. Here recursion is not used - see
example 4 for a recursive example. To search for datasets, it is
necessary to assign the appropriate criteria in the
DataSetFileSearchCriteria object. It is also possible to search for
datasets that contain certain files, as demonstrated below. Searching
for files via the searchFiles method returns a list of DataSetFile
objects that contain meta data about the files and also the file
contents. The meta data includes the file perm ids, the dataset perm ids
(the perm ids are objects, not simple codes!), the file path, the file
length, and whether or not the file is a directory. With this list of
files, it is possible to iterate and access the contents as shown in
this example.

  

**Search & download a whole dataset, file by file**

```java
    import java.io.InputStream;
    import java.util.LinkedList;
    import java.util.List;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;


    public class V3DSSExample3
    {
        // DATASET EXAMPLE STRUCTURE
        // The dataset consists of a root folder with 2 files and a subfolder with 1 file
        // root:
        //   - file1.txt
        //   - file2.txt
        //   - subfolder:
        //      - file3.txt
     
        public static void main(String[] args)
        {
            String AS_URL = "https://localhost:8443/openbis/openbis";
            String DSS_URL = "https://localhost:8444/datastore_server";       

            // Reference the DSS
            IDataStoreServerApi dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            DSS_URL + IDataStoreServerApi.SERVICE_URL, 10000);

            // Reference the AS and login & get a session token
            IApplicationServerApi as = HttpInvokerUtils
                    .createServiceStub(IApplicationServerApi.class, AS_URL
                            + IApplicationServerApi.SERVICE_URL, 10000);

            String sessionToken = as.login("admin", "password");
            
            // Create search criteria
            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet().withCode().thatEquals("20161205154857065-25");
            // Search for a dataset with a certain file inside like this:
            //criteria.withDataSet().withChildren().withPermId(mypermid);
            // Search for the files & put the file perm ids in a list for easy access
            // (file perm ids are objects containing meta data describing the file) 
            SearchResult<DataSetFile> result = dss.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());
            List<DataSetFile> files = result.getObjects();

            // This returns the following list of objects:
            // DataSetFile("root", isDirectory = true)
            // DataSetFile("root/file1.txt", isDirectory = false)
            // DataSetFile("root/file2.txt", isDirectory = false)
            // DataSetFile("root/subfolder", isDirectory = true)
            // DataSetFile("root/subfolder/file3.txt", isDirectory = false)
           
            List<IDataSetFileId> fileIds = new LinkedList<IDataSetFileId>();
            for (DataSetFile file : files)
            {
                System.out.println(file.getPath() + " " + file.getFileLength());
                fileIds.add(file.getPermId());
            }

            // Download the files & print the contents
            DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
            options.setRecursive(false);
            InputStream stream = dss.downloadFiles(sessionToken, fileIds, options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
            DataSetFileDownload file = null;
            while ((file = reader.read()) != null)
            {
                System.out.println("Downloaded " + file.getDataSetFile().getPath() + " " + file.getDataSetFile().getFileLength());
                System.out.println(file.getInputStream());
            }
        }
    }
```

#### Download a whole dataset recursively

Here is a simplified way to download a dataset. Instead of downloading
files one by one, it is possible to download the entire dataset
recursively by simply setting the recursive file to true in the
DataSetFileDownloadOptions object.

**Download a whole dataset recursively**

```java
    import java.io.InputStream;
    import java.util.Arrays;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3DSSExample4
    {
        // DATASET EXAMPLE STRUCTURE
        // The dataset consists of a root folder with 2 files and a subfolder with 1 file
        // root:
        //   - file1.txt
        //   - file2.txt
        //   - subfolder:
        //      - file3.txt

        public static void main(String[] args)
        {
            String AS_URL = "https://localhost:8443/openbis/openbis";
            String DSS_URL = "https://localhost:8444/datastore_server";
          
            // Reference the DSS
            IDataStoreServerApi dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            DSS_URL + IDataStoreServerApi.SERVICE_URL, 10000);

            // Reference the AS and login & get a session token
            IApplicationServerApi as = HttpInvokerUtils
                    .createServiceStub(IApplicationServerApi.class, AS_URL
                            + IApplicationServerApi.SERVICE_URL, 10000);

            String sessionToken = as.login("admin", "password");

            // Download the files and print the contents
            DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
            IDataSetFileId fileId = new DataSetFilePermId(new DataSetPermId("20161205154857065-25"));
            options.setRecursive(true);
            InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(fileId), options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
            DataSetFileDownload file = null;

            while ((file = reader.read()) != null)
            {
                file.getInputStream();
                System.out.println("Downloaded " + file.getDataSetFile().getPath() + " " + file.getDataSetFile().getFileLength());
            }
        }
    }
```

#### Search and list all the files inside a data store 

Here is an example that demonstrates how to list all the files in a data
store. By simply leaving the following line as is:

    DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();

it will automatically return every object in the data store. This is
useful when it is desired to list an entire directory or iterate over
the whole data store. 

**Search and list all files inside a data store**

```java
    import java.io.InputStream;
    import java.util.LinkedList;
    import java.util.List;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class V3DSSExample5
    {
        // DATASET EXAMPLE STRUCTURE
        // The dataset consists of a root folder with 2 files and a subfolder with 1 file
        // root:
        //   - file1.txt
        //   - file2.txt
        //   - subfolder:
        //      - file3.txt

        public static void main(String[] args)
        {
            String AS_URL = "https://localhost:8443/openbis/openbis";
            String DSS_URL = "https://localhost:8444/datastore_server";

            // Reference the DSS
            IDataStoreServerApi dss =
                    HttpInvokerUtils.createStreamSupportingServiceStub(IDataStoreServerApi.class,
                            DSS_URL + IDataStoreServerApi.SERVICE_URL, 10000);

            // Reference the AS and login & get a session token
            IApplicationServerApi as = HttpInvokerUtils
                    .createServiceStub(IApplicationServerApi.class, AS_URL
                            + IApplicationServerApi.SERVICE_URL, 10000);

            String sessionToken = as.login("admin", "password");

            // Create search criteria
            DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
            criteria.withDataSet();

            //comment out this line below, and just leave the criteria empty - and it will return everything.
            //criteria.withDataSet().withCode().thatEquals("20151201115639682-98322");
            // Search for the files & put the file perm ids (objects containing meta data) in a list for easy access
            SearchResult<DataSetFile> result = dss.searchFiles(sessionToken, criteria, new DataSetFileFetchOptions());        
            List<DataSetFile> files = result.getObjects();
            List<IDataSetFileId> fileIds = new LinkedList<IDataSetFileId>();
            for (DataSetFile file : files)
            {
                System.out.println(file.getPath() + " " + file.getFileLength());
                fileIds.add(file.getPermId());
            }

            // Download the files and print the contents
            DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
            options.setRecursive(false);
            InputStream stream = dss.downloadFiles(sessionToken, fileIds, options);
            DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);
            DataSetFileDownload file = null;


            while ((file = reader.read()) != null)
            {
                System.out.println("Downloaded " + file.getDataSetFile().getPath() + " " + file.getDataSetFile().getFileLength());
                System.out.println("-----FILE CONTENTS-----");
                System.out.println(file.getInputStream());
            }
        }
    }
```

### Fast Downloading

Fast downloading is based on the [SIS File Transfer Protocol](#) and
library. Downloading is done in two steps:

1.  Create a fast download session with the
    method `createFastDownloadSession()` on  V3 DSS API. One parameter
    is a list of data set file ids. Such an id contains the data set
    code and the path to the file inside the data set. If a file id
    points to a folder the whole folder will be downloaded. The last
    parameter specifies download preferences. Currently only the wished
    number of parallel download streams can be specified. The API call
    returns a `FastDownloadSession` object.

2.  Download the files with the helper class `FastDownloader`. The
    simplest usage is just do ``

    **Search and list all files inside a data store**

        new FastDownloader(downloadSession).downloadTo(destinationFolder);

    The files are stored in the destination folder in <data set
    code>/<relative file path as in the data store on openBIS>.

Here is a complete example:

**Search and list all files inside a data store**

```java
    import java.io.File;
    import java.nio.file.Path;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.List;
    import java.util.Map;
    import java.util.Map.Entry;

    import org.apache.commons.lang3.time.StopWatch;

    import ch.ethz.sis.filetransfer.DownloadListenerAdapter;
    import ch.ethz.sis.filetransfer.IDownloadItemId;
    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
    import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloadResult;
    import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloader;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
    import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;

    public class V3FastDownloadExample
    {
        public static void main(String[] args)
        {
            IApplicationServerApi v3 =
                    HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, "http://localhost:8888/openbis/openbis"
                            + IApplicationServerApi.SERVICE_URL, 10000);
            String sessionToken = v3.login("test", "password");

            // Search for some data sets
            DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
            searchCriteria.withCode().thatStartsWith("201902");
            DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
            fetchOptions.withDataStore();
            fetchOptions.withPhysicalData();
            List<DataSet> dataSets = v3.searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();

            // Get the DSS URL from the first data set assuming that all data sets from the same data store
            String dssUrl = dataSets.get(0).getDataStore().getDownloadUrl();
            System.out.println("url:" + dssUrl);

            // Create DSS server
            IDataStoreServerApi dssServer = new ServiceFinder("datastore_server", IDataStoreServerApi.SERVICE_URL)
                    .createService(IDataStoreServerApi.class, dssUrl);

            // We download all files of the all found data sets.
            List<DataSetFilePermId> fileIds = new ArrayList<>();
            for (DataSet dataSet : dataSets)
            {
                fileIds.add(new DataSetFilePermId(new DataSetPermId(dataSet.getCode())));
            }

            // Create the download session for 2 streams in parallel (if possible)
            FastDownloadSession downloadSession = dssServer.createFastDownloadSession(sessionToken,
                    fileIds, new FastDownloadSessionOptions().withWishedNumberOfStreams(2));

            // Do the actual download into 'targets/fast-download' and print the time needed by using a download listener
            FastDownloadResult result = new FastDownloader(downloadSession).withListener(
                    new DownloadListenerAdapter()
                        {
                            private StopWatch stopWatch = new StopWatch();

                            @Override
                            public void onDownloadStarted()
                            {
                                stopWatch.start();
                            }
                            @Override
                            public void onDownloadFinished(Map<IDownloadItemId, Path> itemPaths)
                            {
                                System.out.println("Successfully finished after " + stopWatch);
                            }
                            @Override
                            public void onDownloadFailed(Collection<Exception> e)
                            {
                                System.out.println("Downloading failed after " + stopWatch);
                            }
                        })
                    .downloadTo(new File("targets/fast-download"));

            // Print the mapping of data set file id to the actual path
            for (Entry<IDataSetFileId, Path> entry : result.getPathsById().entrySet())
            {
                System.out.println(entry);
            }

            v3.logout(sessionToken);
        }
    }
```

#### What happens under the hood?

The files to be downloaded are chunked into chunks of maximum size 1 MB.
On the DSS a special web service (`FileTransferServerServlet`) provides
these chunks. On the client side these chunks are requested and stored
in the file system. This is done in parallel if possible and requested
(withWishedNumberOfStreams). The server tells the client the actual
number of streams available for parallel downloading without slowing
down DSS. The actual number of streams depends on

-   the wished number of streams
-   the number of streams currently used by other download sessions
-   the maximum number of allowed streams as specified by the
    property `api.v3.fast-download.maximum-number-of-allowed-streams` in
    DSS `service.properties`. Default value is 10.

The actual number of streams is half of the number of free streams or
the wished number of streams, if it is less. The number of free streams
is given by the difference between the maximum number of allowed streams
and the total number of used streams. 

It is possible that the actual number of streams is zero if the server
is currently too busy with downloading (that is, there is no free
dowload stream available). The FastDownloader will retry it later.

#### Customizing Fast Dowloading

There are three ways to customizing the FastDownloader:

-   withListener(): Adds a listener which will be notified when
    -   the download session has been started/finished/failed,
    -   the download of a file/folder has been started/finished and
    -   a chunk has been downloaded.  
        There can be several listeners. By default there are no
        listeners. Note, that listeners are notified in a separated
        thread associated with the download session.
-   withLogger(): Sets a logger. By default nothing is logged.
-   withRetryProviderFactory(): Sets the factory which creates a retry
    provider. A retry provider knows when and how often a failed action
    (e.g. sever request) should be retried. By default it is retried
    three times. The first retry is a second later. For each following
    retry the waiting time is increases by the factor two.

### Register Data Sets

To register datasets using the Java or JavaScript API use one of the
following examples as a template.

**Example (Java)**

**Register Data Set**

```java
    import java.util.UUID;
    import org.eclipse.jetty.client.HttpClient;
    import org.eclipse.jetty.client.api.Request;
    import org.eclipse.jetty.client.util.MultiPartContentProvider;
    import org.eclipse.jetty.client.util.StringContentProvider;
    import org.eclipse.jetty.http.HttpMethod;

    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
    import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
    import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
    import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
    import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

    public class RegisterDataSet
    {
        public static void main(String[] args) throws Exception
        {         
            final String AS_URL = "http://localhost:8888/openbis/openbis";
            final String DSS_URL = "http://localhost:8889/datastore_server";

            final OpenBIS openbisV3 = new OpenBIS(AS_URL, DSS_URL);

            openbisV3.login("admin", "password");

            final Path path = Path.of("/uploadPath");
            final String uploadId = openbisV3.uploadFileWorkspaceDSS(path);

            final UploadedDataSetCreation creation = new UploadedDataSetCreation();
            creation.setUploadId(uploadId);
            creation.setExperimentId(new ExperimentIdentifier("/DEFAULT/DEFAULT/DEFAULT"));
            creation.setTypeId(new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET));

            try
            {
                final DataSetPermId dataSetPermId = openbisV3.createUploadedDataSet(creation);
                // A data set assigned to the experiment "/DEFAULT/DEFAULT/DEFAULT" with the folder "uploadPath" is created
                System.out.println("dataSetPermId=" + dataSetPermId);
            } catch (final Exception e)
            {
                e.printStackTrace();
            }

            openbisV3.logout();
        }
    }
```

**Example (Javascript)**

**Register Data Set**

```html
    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="utf-8">
    <title>Dataset upload</title>

    <script type="text/javascript" src="/openbis-test/resources/api/v3/config.js"></script>
    <script type="text/javascript" src="/openbis-test/resources/api/v3/require.js"></script>

    </head>
    <body>
        <label for="myfile">Select a file:</label>
        <input type="file" id="myFile"/>
        <script>         
            require(["openbis", "dss/dto/dataset/create/UploadedDataSetCreation", "as/dto/experiment/id/ExperimentIdentifier",
                "as/dto/entitytype/id/EntityTypePermId", "as/dto/entitytype/EntityKind"],
            function(openbis, UploadedDataSetCreation, ExperimentIdentifier, EntityTypePermId, EntityKind) {
                var testProtocol = window.location.protocol;
                var testHost = window.location.hostname;
                var testPort = window.location.port;

                var testUrl = testProtocol + "//" + testHost + ":" + testPort;
                var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

                var openbisV3 = new openbis(testApiUrl);

                var fileInput = document.getElementById("myFile");
                fileInput.onchange = (e) => {
                    var files = e.target.files;

                    openbisV3.login("admin","password").done(sessionToken => {
                        var dataStoreFacade = openbisV3.getDataStoreFacade();
                        dataStoreFacade.uploadFilesWorkspaceDSS(files).done(uploadId => {
                            var creation = new UploadedDataSetCreation();
                            creation.setUploadId(uploadId);
                            creation.setExperimentId(new ExperimentIdentifier("/DEFAULT/DEFAULT/DEFAULT"));
                            creation.setTypeId(new EntityTypePermId("ATTACHMENT", EntityKind.DATA_SET));

                            dataStoreFacade.createUploadedDataSet(creation).done(dataSetPermId => {
                                 // A data set assigned to the experiment "/DEFAULT/DEFAULT/DEFAULT" with the folder "uploadPath" is created
                                console.log("dataSetPermId=" + dataSetPermId);
                                openbisV3.logout();
                            }).fail(error => {
                                console.error(error);
                                openbisV3.logout();
                            });
                        });
                    });
                }
            });     
        </script>
    </body>
    </html>
```

## VI. Web application context

When making web applications and embedding them into an openBIS tab on
the core UI is often required to have information about the context
those applications are being loaded for two particular purposes:

-   Making the application context sensitive and show
    information/functionality related to the current context. The
    context object provided by **getWebAppContext()** contains all
    information required for this purpose.
-   Login into the facade without presenting the user with another login
    screen since they have already login into openBIS. For
    that **loginFromContext()** can be used.

This methods only exist on the Javascript facade with the purpose of
being used on embedded web applications, calling them from an external
web application will do nothing.

**WebAppContextExample.html**

```html
    <script>
        require(['openbis'], function(openbis) {
                var openbisV3 = new openbis();
                var webappcontext = openbisV3.getWebAppContext();

                console.log(webappcontext.getWebappCode());
                console.log(webappcontext.getSessionId());
                console.log(webappcontext.getEntityKind());
                console.log(webappcontext.getEntityType());
                console.log(webappcontext.getEntityIdentifier());
                console.log(webappcontext.getEntityPermId());
                
                openbisV3.loginFromContext();
                openbisV3.getSessionInformation().done(function(sessionInfo) {
                    console.log(sessionInfo.getUserName());
                });
            });
    </script>
```
