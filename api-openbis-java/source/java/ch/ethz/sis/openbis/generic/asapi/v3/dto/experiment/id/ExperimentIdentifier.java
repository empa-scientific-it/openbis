/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectIdentifier;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Experiment identifier.
 * 
 * @author pkupczyk
 */
@JsonObject("as.dto.experiment.id.ExperimentIdentifier")
public class ExperimentIdentifier extends ObjectIdentifier implements IExperimentId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param identifier Experiment identifier, e.g. "/MY_SPACE/MY_PROJECT/MY_EXPERIMENT".
     */
    public ExperimentIdentifier(String identifier)
    {
        super(identifier != null ? identifier.toUpperCase() : null);
    }

    public ExperimentIdentifier(String spaceCode, String projectCode, String experimentCode)
    {
        this("/" + spaceCode + "/" + projectCode + "/" + experimentCode);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private ExperimentIdentifier()
    {
        super();
    }

}
