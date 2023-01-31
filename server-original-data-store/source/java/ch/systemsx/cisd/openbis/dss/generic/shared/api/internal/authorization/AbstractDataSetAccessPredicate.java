/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationGuardPredicate;

/**
 * Abstract super class of all implementations checking data set access.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataSetAccessPredicate<T, D> implements IAuthorizationGuardPredicate<T, D>
{

    @Override
    public Status evaluate(T receiver, String sessionToken, D argument) throws UserFailureException
    {
        List<String> dataSetCodes = getDataSetCodes(argument);
        return DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(sessionToken,
                dataSetCodes);
    }

}
