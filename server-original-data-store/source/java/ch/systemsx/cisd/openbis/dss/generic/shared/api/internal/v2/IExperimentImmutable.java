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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

import java.util.Date;

/**
 * Read-only interface to an existing experiment.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IExperimentImmutable extends IMetaprojectContent
{
    /**
     * Return the experiment identifier of this experiment.
     */
    String getExperimentIdentifier();

    /**
     * Return true if the experiment is in openBIS.
     */
    boolean isExistingExperiment();

    /**
     * Return the type for this experiment. May be null.
     */
    String getExperimentType();

    /**
     * Return the value of a property specified by a code. May return null of no such property with code <code>propertyCode</code> is found.
     */
    String getPropertyValue(String propertyCode);

    /**
     * Returns the permId of this experiment.
     */
    String getPermId();

    /**
     * Returns the registration date of this experiment.
     */
    Date getRegistrationDate();

    /**
     * Returns the modification date of this experiment.
     */
    Date getModificationDate();
}
