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

package ch.systemsx.cisd.etlserver.threev;

import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.etlserver.DefaultDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Implementation which assumes that the information can be extracted from the file name. Following information can be extracted:
 * <ul>
 * <li>Sample code
 * <li>Parent data set code
 * <li>Data producer code
 * <li>Data production date
 * <li>Data set code
 * </ul>
 * This class uses the same properties as {@link DefaultDataSetInfoExtractor}. In addition the following properties are used to extract the data set
 * code:
 * <table border="1" cellspacing="0" * cellpadding="5">
 * <tr>
 * <th>Property</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>indices-of-data-set-code-entities</code></td>
 * <td>&nbsp;</td>
 * <td>Space or comma separated list of entity indices which define the data set code uniquely. This is a mandatory property.</td>
 * </tr>
 * <tr>
 * <td><code>data-set-code-entities-glue</code></td>
 * <td><code>.</code></td>
 * <td>Symbol used to concatenate entities defining the data set code.</td>
 * </tr>
 * </table>
 * The first entity has index 0, the second 1, etc. Using negative numbers one can specify entities from the end. Thus, -1 means the last entity, -2
 * the second last entity, etc.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForDataAcquisition extends AbstractDataSetInfoExtractorFor3V
{
    /**
     * Name of the property specifying the indices of those entities which define uniquely the data set code.
     * <p>
     * Use a negative number to count from the end, e.g. <code>-1</code> indicates the last entity.
     * </p>
     */
    @Private
    static final String INDICES_OF_DATA_SET_CODE_ENTITIES = "indices-of-data-set-code-entities";

    public DataSetInfoExtractorForDataAcquisition(final Properties properties)
    {
        super(properties, INDICES_OF_DATA_SET_CODE_ENTITIES);
    }

    @Override
    protected void setCodeFor(final DataSetInformation dataSetInfo, final String code)
    {
        dataSetInfo.setDataSetCode(code);
    }

}
