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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * @author pkupczyk
 */
public class UpdatedDataSet extends NewDataSet
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String DATASET_UPDATE_TEMPLATE_COMMENT =
            "# All columns except \"code\" can be removed from the file.\n"
                    + "# If a column is removed from the file or a cell in a column is left empty the corresponding values of updated data set will be preserved.\n"
                    + "# To delete a value/connection from openBIS one needs to put \"--DELETE--\"  or \\\"__DELETE__\\\" into the corresponding cell\n"
                    + "# (in particular, a data set can become detached from an experiment, sample, container or all parents this way).\n"
                    + "# Basically the \"code\" column should contain a data set code, e.g. 20100823231225012-79089,\n"
                    + "# The \"container\" column (if not removed) should contain a data set code for the new container of the updated data set, e.g. 20100823231225012-83462\n"
                    + "# The \"parents\" column (if not removed) should contain comma separated list of data set codes, e.g. 20100823231225012-93247,20100823231225012-23877\n"
                    + "# The \"experiment\" column (if not removed) should contain an experiment identifier, e.g. /SPACE/PROJECT/EXP_1\n"
                    + "# The \"sample\" column (if not removed) should contain a sample identifier, e.g. /SPACE/SAMPLE_1\n"
                    + "# The \"file_format\" column (if not removed) should contain a file format code, e.g. XML\n";

    private DataSetBatchUpdateDetails batchUpdateDetails;

    public UpdatedDataSet(NewDataSet newDataSet, DataSetBatchUpdateDetails batchUpdateDetails)
    {
        setCode(newDataSet.getCode());
        setContainerIdentifierOrNull(newDataSet.getContainerIdentifierOrNull());
        setExperimentIdentifier(newDataSet.getExperimentIdentifier());
        setFileFormatOrNull(newDataSet.getFileFormatOrNull());
        setParentsIdentifiersOrNull(newDataSet.getParentsIdentifiersOrNull());
        setProperties(newDataSet.getProperties());
        setSampleIdentifierOrNull(newDataSet.getSampleIdentifierOrNull());
        this.batchUpdateDetails = batchUpdateDetails;
    }

    public DataSetBatchUpdateDetails getBatchUpdateDetails()
    {
        return batchUpdateDetails;
    }

    public void setBatchUpdateDetails(DataSetBatchUpdateDetails batchUpdateDetails)
    {
        this.batchUpdateDetails = batchUpdateDetails;
    }

}
