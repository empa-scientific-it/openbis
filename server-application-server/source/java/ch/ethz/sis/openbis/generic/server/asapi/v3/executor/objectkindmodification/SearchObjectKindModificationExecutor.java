/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.objectkindmodification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.OperationKindCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;

/**
 * @author pkupczyk
 */
@Component
public class SearchObjectKindModificationExecutor implements ISearchObjectKindModificationExecutor
{

    @Autowired
    private LastModificationState lastModificationState;

    @Autowired
    private IObjectKindModificationAuthorizationExecutor authorizationExecutor;

    @Override
    public List<ObjectKindModification> search(final IOperationContext context, final ObjectKindModificationSearchCriteria searchCriteria,
            final ObjectKindModificationFetchOptions fetchOptions)
    {
        authorizationExecutor.canSearch(context);
        
        List<ObjectKindModification> result = new ArrayList<>();
        List<ObjectKind> objectKinds = getObjectKinds(searchCriteria);
        List<OperationKind> operationKinds = getOperationKinds(searchCriteria);
        for (ObjectKind objectKind : objectKinds)
        {
            for (OperationKind operationKind : operationKinds)
            {
                ObjectKindModification objectKindModification = new ObjectKindModification();
                objectKindModification.setObjectKind(objectKind);
                objectKindModification.setOperationKind(operationKind);
                Date date = new Date(lastModificationState.getLastModificationTime(translate(objectKind, operationKind)));
                objectKindModification.setLastModificationTimeStamp(date);
                objectKindModification.setFetchOptions(fetchOptions);
                result.add(objectKindModification);
            }
        }
        return result;
    }

    private List<ObjectKind> getObjectKinds(ObjectKindModificationSearchCriteria searchCriteria)
    {
        ObjectKindCriteria lastCriteria = null;

        for (ISearchCriteria subCriteria : searchCriteria.getCriteria())
        {
            if (subCriteria instanceof ObjectKindCriteria)
            {
                lastCriteria = (ObjectKindCriteria) subCriteria;
            }
        }

        if (lastCriteria == null || lastCriteria.getObjectKinds() == null || lastCriteria.getObjectKinds().isEmpty())
        {
            return Arrays.asList(ObjectKind.values());
        } else
        {
            return lastCriteria.getObjectKinds();
        }
    }

    private List<OperationKind> getOperationKinds(ObjectKindModificationSearchCriteria searchCriteria)
    {
        OperationKindCriteria lastCriteria = null;

        for (ISearchCriteria subCriteria : searchCriteria.getCriteria())
        {
            if (subCriteria instanceof OperationKindCriteria)
            {
                lastCriteria = (OperationKindCriteria) subCriteria;
            }
        }

        if (lastCriteria == null || lastCriteria.getOperationKinds() == null || lastCriteria.getOperationKinds().isEmpty())
        {
            return Arrays.asList(OperationKind.values());
        } else
        {
            return lastCriteria.getOperationKinds();
        }
    }

    private DatabaseModificationKind translate(ObjectKind objectKind, OperationKind operationKind)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind translatedObjectKind =
                DatabaseModificationKind.ObjectKind.valueOf(objectKind.name());
        switch (operationKind)
        {
            case CREATE_OR_DELETE:
                return DatabaseModificationKind.createOrDelete(translatedObjectKind);
            case UPDATE:
                return DatabaseModificationKind.edit(translatedObjectKind);
        }
        return null;
    }

}
