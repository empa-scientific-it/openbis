# Service plugins

## Introduction

A service plugin runs on a DSS. It is a java servlet that processes
incoming requests and generates the responses. A user can trigger a
service plugin by accessing an url the servlet has been set up for. A
service plugin is configured on the DSS best by introducing a [core
plugin](../software-developer-documentation/server-side-extensions/core-plugins.md#core-plugins) of type services. All
service plugins have the following properties in common:

|Property Key|Description|
|--- |--- |
|class|The fully-qualified Java class name of the service plugin. The class has to implement javax.servlet.Servlet  interface.|
|path|The path the servlet will be available at. For instance, a service with /test-path/* path can be access via  http://my-data-store/test-path  url.|

## Service Plugins

### ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.OaipmhServlet

A servlet that handles OAI-PMH protocol requests (see
<http://www.openarchives.org/OAI/openarchivesprotocol.html> for more
details on OAI-PMH). The requests are handled in two steps:

-   user authentication

-   response generation

The user authentication step is handled by
ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IAuthenticationHandler.
The handler is configured via "authentication-handler" property. The
response generation step is handled
by ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IRequestHandler.
The handler is configured via "request - handler" property. An example
of such a configuration is presented below:

**Example**:

**plugin.properties**

```
class = ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.OaipmhServlet
path = /oaipmh/*
request-handler = ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.JythonBasedRequestHandler
request-handler.script-path = handler.py
authentication-handler = ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.BasicHttpAuthenticationHandler
```


**Configuration**:

|Property Key|Description|
|--- |--- |
|authentication-handler|A class that implements ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IAuthenticationHandler interface. The handler is responsible for authenticating a user for an OAI-PMH request.Currently there are two implementations of this handler available:ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.BasicHttpAuthenticationHandlerch.systemsx.cisd.openbis.dss.generic.server.oaipmh.AnonymousAuthenticationHandlerSee the sections below for more details on these handlers.|
|request-handler|A class that implements   ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IRequestHandler   interface. The h andler is responsible for generating a response for an OAI-PMH request.Currently there are two implementations of this handler available:ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.JythonBasedRequestHandlerch.systemsx.cisd.openbis.dss.screening.server.oaipmh.ScreeningJythonBasedRequestHandlerSee the sections below for more details on these handlers.|

#### ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.BasicHttpAuthenticationHandler

Handler that performs Basic HTTP authentication as described here:
<http://en.wikipedia.org/wiki/Basic_access_authentication>.

#### ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.AnonymousAuthenticationHandler

Handler that allows clients to access the OAI-PMH service without any
authentication. The handler automatically authenticates as a user
specified in the configuration.

**Configuration:**

|Property Key|Description|
|--- |--- |
|user|User that should be used for the automatic authentication, e.g. observer|
|password|Password of the user|

#### ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.JythonBasedRequestHandler

OAI-PMH response handler that delegates a response generation to a
Jython script. The script can be configured via "script-path" property.
The script should define a function with a following signature:

**handler.py**

`def handle(request, response)`

where request is javax.servlet.http.HttpServletRequest request and
response is javax.servlet.http.HttpServletResponse. Following variables
are available in the script:

-   searchService
    - ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
-   searchServiceUnfiltered -
    ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService
-   mailService -
    ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IMailService
-   queryService -
    ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService
-   authorizationService -
    ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization.IAuthorizationService
-   sessionWorkspaceProvider -
    ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider
-   contentProvider -
    ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetContentProvider
-   contentProviderUnfiltered -
    ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetContentProvider
-   userId

**Configuration:**

|Property Key|Description|
|--- |--- |
|script-path|The path to a jython script that should handle OAI-PMH requests.|

An example of a jython script that can be used for handling OAI-PMH
responses is presented below. The example uses XOAI java library
(see <https://github.com/lyncode/xoai> for more details on the library)
to provide dataset metadata. XOAI library is available in openBIS and
can be used without any additional configuration.

**handler.py**

```py
#! /usr/bin/env python
from java.util import Date
from java.text import SimpleDateFormat
from xml.etree import ElementTree
from xml.etree.ElementTree import Element, SubElement 
from com.lyncode.xoai.dataprovider import DataProvider
from com.lyncode.xoai.dataprovider.model import Context, MetadataFormat, Item
from com.lyncode.xoai.dataprovider.repository import Repository, RepositoryConfiguration
from com.lyncode.xoai.dataprovider.parameters import OAIRequest
from com.lyncode.xoai.dataprovider.handlers.results import ListItemIdentifiersResult, ListItemsResults
from com.lyncode.xoai.model.oaipmh import OAIPMH, DeletedRecord, Granularity, Metadata 
from com.lyncode.xoai.xml import XmlWriter
from ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai import SimpleItemIdentifier, SimpleItem, SimpleItemRepository, SimpleSetRepository
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute, MatchClauseTimeAttribute, CompareMode 
DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
TIME_ZONE = "0"

def handle(req, resp):
    context = Context();
    context.withMetadataFormat(MetadataFormat().withPrefix("testPrefix").withTransformer(MetadataFormat.identity()));
    configuration = RepositoryConfiguration();
    configuration.withMaxListSets(100);
    configuration.withMaxListIdentifiers(100);
    configuration.withMaxListRecords(100);
    configuration.withAdminEmail("test@test");
    configuration.withBaseUrl("http://localhost");
    configuration.withDeleteMethod(DeletedRecord.NO);
    configuration.withEarliestDate(Date(0));
    configuration.withRepositoryName("TEST");
    configuration.withGranularity(Granularity.Day);
    repository = Repository();
    repository.withConfiguration(configuration);
    repository.withItemRepository(ItemRepository());
    repository.withSetRepository(SimpleSetRepository());
    provider = DataProvider(context, repository);
    params = {}
    for param in req.getParameterNames():
        values = []
        for value in req.getParameterValues(param):
            values.append(value)
        params[param] = values
    request = OAIRequest(params);
    response = provider.handle(request);
    writer = XmlWriter(resp.getOutputStream());
    response.write(writer);
    writer.flush();

class ItemRepository(SimpleItemRepository):
    
    def doGetItem(self, identifier):
        criteria = SearchCriteria()
        criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, identifier))
        dataSets = searchService.searchForDataSets(criteria)
        
        if dataSets:
            return createItem(dataSets[0])
        else:
            return None
    def doGetItemIdentifiers(self, filters, offset, length, setSpec, fromDate, untilDate):
        results = self.doGetItems(filters, offset, length, setSpec, fromDate, untilDate)
        return ListItemIdentifiersResult(results.hasMore(), results.getResults(), results.getTotal())
    
    def doGetItems(self, filters, offset, length, setSpec, fromDate, untilDate):
        criteria = SearchCriteria()
        if fromDate:
            criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.GREATER_THAN_OR_EQUAL, DATE_FORMAT.format(fromDate), TIME_ZONE))
        if untilDate:
            criteria.addMatchClause(MatchClause.createTimeAttributeMatch(MatchClauseTimeAttribute.REGISTRATION_DATE, CompareMode.LESS_THAN_OR_EQUAL, DATE_FORMAT.format(untilDate), TIME_ZONE))
        dataSets = searchService.searchForDataSets(criteria)
        if dataSets:
            hasMoreResults = (offset + length) < len(dataSets)
            results = [createItem(dataSet) for dataSet in dataSets[offset:(offset + length)]]
            total = len(dataSets)
            return ListItemsResults(hasMoreResults, results, total)
        else:
            return ListItemsResults(False, [], 0)


def createItemMetadata(dataSet):
    properties = Element("properties")
    
    for propertyCode in dataSet.getAllPropertyCodes():
        property = SubElement(properties, "property")
        property.set("code", propertyCode)
        property.text = dataSet.getPropertyValue(propertyCode) 
        
    return Metadata(ElementTree.tostring(properties))

def createItem(dataSet):
    item = SimpleItem()
    item.setIdentifier(dataSet.getDataSetCode())
    item.setDatestamp(Date())
    item.setMetadata(createItemMetadata(dataSet))
    return item
```

now assuming that the OaipmhServlet has been configured at /oaipmh path
try accessing the following urls:

- \<data store url\>/oaipmh/?verb=Identify - returns information about this OAI-PMH repository
- \<data store url\>/oaipmh/?verb=ListIdentifiers&metadataPrefix=testPrefix - returns the first 100 of data set codes and a resumption token if there is more than 100 data sets available
- \<data store url\>/oaipmh/?verb=ListIdentifiers&resumptionToken=\<resumption token\> - returns another 100 of data set codes
- \<data store url\>/oaipmh/?verb=ListRecords&metadataPrefix=testPrefix - returns the first 100 of data set records and a resumption token if there is more than 100 data sets available
- \<data store url\>/oaipmh/?verb=ListRecords&resumptionToken=\<resumption token\> - returns another 100 of data set records
- \<data store url\>/oaipmh/?verb=GetRecord&metadataPrefix=testPrefix&identifier=\<data set code\> - returns a record for a data set with the specified code

#### ch.systemsx.cisd.openbis.dss.screening.server.oaipmh.ScreeningJythonBasedRequestHandler

Screening version of
ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.JythonBasedRequestHandler.
It works exactly the same as the generic counterpart, but it defines an
additional variable that is available in the script:

-   screeningFacade
    - ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade
