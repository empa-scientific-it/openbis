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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import net.lemnik.eodsql.AutoGeneratedKeys;
import net.lemnik.eodsql.ResultColumn;

/**
 * @author Tomasz Pylak
 */
// CREATE TABLE CHANNELS (
// ID BIGSERIAL NOT NULL,
//        
// NAME NAME NOT NULL,
// DESCRIPTION DESCRIPTION,
// WAVELENGTH INTEGER,
//
// DS_ID TECH_ID,
// EXP_ID TECH_ID,
//        
// PRIMARY KEY (ID),
// CONSTRAINT FK_CHANNELS_1 FOREIGN KEY (DS_ID) REFERENCES DATA_SETS (ID) ON DELETE CASCADE ON
// UPDATE CASCADE,
// CONSTRAINT FK_CHANNELS_2 FOREIGN KEY (EXP_ID) REFERENCES EXPERIMENTS (ID) ON DELETE CASCADE ON
// UPDATE CASCADE,
// CONSTRAINT CHANNELS_DS_EXP_ARC_CK CHECK ((DS_ID IS NOT NULL AND EXP_ID IS NULL) OR (DS_ID IS NULL
// AND EXP_ID IS NOT NULL))
// );
public class ImgChannelDTO
{
    @AutoGeneratedKeys
    private long id;

    @ResultColumn("NAME")
    private String name;

    @ResultColumn("DESCRIPTION")
    private String descriptionOrNull;

    @ResultColumn("WAVELENGTH")
    private Integer wavelengthOrNull;

    // can be null if experimentId is not null
    @ResultColumn("DS_ID")
    private Long datasetIdOrNull;

    // can be null if datasetId is not null
    @ResultColumn("EXP_ID")
    private Long experimentIdOrNull;

    public static ImgChannelDTO createDatasetChannel(String name, String descriptionOrNull,
            Integer wavelengthOrNull, long datasetId)
    {
        return new ImgChannelDTO(name, descriptionOrNull, wavelengthOrNull, datasetId, null);
    }

    public static ImgChannelDTO createExperimentChannel(String name, String descriptionOrNull,
            Integer wavelengthOrNull, long experimentId)
    {
        return new ImgChannelDTO(name, descriptionOrNull, wavelengthOrNull, null, experimentId);
    }

    private ImgChannelDTO(String name, String descriptionOrNull, Integer wavelengthOrNull,
            Long datasetIdOrNull, Long experimentIdOrNull)
    {
        assert (datasetIdOrNull == null && experimentIdOrNull != null)
                || (datasetIdOrNull != null && experimentIdOrNull == null);
        this.name = name;
        this.descriptionOrNull = descriptionOrNull;
        this.wavelengthOrNull = wavelengthOrNull;
        this.datasetIdOrNull = datasetIdOrNull;
        this.experimentIdOrNull = experimentIdOrNull;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return descriptionOrNull;
    }

    public void setDescription(String description)
    {
        this.descriptionOrNull = description;
    }

    public Integer getWavelength()
    {
        return wavelengthOrNull;
    }

    public void setWavelength(Integer wavelength)
    {
        this.wavelengthOrNull = wavelength;
    }

    public Long getDatasetId()
    {
        return datasetIdOrNull;
    }

    public void setDatasetId(Long datasetId)
    {
        this.datasetIdOrNull = datasetId;
    }

    public Long getExperimentId()
    {
        return experimentIdOrNull;
    }

    public void setExperimentId(Long experimentId)
    {
        this.experimentIdOrNull = experimentId;
    }

}
