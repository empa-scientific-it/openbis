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

package ch.systemsx.cisd.etlserver.registrator.v1;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A factory for creating DataSetRegistrationDetails objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetRegistrationDetailsFactory<T extends DataSetInformation>
{
    /**
     * Create an object that contains information about the data set to be registered.
     */
    DataSetRegistrationDetails<T> createDataSetRegistrationDetails();

    /**
     * Create a representation of the data set using the registration details.
     */
    DataSet<T> createDataSet(DataSetRegistrationDetails<T> registrationDetails, File stagingFile);

    /**
     * Get the user associated with this registration if there is one.
     */
    String getUserIdOrNull();
}
