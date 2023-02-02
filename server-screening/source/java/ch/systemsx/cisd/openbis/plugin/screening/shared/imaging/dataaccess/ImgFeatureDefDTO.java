/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import java.io.Serializable;

import net.lemnik.eodsql.ResultColumn;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;

/**
 * Corresponds to a row in the FEATURE_DEFS table. Name and description should be filled out, but if the object has not yet been persisted, id and
 * dataSetId may be invalid.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImgFeatureDefDTO extends AbstractImgIdentifiable implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ResultColumn("LABEL")
    private String label;

    @ResultColumn("CODE")
    private String code;

    @ResultColumn("DESCRIPTION")
    private String description;

    @ResultColumn("DS_ID")
    private long dataSetId;

    public ImgFeatureDefDTO()
    {
    }

    public ImgFeatureDefDTO(String label, String code, String description, long dataSetId)
    {
        setCode(code);
        this.label = label;
        this.description = description;
        this.dataSetId = dataSetId;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setCode(String code)
    {
        this.code = CodeNormalizer.normalize(code.toUpperCase());
    }

    public String getCode()
    {
        return code;
    }

    public long getDataSetId()
    {
        return dataSetId;
    }

    public void setDataSetId(long dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

}
