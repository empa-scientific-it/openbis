/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.api.v1;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;

/**
 * @author pkupczyk
 */
public class PutDataSetUtil
{

    public static void checkAccess(String sessionToken, IEncapsulatedOpenBISService service, NewDataSetDTO newDataSet, boolean noOwnerAllowed)
    {
        if (newDataSet == null)
        {
            throw new UserFailureException("New data set cannot be null");
        }

        DataSetOwner owner = newDataSet.getDataSetOwner();

        if (owner == null)
        {
            if (noOwnerAllowed)
            {
                return;
            }
            throw new UserFailureException("Owner of a new data set cannot be null");
        }

        String ownerIdentifier = owner.getIdentifier();

        if (ownerIdentifier == null)
        {
            throw new UserFailureException("Owner identifier of a new data set cannot be null");
        }

        switch (owner.getType())
        {
            case EXPERIMENT:
                service.checkExperimentAccess(sessionToken, ownerIdentifier);
                break;
            case SAMPLE:
                service.checkSampleAccess(sessionToken, ownerIdentifier);
                break;
            case DATA_SET:
                service.checkDataSetAccess(sessionToken, ownerIdentifier);
                break;
        }
    }

}
