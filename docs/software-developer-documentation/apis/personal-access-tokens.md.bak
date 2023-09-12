# Personal Access Tokens

## Background

"Personal access token" (in short: PAT) is an openBIS feature that was
introduced to simplify integration of openBIS with other systems. Such
integrations are usually done using openBIS V3 API and therefore require
an external application to authenticate in openBIS to fetch or create
some data. Without "Personal access tokens" the only way of
authenticating in openBIS V3 API was the V3 API login method. Given a
user name and a password the login method would return back an openBIS
session token, which could be later used in other V3 API calls as a
secret and a proof of who we are.

Unfortunately, even though this approach worked well it had some
limitations. These were mainly caused by the nature of session tokens in
openBIS:

-   session tokens are short lived
-   session tokens do not survive openBIS restarts
-   obtaining a new session token requires a user name and a password

Because of these limitations external applications had to be prepared
for a situation where an openBIS session token stops working. They had
to know how to recover. When one session token expired or was
invalidated they had to obtain a new one by calling the login method
again and providing a user name and a password. But even then the whole
state of the previous session (e.g. files stored in the session
workspace) would be gone and not available in the new session.

Depending on a use case and a type of the integration that could cause
smaller or bigger headaches for the developers of the external system.
Fortunately, "Personal access tokens" come to a rescue.

## What are "Personal access tokens" ?

A personal access token (in short: PAT) is very similar to a session
token but there are also some important differences.

Similarities:

-   a PAT is bound to a specific user and represents that user's
    session. Two users can't share a session using PAT. Internal PAT
    sessions identifier is the combination of both the userId and the
    session name.
-   a PAT is a secret that must not be publicly shared (having a user's
    PAT one can perform any actions in openBIS that this user could
    normally perform, except for user and PAT management)
-   a user can have multiple PATs active at the same time
-   a PAT can be used in places where a regular session token could be
    normally used, e.g. to call V3 API methods (a full list of endpoints
    that support PATs is presented below)

Differences:

-   a PAT is created using a dedicated "createPersonalAccessTokens" V3
    API method (not using "login" method as a regular session token)
-   a PAT can be long lived (its validFrom and validTo dates are defined
    at the moment of creation), still it should be replaced periodically
    for security reasons
-   a PAT session survives openBIS restarts, i.e. the same PAT can be
    used before and after a restart (session workspace folder state is
    also kept)
-   multiple PATs may represent a single PAT session (both PATs must
    have the same "session name") - this becomes useful for handling a
    transition period from one soon to be expired PAT to a new PAT that
    replaces it without losing the session's state

## Who can create a "Personal access token" ?

Any openBIS user can manage its own PATs. Instance admin users can
manage all PATs in the system.

## Where can I use "Personal access tokens" ?

Endpoints that support PATs:

AS:

-   V3 API
-   File Upload Servlet (class: UploadServiceServlet, path: /upload)
-   File Download Servlet (class: DownloadServiceServlet, path:
    /download)
-   Session Workspace Provider

DSS:

-   V3 API
-   File Upload Servlet (class: StoreShareFileUploadServlet, path:
    /store\_share\_file\_upload)
-   File Download Servlet (class: DatasetDownloadServlet, path: /\*)
-   Session Workspace Upload Servlet (class:
    SessionWorkspaceFileUploadServlet, path:
    /session\_workspace\_file\_upload)
-   Session Workspace Download Servlet (class:
    SessionWorkspaceFileDownloadServlet, path:
    /session\_workspace\_file\_download)
-   Session Workspace Provider
-   SFTP

## Where "Personal access tokens" are stored ?

PATs are stored in "personal-access-tokens.json" JSON file. By default
the file is located in the main openBIS folder where it survives openBIS
restarts and upgrades.

The location can be changed using "personal-access-tokens-file-path"
property in AS service.properties. The JSON file is read at the openBIS
start up.

## How long should my "Personal Access Tokens" be valid ?

Because of security reasons PATs should not be valid indefinitely.
Instead, each PAT should have a well defined validity period after which
it should be replaced with a new PAT with a different hash. To make this
transition as smooth as possible please use the following guide:

-   create PAT\_1 with sessionName = <MY\_SESSION> and use it in
    your integration
-   when PAT\_1 is soon to be expired, create PAT\_2 with the same
    sessionName = <MY\_SESSION> (both PAT\_1 and PAT\_2 will work
    at this point and will refer to the same openBIS session)
-   replace PAT\_1 with PAT\_2 in your integration

PATs created by the same user and with the same "session name" refer
under the hood to the same openBIS session. Therefore, even if one of
such PATs expires the session is kept active and its state is
maintained.

## Configuration

"Personal access tokens" functionality is enabled by default. To
configure it please use AS service.properties:

    # personal access tokens feature
    personal-access-tokens-enabled = true

    # change the default location of the JSON file that stores personal access tokens (default: personal-access-tokens.json file in the main openBIS folder)
    personal-access-tokens-file-path = MY_FOLDER/personal-access-tokens.json

    # set maximum allowed validity period (in seconds) - personal access token with a longer validity period cannot be created (default: 30 days)
    personal-access-tokens-max-validity-period = 2592000

    # set validity warning period (in seconds) - owners of personal access tokens that are going to expire within this warning period are going to receive email notifications (default: 5 days)
    personal-access-tokens-validity-warning-period = 259200

## Typical Application Workflow

Most typical use case for Personal Access Tokens is to run code on a
third party service against openBIS.

On such services we want to have:

1.  A long lasting session with openBIS for several days that survives
    restarts.
2.  We don't want to keep the user and password stored.

