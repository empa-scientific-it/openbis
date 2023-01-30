/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * A sample to update.
 * 
 * @author Piotr Buczek
 */
public final class UpdatedSample extends NewSample
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SAMPLE_UPDATE_TEMPLATE_COMMENT =
            "# All columns except \"identifier\" can be removed from the file.\n"
                    + "# If a column is removed from the file or a cell in a column is left empty the corresponding values of updated samples will be preserved.\n"
                    + "# To delete a value/connection from openBIS one needs to put \"--DELETE--\"  or \\\"__DELETE__\\\" into the corresponding cell\n"
                    + "# (in particular, a sample can become detached from an experiment, container or all parents this way).\n"
                    + "# Basically the \"identifier\" column should contain sample identifiers, e.g. /SPACE/SAMPLE_1,\n"
                    + "# but for samples from default space (if it was provided in the form) it is enough to put sample codes (e.g. SAMPLE_1) into the column.\n"
                    + "# The \"container\" column (if not removed) should contain sample identifier for the new container of the updated sample, e.g. /SPACE/SAMPLE_1\n"
                    + "# The \"parent\" column (if not removed) should contain comma separated list of sample identifiers, e.g. /SPACE/SAMPLE_1,/SPACE/SAMPLE_2\n"
                    + "# The \"experiment\" column (if not removed) should contain experiment identifier, e.g. /SPACE/PROJECT/EXP_1\n"
                    + "# The \"default_space\" column is optional, it can be used to override home space for the row\n"
                    + "# The \"current_container\" column is optional, it can be used to specify container where the updated sample belongs before the update\n";

    private SampleBatchUpdateDetails batchUpdateDetails;

    public UpdatedSample(NewSample newSample, SampleBatchUpdateDetails batchUpdateDetails)
    {
        super(newSample.getIdentifier(), newSample.getSampleType(), newSample
                .getContainerIdentifier(), newSample.getParentsOrNull(), newSample
                .getExperimentIdentifier(), newSample.getDefaultSpaceIdentifier(), newSample
                .getCurrentContainerIdentifier(), newSample.getProperties(), newSample
                .getAttachments());
        this.batchUpdateDetails = batchUpdateDetails;
    }

    public SampleBatchUpdateDetails getBatchUpdateDetails()
    {
        return batchUpdateDetails;
    }

    public void setBatchUpdateDetails(SampleBatchUpdateDetails batchUpdateDetails)
    {
        this.batchUpdateDetails = batchUpdateDetails;
    }

}
