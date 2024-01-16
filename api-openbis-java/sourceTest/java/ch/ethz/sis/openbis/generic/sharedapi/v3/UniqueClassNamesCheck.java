/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.sharedapi.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class UniqueClassNamesCheck
{

    @Test
    public void testSimpleNamesOfApiClassesAreUnique()
    {
        Map<String, List<Class<?>>> simpleNameToClasses = new HashMap<>();

        for(Class<?> publicClass : ApiClassesProvider.getPublicClasses()){
            List<Class<?>> simpleNameClasses = simpleNameToClasses.computeIfAbsent(publicClass.getSimpleName(), k -> new ArrayList<>());
            simpleNameClasses.add(publicClass);
        }

        Set<Class<?>> allowedDuplicates = new HashSet<>();
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ExternalDmsSearchCriteria.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AbstractExecutionOptionsWithParameters.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.execute.AbstractExecutionOptionsWithParameters.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.dssapi.v3.dto.common.operation.IOperationResult.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSessionNameSearchCriteria.class);
        allowedDuplicates.add(ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.PersonalAccessTokenSessionNameSearchCriteria.class);

        Set<Class<?>> foundDuplicates = new HashSet<>();

        for(List<Class<?>> simpleNameClasses : simpleNameToClasses.values()){
            if(simpleNameClasses.size() > 1){
                foundDuplicates.addAll(simpleNameClasses);
            }
        }

        Assert.assertEquals(foundDuplicates, allowedDuplicates, "Allowed duplicates: " + allowedDuplicates + ", found duplicates: " + foundDuplicates);
    }

}
