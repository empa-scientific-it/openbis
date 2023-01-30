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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * Builder class for creating an instance of {@link PhysicalDataSet} or {@link ContainerDataSet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetBuilder extends AbstractDataSetBuilder<DataSetBuilder>
{
    public DataSetBuilder()
    {
        super(new PhysicalDataSet());
        dataSet.tryGetAsDataSet().setLocatorType(
                new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
    }

    public DataSetBuilder(long id)
    {
        this();
        dataSet.setId(id);
    }

    public DataSetBuilder location(String location)
    {
        dataSet.tryGetAsDataSet().setLocation(location);
        return this;
    }

    public DataSetBuilder shareID(String shareID)
    {
        dataSet.tryGetAsDataSet().setShareId(shareID);
        return this;
    }

    public DataSetBuilder fileFormat(String fileFormatType)
    {
        dataSet.tryGetAsDataSet().setFileFormatType(new FileFormatType(fileFormatType));
        return this;
    }

    public DataSetBuilder status(DataSetArchivingStatus status)
    {
        dataSet.tryGetAsDataSet().setStatus(status);
        return this;
    }

    public final PhysicalDataSet getDataSet()
    {
        return dataSet.tryGetAsDataSet();
    }

    @Override
    protected DataSetBuilder asConcreteSubclass()
    {
        return this;
    }
}
