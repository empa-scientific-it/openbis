/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * A role that can perform a self test.
 * 
 * @author Bernd Rinn
 */
public interface ISelfTestable
{

    /**
     * Returns <code>true</code>, if this self-testable performs remote operations (i.e. requires network resources.
     */
    public boolean isRemote();

    /**
     * Checks this <code>ISelfTestable</code>. Implementations are not supposed to do any failure logging (debug logging is OK), but the caller is in
     * charge of this.
     * 
     * @throws ConfigurationFailureException If the self-test fails due to a configuration problem.
     * @throws EnvironmentFailureException If the self-test fails due to a problem in the environment.
     */
    public void check() throws EnvironmentFailureException, ConfigurationFailureException;

}
