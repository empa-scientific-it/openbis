/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListTechIdById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.TechIdStringIdentifierRecord;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
public class ListSampleTechIdByIdentifier extends AbstractListTechIdById<SampleIdentifier>
{
    public static final String CONTAINER_SHORTCUT_ALLOWED_ATTRIBUTE = "container-shortcut-allowed";

    private String homeSpaceCodeOrNull;

    public ListSampleTechIdByIdentifier(String homeSpaceCodeOrNull)
    {
        this.homeSpaceCodeOrNull = homeSpaceCodeOrNull;
    }

    @Override
    public Class<SampleIdentifier> getIdClass()
    {
        return SampleIdentifier.class;
    }

    @Override
    protected Map<Long, SampleIdentifier> createIdsByTechIdsMap(IOperationContext context, List<SampleIdentifier> ids)
    {
        Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupedIdentifiers = groupIdentifiers(ids);
        boolean containerShortcutAllowed = getContainerShortcutAllowed(context);
        Map<Long, SampleIdentifier> result = new HashMap<>();
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        for (Entry<SampleIdentifierParts, Map<String, SampleIdentifier>> entry : groupedIdentifiers.entrySet())
        {
            SampleIdentifierParts key = entry.getKey();
            Map<String, SampleIdentifier> identifiersByCode = entry.getValue();
            List<TechIdStringIdentifierRecord> records = list(query, key, identifiersByCode.keySet(), containerShortcutAllowed);
            for (TechIdStringIdentifierRecord record : records)
            {
                String sampleCode = record.identifier;
                result.put(record.id, identifiersByCode.get(sampleCode));
            }
        }
        return result;
    }

    private boolean getContainerShortcutAllowed(IOperationContext context)
    {
        Object value = context.getAttribute(CONTAINER_SHORTCUT_ALLOWED_ATTRIBUTE);
        return Boolean.TRUE.equals(value);
    }

    private Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupIdentifiers(List<SampleIdentifier> ids)
    {
        Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupedIdentifiers = new HashMap<>();
        for (SampleIdentifier sampleIdentifier : ids)
        {
            FullSampleIdentifier fullSampleIdentifier = new FullSampleIdentifier(sampleIdentifier.getIdentifier(),
                    homeSpaceCodeOrNull);

            SampleIdentifierParts key = fullSampleIdentifier.getParts();
            Map<String, SampleIdentifier> identifiersByCode = groupedIdentifiers.get(key);
            if (identifiersByCode == null)
            {
                identifiersByCode = new HashMap<>();
                groupedIdentifiers.put(key, identifiersByCode);
            }
            identifiersByCode.put(fullSampleIdentifier.getSampleCode(), sampleIdentifier);
        }
        return groupedIdentifiers;
    }

    private List<TechIdStringIdentifierRecord> list(final SampleQuery query, final SampleIdentifierParts key,
            final Collection<String> codes, boolean containerShortcutAllowed)
    {
        final String[] codesArray = codes.toArray(new String[codes.size()]);
        final String spaceCode = key.getSpaceCodeOrNull();
        final String projectCode = key.getProjectCodeOrNull();
        final String containerCode = key.getContainerCodeOrNull();

        if (spaceCode == null)
        {
            if (containerCode == null)
            {
                return listWithoutContainerOrWithSomeContainerAndUniqueCode(query, codesArray, new IListAction()
                    {
                        @Override
                        public List<TechIdStringIdentifierRecord> list(String[] codesToList)
                        {
                            return query.listSharedSampleTechIdsByCodesWithoutContainer(codesToList);
                        }
                    }, new IListAction()
                        {
                            @Override
                            public List<TechIdStringIdentifierRecord> list(String[] codesToList)
                            {
                                return query.listSharedSampleTechIdsByCodesWithSomeContainer(codesToList);
                            }
                        }, containerShortcutAllowed);
            }
            return query.listSharedSampleTechIdsByContainerCodeAndCodes(containerCode, codesArray);
        }
        if (projectCode == null)
        {
            if (containerCode == null)
            {
                return listWithoutContainerOrWithSomeContainerAndUniqueCode(query, codesArray, new IListAction()
                    {
                        @Override
                        public List<TechIdStringIdentifierRecord> list(String[] codesToList)
                        {
                            return query.listSpaceSampleTechIdsByCodesWithoutContainer(spaceCode, codesToList);
                        }
                    }, new IListAction()
                        {
                            @Override
                            public List<TechIdStringIdentifierRecord> list(String[] codesToList)
                            {
                                return query.listSpaceSampleTechIdsByCodesWithSomeContainer(spaceCode, codesToList);
                            }
                        }, containerShortcutAllowed);
            }
            return query.listSpaceSampleTechIdsByContainerCodeAndCodes(spaceCode, containerCode, codesArray);
        }
        if (containerCode == null)
        {
            return listWithoutContainerOrWithSomeContainerAndUniqueCode(query, codesArray, new IListAction()
                {
                    @Override
                    public List<TechIdStringIdentifierRecord> list(String[] codesToList)
                    {
                        return query.listProjectSampleTechIdsByCodesWithoutContainer(spaceCode, projectCode, codesToList);
                    }
                }, new IListAction()
                    {
                        @Override
                        public List<TechIdStringIdentifierRecord> list(String[] codesToList)
                        {
                            return query.listProjectSampleTechIdsByCodesWithSomeContainer(spaceCode, projectCode, codesToList);
                        }
                    }, containerShortcutAllowed);
        }
        return query.listProjectSampleTechIdsByContainerCodeAndCodes(spaceCode, projectCode, containerCode, codesArray);
    }

    private List<TechIdStringIdentifierRecord> listWithoutContainerOrWithSomeContainerAndUniqueCode(SampleQuery query,
            String[] codes, IListAction listWithoutContainer, IListAction listWithSomeContainer,
            boolean containerShortcutAllowed)
    {
        try
        {
            List<TechIdStringIdentifierRecord> foundWithoutContainer = listWithoutContainer.list(codes);

            Set<String> codesNotFound = new HashSet<String>(Arrays.asList(codes));
            for (TechIdStringIdentifierRecord found : foundWithoutContainer)
            {
                codesNotFound.remove(found.identifier);
            }

            if (codesNotFound.isEmpty() || containerShortcutAllowed == false)
            {
                return foundWithoutContainer;
            }

            List<TechIdStringIdentifierRecord> allFound = new LinkedList<TechIdStringIdentifierRecord>(foundWithoutContainer);

            List<TechIdStringIdentifierRecord> foundWithSomeContainer = listWithSomeContainer.list(codesNotFound.toArray(new String[] {}));
            Map<String, TechIdStringIdentifierRecord> foundWithSomeContainerMap = new HashMap<String, TechIdStringIdentifierRecord>();
            Map<String, Integer> foundWithSomeContainerCounter = new HashMap<String, Integer>();

            for (TechIdStringIdentifierRecord found : foundWithSomeContainer)
            {
                foundWithSomeContainerMap.put(found.identifier, found);

                Integer counter = foundWithSomeContainerCounter.get(found.identifier);
                counter = counter == null ? 1 : counter + 1;
                foundWithSomeContainerCounter.put(found.identifier, counter);
            }

            for (Map.Entry<String, Integer> counterEntry : foundWithSomeContainerCounter.entrySet())
            {
                if (counterEntry.getValue() == 1)
                {
                    TechIdStringIdentifierRecord found = foundWithSomeContainerMap.get(counterEntry.getKey());
                    allFound.add(found);
                }
            }

            return allFound;

        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private static interface IListAction
    {
        public List<TechIdStringIdentifierRecord> list(String[] codesToList);
    }

}
