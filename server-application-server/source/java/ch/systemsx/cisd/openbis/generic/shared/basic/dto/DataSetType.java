/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.util.List;
import java.util.Map;

/**
 * The <i>GWT</i> equivalent to DataSetTypePE.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetType extends EntityType
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<DataSetTypePropertyType> dataSetTypePropertyTypes;

    private String mainDataSetPattern;

    private String mainDataSetPath;

    private boolean deletionDisallow;

    private Map<String, String> metaData;

    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }

    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }

    public DataSetType()
    {
    }

    public DataSetType(final String code)
    {
        setCode(code);
    }

    @Override
    public List<DataSetTypePropertyType> getAssignedPropertyTypes()
    {
        return dataSetTypePropertyTypes;
    }

    public void setDataSetTypePropertyTypes(List<DataSetTypePropertyType> dataSetTypePropertyTypes)
    {
        this.dataSetTypePropertyTypes = dataSetTypePropertyTypes;
    }

    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    public boolean isDeletionDisallow()
    {
        return deletionDisallow;
    }

    public void setDeletionDisallow(boolean deletionDisallow)
    {
        this.deletionDisallow = deletionDisallow;
    }

    @Override
    public boolean isEntityKind(EntityKind kind)
    {
        return kind == null || kind == EntityKind.DATA_SET;
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }
}
