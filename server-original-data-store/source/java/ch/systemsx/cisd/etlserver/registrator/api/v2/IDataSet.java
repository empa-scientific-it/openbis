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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSet extends IDataSetUpdatable
{

    /**
     * Set whether the data is measured or not.
     */
    public void setMeasuredData(boolean measuredData);

    /**
     * Set the data set type.
     */
    public void setDataSetType(String dataSetTypeCode);

    public void setDataSetKind(DataSetKind dataSetKind);

    /**
     * Sets the speed hint for the data set. The speed hint is a negative or positive number with an absolute value less than or equal
     * {@link Constants#MAX_SPEED}.
     * <p>
     * A positive value means that the data set should be stored in a storage with speed &gt;= <code>speedHint</code>. A negative value means that the
     * data set should be stored in a storage with speed &lt;= <code>abs(speedHint)</code>. The speed hint might be ignored.
     * <p>
     * If no speed hint has been set the default value {@link Constants#DEFAULT_SPEED_HINT} is assumed.
     * <p>
     * This property is undefined for container data sets.
     */
    public void setSpeedHint(int speedHint);

}
