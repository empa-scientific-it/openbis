#! /usr/bin/env python
from java.util import Date
from java.text import SimpleDateFormat
from xml.etree import ElementTree
from xml.etree.ElementTree import Element, SubElement
from org.dspace.xoai.dataprovider import DataProvider
from org.dspace.xoai.dataprovider.model import Context, MetadataFormat, Item
from org.dspace.xoai.dataprovider.repository import Repository, RepositoryConfiguration
from org.dspace.xoai.dataprovider.parameters import OAIRequest
from org.dspace.xoai.dataprovider.handlers.results import ListItemIdentifiersResult, ListItemsResults
from org.dspace.xoai.model.oaipmh import OAIPMH, DeletedRecord, Granularity, Metadata
from org.dspace.xoai.xml import XmlWriter
from ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai import SimpleItemIdentifier, SimpleItem, SimpleItemRepository, SimpleSetRepository
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute, MatchClauseTimeAttribute, CompareMode
DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
TIME_ZONE = "0"
 
def handle(req, resp):
    context = Context();
    context.withMetadataFormat(MetadataFormat().withPrefix("testPrefix").withTransformer(MetadataFormat.identity()));
    configuration = RepositoryConfiguration();
    configuration.withMaxListSets(10);
    configuration.withMaxListIdentifiers(10);
    configuration.withMaxListRecords(10);
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