For such services we recommend to create a PAT on log in and store the
PAT instead. We provide the example Gradle project with the java class
PersonalAccessTokensApplicationWorkflows ([source downloadable
here](att/src.zip))
as the recommend way to manage getting the most up to date personal
access token for an application and user. Including creation and renewal
management.

```java
        private static final String URL = "https://openbis-sis-ci-sprint.ethz.ch/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
        private static final int TIMEOUT = 10000;

        private static final String USER = "admin";
        private static final String PASSWORD = "changeit";

        public static void main(String[] args) {
            IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
            String sessionToken = v3.login(USER, PASSWORD);
            System.out.println("sessionToken: " + sessionToken);
            PersonalAccessTokenPermId pat = PersonalAccessTokensApplicationWorkflows.getApplicationPersonalAccessTokenOnLogin(v3, sessionToken, "MY_APPLICATION");
            System.out.println("pat: " + pat);
            v3.logout(sessionToken);
        }

    package ch.ethz.sis.pat;

    import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
    import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
    import org.apache.commons.lang3.time.DateUtils;

    import java.util.Calendar;
    import java.util.Date;
    import java.util.List;
    import java.util.Map;

    public class PersonalAccessTokensApplicationWorkflows {

        private static final int DAY_IN_SECONDS = 24 * 60 * 60;

        private static final String PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD = "personal-access-tokens-max-validity-period";

        private static final String PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD = "personal-access-tokens-validity-warning-period";

        private PersonalAccessTokensApplicationWorkflows() {

        }

        /*
         * This utility method returns the current application token, creates one if no one is found and renews it if is close to expiration.
         * Requires are real session token hence requires a form where the user can input its user and password on an application.
         */
        public static PersonalAccessTokenPermId getApplicationPersonalAccessTokenOnLogin(IApplicationServerApi v3, String sessionToken, String applicationName) {
            // Obtain servers renewal information
            Map<String, String> information = v3.getServerInformation(sessionToken);
            int personalAccessTokensRenewalPeriodInSeconds = Integer.parseInt(information.get(PersonalAccessTokensApplicationWorkflows.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD));
            int personalAccessTokensRenewalPeriodInDays = personalAccessTokensRenewalPeriodInSeconds / DAY_IN_SECONDS;
            int personalAccessTokensMaxValidityPeriodInSeconds = Integer.parseInt(information.get(PersonalAccessTokensApplicationWorkflows.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD));
            int personalAccessTokensMaxValidityPeriodInDays = personalAccessTokensMaxValidityPeriodInSeconds / DAY_IN_SECONDS;

            // Obtain user id
            SessionInformation sessionInformation = v3.getSessionInformation(sessionToken);

            // Search for PAT for this user and application
            // NOTE: Standard users only get their PAT but admins get all, filtering with the user solves this corner case
            PersonalAccessTokenSearchCriteria personalAccessTokenSearchCriteria = new PersonalAccessTokenSearchCriteria();
            personalAccessTokenSearchCriteria.withSessionName().thatEquals(applicationName);
            personalAccessTokenSearchCriteria.withOwner().withUserId().thatEquals(sessionInformation.getPerson().getUserId());

            SearchResult<PersonalAccessToken> personalAccessTokenSearchResult = v3.searchPersonalAccessTokens(sessionToken, personalAccessTokenSearchCriteria, new PersonalAccessTokenFetchOptions());
            PersonalAccessToken bestTokenFound = null;
            PersonalAccessTokenPermId bestTokenFoundPermId = null;

            // Obtain longer lasting application token
            for (PersonalAccessToken personalAccessToken:personalAccessTokenSearchResult.getObjects()) {
                if (personalAccessToken.getValidToDate().after(new Date())) {
                    if (bestTokenFound == null) {
                        bestTokenFound = personalAccessToken;
                    } else if (personalAccessToken.getValidToDate().after(bestTokenFound.getValidToDate())) {
                        bestTokenFound = personalAccessToken;
                    }
                }
            }

            // If best token doesn't exist, create
            if (bestTokenFound == null) {
                bestTokenFoundPermId = createApplicationPersonalAccessToken(v3, sessionToken, applicationName, personalAccessTokensMaxValidityPeriodInDays);
            }

            // If best token is going to expire in less than the warning period, renew
            Calendar renewalDate = Calendar.getInstance();
            renewalDate.add(Calendar.DAY_OF_MONTH, personalAccessTokensRenewalPeriodInDays);
            if (bestTokenFound != null && bestTokenFound.getValidToDate().before(renewalDate.getTime())) {
                bestTokenFoundPermId = createApplicationPersonalAccessToken(v3, sessionToken, applicationName, personalAccessTokensMaxValidityPeriodInDays);
            }

            // If we have not created or renewed, return current
            if (bestTokenFoundPermId == null) {
                bestTokenFoundPermId = bestTokenFound.getPermId();
            }

            return bestTokenFoundPermId;
        }

        private static PersonalAccessTokenPermId createApplicationPersonalAccessToken(IApplicationServerApi v3, String sessionToken, String applicationName, int personalAccessTokensMaxValidityPeriodInDays) {
            PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
            creation.setSessionName(applicationName);
            creation.setValidFromDate(new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY));
            creation.setValidToDate(new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY * personalAccessTokensMaxValidityPeriodInDays));
            List<PersonalAccessTokenPermId> personalAccessTokens = v3.createPersonalAccessTokens(sessionToken, List.of(creation));
            return personalAccessTokens.get(0);
        }

    }
```

## V3 API 

Code examples for personal access tokens can be found in the main V3 API documentation: [openBIS V3 API\#PersonalAccessTokens](https://openbis.readthedocs.io/en/latest/software-developer-documentation/apis/personal-access-tokens.html#personal-access-tokens)
