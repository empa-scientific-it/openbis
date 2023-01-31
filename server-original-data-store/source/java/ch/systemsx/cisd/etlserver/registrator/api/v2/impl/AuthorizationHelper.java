/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.action.IMapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * @author Jakub Straszewski
 */
public class AuthorizationHelper
{
    public static enum EntityKind
    {
        EXPERIMENT()
        {
            @Override
            List<String> filterToVisible(IEncapsulatedOpenBISService openBisService, String user,
                    List<String> codes)
            {
                return openBisService.filterToVisibleExperiments(user, codes);
            }
        },
        SAMPLE
        {
            @Override
            List<String> filterToVisible(IEncapsulatedOpenBISService openBisService, String user,
                    List<String> codes)
            {
                return openBisService.filterToVisibleSamples(user, codes);
            }
        },
        DATA_SET
        {
            @Override
            List<String> filterToVisible(IEncapsulatedOpenBISService openBisService, String user,
                    List<String> codes)
            {
                return openBisService.filterToVisibleDataSets(user, codes);
            }
        };
        abstract List<String> filterToVisible(IEncapsulatedOpenBISService openBisService,
                String user, List<String> codes);
    }

    public static <T> List<T> filterToVisible(IEncapsulatedOpenBISService openBisService,
            String user, List<T> entities,
            IMapper<T, String> codeMapper, EntityKind entityKind)
    {
        // create a list of codes
        List<String> codes = new LinkedList<String>();
        for (T entity : entities)
        {
            codes.add(codeMapper.map(entity));
        }

        // call service - to filter the codes
        List<String> filteredCodes = entityKind.filterToVisible(openBisService, user, codes);
        // put filtered codes to the set
        Set<String> filteredSet = new HashSet<String>(filteredCodes);

        // filter original values to those returned by the service call
        List<T> resultList = new LinkedList<T>();
        for (T entity : entities)
        {
            if (filteredSet.contains(codeMapper.map(entity)))
            {
                resultList.add(entity);
            }
        }
        return resultList;
    }
}
