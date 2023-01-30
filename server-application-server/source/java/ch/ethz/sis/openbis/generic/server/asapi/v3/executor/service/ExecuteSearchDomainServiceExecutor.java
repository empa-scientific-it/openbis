/*
 * Copyright 2018 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.IObjectAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISearchDomainSearcher;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.AbstractEntitySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.AlignmentMatch;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.BlastScore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertyBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultScore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchDomainSearchResultWithFullEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class ExecuteSearchDomainServiceExecutor implements IExecuteSearchDomainServiceExecutor
{
    private static final IObjectAuthorizationValidator ALL_VALIDATOR = new IObjectAuthorizationValidator()
        {
            @Override
            public Set<Long> validate(PersonPE person, Collection<Long> objectIds)
            {
                return new TreeSet<>(objectIds);
            }
        };

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IExperimentAuthorizationValidator experimentValidator;

    @Autowired
    private ISampleAuthorizationValidator sampleValidator;

    @Autowired
    private IDataSetAuthorizationValidator dataSetValidator;

    @Autowired
    private ISearchDomainServiceAuthorizationExecutor authorizationExecutor;

    @Override
    public SearchResult<SearchDomainServiceExecutionResult> execute(IOperationContext context, SearchDomainServiceExecutionOptions options)
    {
        authorizationExecutor.canExecute(context);
        String preferredSearchDomain = options.getPreferredSearchDomain();
        String searchString = options.getSearchString();
        Map<String, String> parameters = options.getParameters();
        List<SearchDomainServiceExecutionResult> serviceResults = new ArrayList<>();
        ISearchDomainSearcher searcher = businessObjectFactory.createSearchDomainSearcher(context.getSession());
        List<SearchDomainSearchResultWithFullEntity> results =
                searcher.searchForEntitiesWithSequences(preferredSearchDomain, searchString, parameters);
        List<SearchDomainSearchResultWithFullEntity> filteredResults = filterResultByValidation(context, results);
        for (SearchDomainSearchResultWithFullEntity result : filteredResults)
        {
            SearchDomainSearchResult searchResult = result.getSearchResult();
            IEntityInformationHolderWithIdentifier entity = result.getEntity();
            SearchDomainServiceExecutionResult serviceResult = new SearchDomainServiceExecutionResult();
            String dataStoreCode = searchResult.getSearchDomain().getDataStoreCode();
            String name = searchResult.getSearchDomain().getName();
            serviceResult.setServicePermId(new DssServicePermId(name, new DataStorePermId(dataStoreCode)));
            serviceResult.setSearchDomainName(name);
            serviceResult.setSearchDomainLabel(searchResult.getSearchDomain().getLabel());
            if (entity != null)
            {
                serviceResult.setEntityKind(EntityKindConverter.convert(entity.getEntityKind()));
                serviceResult.setEntityType(entity.getEntityType().getCode());
                serviceResult.setEntityPermId(entity.getPermId());
                serviceResult.setEntityIdentifier(entity.getIdentifier());
            }
            Map<String, String> resultDetails = new TreeMap<>();
            serviceResult.setResultDetails(resultDetails);
            fillResultDetailsWithLocation(resultDetails, searchResult.getResultLocation());
            fillResultDetailsWithScore(resultDetails, searchResult.getScore());
            serviceResults.add(serviceResult);
        }
        return new SearchResult<SearchDomainServiceExecutionResult>(serviceResults, serviceResults.size());
    }

    private void fillResultDetailsWithLocation(Map<String, String> resultDetails, ISearchDomainResultLocation resultLocation)
    {
        if (resultLocation instanceof AbstractEntitySearchResultLocation)
        {
            AbstractEntitySearchResultLocation entityLocation = (AbstractEntitySearchResultLocation) resultLocation;
            addInteger(resultDetails, "position", entityLocation.getPosition());
        }
        if (resultLocation instanceof DataSetFileSearchResultLocation)
        {
            DataSetFileSearchResultLocation dsf = (DataSetFileSearchResultLocation) resultLocation;
            resultDetails.put("path_in_data_set", dsf.getPathInDataSet());
            resultDetails.put("identifier", dsf.getIdentifier());
        }
        if (resultLocation instanceof DataSetFileBlastSearchResultLocation)
        {
            fillResultDetailsWithAlignmentMatch(resultDetails,
                    ((DataSetFileBlastSearchResultLocation) resultLocation).getAlignmentMatch());
        }
        if (resultLocation instanceof EntityPropertySearchResultLocation)
        {
            EntityPropertySearchResultLocation epr = (EntityPropertySearchResultLocation) resultLocation;
            resultDetails.put("property_type", epr.getPropertyType());
        }
        if (resultLocation instanceof EntityPropertyBlastSearchResultLocation)
        {
            fillResultDetailsWithAlignmentMatch(resultDetails,
                    ((EntityPropertyBlastSearchResultLocation) resultLocation).getAlignmentMatch());
        }
    }

    private void fillResultDetailsWithAlignmentMatch(Map<String, String> resultDetails, AlignmentMatch alignmentMatch)
    {
        if (alignmentMatch != null)
        {
            addInteger(resultDetails, "number_of_mismatches", alignmentMatch.getNumberOfMismatches());
            addInteger(resultDetails, "query_start", alignmentMatch.getQueryStart());
            addInteger(resultDetails, "query_end", alignmentMatch.getQueryEnd());
            addInteger(resultDetails, "sequence_start", alignmentMatch.getSequenceStart());
            addInteger(resultDetails, "sequence_end", alignmentMatch.getSequenceEnd());
            addInteger(resultDetails, "total_number_of_gaps", alignmentMatch.getTotalNumberOfGaps());
        }
    }

    private void fillResultDetailsWithScore(Map<String, String> resultDetails, ISearchDomainResultScore resultScore)
    {
        if (resultScore instanceof BlastScore)
        {
            BlastScore blast = (BlastScore) resultScore;
            addDouble(resultDetails, "score", blast.getScore());
            addDouble(resultDetails, "bit_score", blast.getBitScore());
            addDouble(resultDetails, "e_value", blast.getEvalue());
        }
    }

    private void addInteger(Map<String, String> resultDetails, String name, int number)
    {
        resultDetails.put(name, String.valueOf(number));
    }

    private void addDouble(Map<String, String> resultDetails, String name, double number)
    {
        resultDetails.put(name, String.valueOf(number));
    }
    
    private List<SearchDomainSearchResultWithFullEntity> filterResultByValidation(IOperationContext context,
            List<SearchDomainSearchResultWithFullEntity> results)
    {
        List<SearchDomainSearchResultWithFullEntity> filteredResults = new ArrayList<>();
        EnumMap<EntityKind, Map<Long, List<SearchDomainSearchResultWithFullEntity>>> resultsByEntityKind = sortByEntityKind(results);
        for (Entry<EntityKind, Map<Long, List<SearchDomainSearchResultWithFullEntity>>> entry : resultsByEntityKind.entrySet())
        {
            EntityKind entityKind = entry.getKey();
            Map<Long, List<SearchDomainSearchResultWithFullEntity>> resultsByEntityIds = entry.getValue();
            Set<Long> ids = resultsByEntityIds.keySet();
            IObjectAuthorizationValidator validator = getValidator(entityKind);
            Set<Long> filteredIds = validator.validate(context.getSession().tryGetPerson(), ids);
            for (Long filteredId : filteredIds)
            {
                filteredResults.addAll(resultsByEntityIds.get(filteredId));
            }
        }
        return filteredResults;
    }

    private EnumMap<EntityKind, Map<Long, List<SearchDomainSearchResultWithFullEntity>>> sortByEntityKind(
            List<SearchDomainSearchResultWithFullEntity> results)
    {
        EnumMap<EntityKind, Map<Long, List<SearchDomainSearchResultWithFullEntity>>> resultsByEntityKind =
                new EnumMap<>(EntityKind.class);
        EntityKind[] values = EntityKind.values();
        for (EntityKind entityKind : values)
        {
            resultsByEntityKind.put(entityKind, new TreeMap<>());
        }
        for (SearchDomainSearchResultWithFullEntity result : results)
        {
            IEntityInformationHolderWithPermId entity = result.getEntity();
            if (entity != null)
            {
                EntityKind entityKind = entity.getEntityKind();
                Map<Long, List<SearchDomainSearchResultWithFullEntity>> map = resultsByEntityKind.get(entityKind);
                List<SearchDomainSearchResultWithFullEntity> list = map.get(entity.getId());
                if (list == null)
                {
                    list = new ArrayList<>();
                    map.put(entity.getId(), list);
                }
                list.add(result);
            }
        }
        return resultsByEntityKind;
    }

    private IObjectAuthorizationValidator getValidator(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return experimentValidator;
            case DATA_SET:
                return dataSetValidator;
            case SAMPLE:
                return sampleValidator;
            default:
                return ALL_VALIDATOR;
        }
    }

}
