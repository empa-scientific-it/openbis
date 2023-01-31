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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetPredicate;

/**
 * Interface for objects that can function as guardClasses in an AuthorizationGuard.
 * <p>
 * Predicates should return Status.OK if the user is authorized for the action; they should return a status with an appropriate error message if the
 * user is not authorized.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IAuthorizationGuardPredicate<T /* Receiver */, D /* Argument */> extends IDataSetPredicate<D>
{

    /**
     * Evaluate the predicate for the receiver object, sessionToken, and predicate argument.
     * 
     * @param receiver The object on which the guarded method was called
     * @param sessionToken A token identifying the user
     * @param argument The argument to the predicate
     * @return Status.OK if the action is allowed, Status.createError(<a message>) otherwise.
     */
    public Status evaluate(T receiver, String sessionToken, D argument) throws UserFailureException;
}
