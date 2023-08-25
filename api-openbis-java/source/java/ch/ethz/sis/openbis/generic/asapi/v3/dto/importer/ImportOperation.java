/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.importer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.IImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.importer.ImportOperation")
public class ImportOperation implements Serializable, IOperation
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IImportData importData;

    @JsonProperty
    private ImportOptions importOptions;

    @SuppressWarnings("unused")
    public ImportOperation()
    {
    }

    public ImportOperation(final IImportData importData, final ImportOptions importOptions)
    {
        this.importData = importData;
        this.importOptions = importOptions;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @JsonIgnore
    public IImportData getImportData()
    {
        return importData;
    }

    @JsonIgnore
    public void setImportData(final IImportData importData)
    {
        this.importData = importData;
    }

    @JsonIgnore
    public ImportOptions getImportOptions()
    {
        return importOptions;
    }

    @JsonIgnore
    public void setImportOptions(final ImportOptions importOptions)
    {
        this.importOptions = importOptions;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("importData", importData).append("importOptions", importOptions).toString();
    }

}
