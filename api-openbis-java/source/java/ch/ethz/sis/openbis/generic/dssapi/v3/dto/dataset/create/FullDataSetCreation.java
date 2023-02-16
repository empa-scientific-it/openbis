/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author anttil
 */
@JsonObject("dss.dto.dataset.create.FullDataSetCreation")
public class FullDataSetCreation implements ICreation
{
    private static final long serialVersionUID = 1L;

    private DataSetCreation metadataCreation;

    private List<DataSetFileCreation> fileMetadata;

    public DataSetCreation getMetadataCreation()
    {
        return metadataCreation;
    }

    public void setMetadataCreation(DataSetCreation metadataCreation)
    {
        this.metadataCreation = metadataCreation;
    }

    public List<DataSetFileCreation> getFileMetadata()
    {
        return fileMetadata;
    }

    public void setFileMetadata(List<DataSetFileCreation> fileMetadata)
    {
        this.fileMetadata = fileMetadata;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("metadataCreation", metadataCreation).append("fileMetadata", fileMetadata).toString();
    }

}
