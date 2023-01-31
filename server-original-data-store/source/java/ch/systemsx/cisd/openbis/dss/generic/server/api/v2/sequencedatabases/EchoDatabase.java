/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.AlignmentMatch;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileBlastSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityPropertySearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ISearchDomainResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

/**
 * A test database that returns a search result that was stored in the parameters map under a key equal to the searched sequence snippet.
 * 
 * @author pkupczyk
 */
public class EchoDatabase extends AbstractSearchDomainService
{
    public static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EchoDatabase.class);

    private boolean available;

    private List<SearchDomainSearchOption> searchOptions = new ArrayList<>();

    public EchoDatabase(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        available = PropertyUtils.getBoolean(properties, "available", true);
        List<String> optionCodes = PropertyUtils.getList(properties, "possible-search-options");
        for (String optionCode : optionCodes)
        {
            SearchDomainSearchOption option = new SearchDomainSearchOption();
            option.setCode(optionCode);
            option.setLabel(properties.getProperty(optionCode + ".label", optionCode));
            option.setDescription(properties.getProperty(optionCode + ".description", ""));
            searchOptions.add(option);
        }
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }
    
    @Override
    public String getPossibleSearchOptionsKey()
    {
        return searchOptions.isEmpty() ? null : "optionsKey";
    }

    @Override
    public List<SearchDomainSearchOption> getPossibleSearchOptions()
    {
        return searchOptions;
    }

    @Override
    public List<SearchDomainSearchResult> search(String sequenceSnippet, Map<String, String> optionalParametersOrNull)
    {
        String resultStr = optionalParametersOrNull.get(sequenceSnippet);
        if (resultStr != null)
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                HelperBean bean = mapper.readValue(resultStr, HelperBean.class);
                ISearchDomainResultLocation resultLocation = bean.getResultLocation();
                SearchDomainSearchResult result = new SearchDomainSearchResult();
                result.setSearchDomain(bean.getSearchDomain());
                result.setResultLocation(resultLocation);
                return Collections.singletonList(result);
            } catch (Exception e)
            {
                operationLog.error("Couldn't create result", e);
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
        } else
        {
            return Collections.emptyList();
        }
    }

    public static final class HelperBean
    {
        private SearchDomain searchDomain = new SearchDomain();

        private DataSetFileBlastSearchResultLocation dataSetFileBlastResultLocation;

        private EntityPropertySearchResultLocation entityPropertyResultLocation;

        public ISearchDomainResultLocation getResultLocation()
        {
            return dataSetFileBlastResultLocation == null ? entityPropertyResultLocation : dataSetFileBlastResultLocation;
        }

        public SearchDomain getSearchDomain()
        {
            return searchDomain;
        }

        public void setSearchDomain(String searchDomain)
        {
            this.searchDomain.setName(searchDomain);
        }

        public void setDataSetCode(String dataSetCode)
        {
            getDataSetFileBlastResultLocation().setCode(dataSetCode);
            getDataSetFileBlastResultLocation().setPermId(dataSetCode);
        }

        public void setPathInDataSet(String path)
        {
            getDataSetFileBlastResultLocation().setPathInDataSet(path);
        }

        public void setSequenceIdentifier(String identifier)
        {
            getDataSetFileBlastResultLocation().setIdentifier(identifier);
        }

        public void setPositionInSequence(int position)
        {
            getDataSetFileBlastResultLocation().setPosition(position);
        }

        public void setAlignmentMatchSequenceStart(int pos)
        {
            getAlignmentMatch().setSequenceStart(pos);
        }

        public void setAlignmentMatchSequenceEnd(int pos)
        {
            getAlignmentMatch().setSequenceEnd(pos);
        }

        public void setAlignmentMatchQueryStart(int pos)
        {
            getAlignmentMatch().setQueryStart(pos);
        }
        
        public void setAlignmentMatchQueryEnd(int pos)
        {
            getAlignmentMatch().setQueryEnd(pos);
        }
        
        public void setAlignmentMatchMismatches(int n)
        {
            getAlignmentMatch().setNumberOfMismatches(n);
        }
        
        public void setAlignmentMatchGaps(int n)
        {
            getAlignmentMatch().setTotalNumberOfGaps(n);
        }
        
        public void setEntityKind(EntityKind entityKind)
        {
            getEntityPropertyResultLocation().setEntityKind(entityKind);
        }

        public void setEntityType(String entityType)
        {
            getEntityPropertyResultLocation().setEntityType(entityType);
        }

        public void setPermId(String permId)
        {
            getEntityPropertyResultLocation().setPermId(permId);
        }

        public void setCode(String code)
        {
            getEntityPropertyResultLocation().setCode(code);
        }

        public void setPropertyType(String propertyType)
        {
            getEntityPropertyResultLocation().setPropertyType(propertyType);
        }

        private DataSetFileBlastSearchResultLocation getDataSetFileBlastResultLocation()
        {
            if (dataSetFileBlastResultLocation == null)
            {
                dataSetFileBlastResultLocation = new DataSetFileBlastSearchResultLocation();
            }
            return dataSetFileBlastResultLocation;
        }

        private AlignmentMatch getAlignmentMatch()
        {
            AlignmentMatch alignmentMatch = getDataSetFileBlastResultLocation().getAlignmentMatch();
            if (alignmentMatch == null)
            {
                alignmentMatch = new AlignmentMatch();
                getDataSetFileBlastResultLocation().setAlignmentMatch(alignmentMatch);
            }
            return alignmentMatch;
        }

        private EntityPropertySearchResultLocation getEntityPropertyResultLocation()
        {
            if (entityPropertyResultLocation == null)
            {
                entityPropertyResultLocation = new EntityPropertySearchResultLocation();
            }
            return entityPropertyResultLocation;
        }
    }
}
