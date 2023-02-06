/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDataSetCodesHolder;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.execute.ProcessingServiceExecutionOptions")
public class ProcessingServiceExecutionOptions
        extends AbstractExecutionOptionsWithParameters<ProcessingServiceExecutionOptions, String>
        implements IDataSetCodesHolder
{
    private static final long serialVersionUID = 1L;

    private List<String> dataSetCodes = new ArrayList<>();

    public ProcessingServiceExecutionOptions withDataSets(String... dataSetCodes)
    {
        return withDataSets(Arrays.asList(dataSetCodes));
    }

    public ProcessingServiceExecutionOptions withDataSets(List<String> dataSetCodes)
    {
        this.dataSetCodes.addAll(dataSetCodes);
        return this;
    }

    @Override
    public List<String> getDataSetCodes()
    {
        return dataSetCodes;
    }

    @Override
    public String toString()
    {
        return super.toString() + ", data sets=" + dataSetCodes;
    }

}
