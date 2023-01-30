/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicDataSetUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Piotr Buczek
 */
public class DataSetUpdates extends BasicDataSetUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String sampleIdentifierOrNull;

    private String experimentIdentifierOrNull;

    public DataSetUpdates()
    {
    }

    public String getSampleIdentifierOrNull()
    {
        return sampleIdentifierOrNull;
    }

    public void setSampleIdentifierOrNull(String sampleIdentifierOrNull)
    {
        this.sampleIdentifierOrNull = sampleIdentifierOrNull;
    }

    public String getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public void setExperimentIdentifierOrNull(String experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

}
