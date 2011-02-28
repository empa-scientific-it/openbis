/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.shared.api.authorization.internal;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IAuthorizationGuardPredicate;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;

/**
 * A predicate for testing a single data set identifier.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 *
 * @author Franz-Josef Elmer
 */
public class SingleDataSetIdentifierPredicate implements
        IAuthorizationGuardPredicate<IDssServiceRpcScreening, IDatasetIdentifier>

{
    public Status evaluate(IDssServiceRpcScreening receiver, String sessionToken,
            IDatasetIdentifier datasetIdentifier) throws UserFailureException
    {
        return DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(
                sessionToken, datasetIdentifier.getDatasetCode());
    }

}
