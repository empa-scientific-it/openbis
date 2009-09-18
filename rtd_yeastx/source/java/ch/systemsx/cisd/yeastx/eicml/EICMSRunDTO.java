/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.eicml;

import java.util.Date;

import net.lemnik.eodsql.AutoGeneratedKeys;
import net.lemnik.eodsql.ResultColumn;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A class that represents a MSRun in an eicML file.
 */
public class EICMSRunDTO
{
    @AutoGeneratedKeys
    private long id;

    @ResultColumn("DS_ID")
    private long dataSetId;

    @ResultColumn("SAMP_ID")
    private Long sampleId;

    @ResultColumn("EXPE_ID")
    private long experimentId;

    @ResultColumn("RAW_DATA_FILE_PATH")
    private String rawDataFilePath;

    @ResultColumn("RAW_DATA_FILE_NAME")
    private String rawDataFileName;

    @ResultColumn("INSTRUMENT_TYPE")
    private String instrumentType;

    @ResultColumn("INSTRUMENT_MANUFACTURER")
    private String instrumentManufacturer;

    @ResultColumn("INSTRUMENT_MODEL")
    private String instrumentModel;

    @ResultColumn("METHOD_IONISATION")
    private String methodIonisation;

    @ResultColumn("METHOD_SEPARATION")
    private String methodSeparation;

    @ResultColumn("ACQUISITION_DATE")
    private Date acquisitionDate;

    @ResultColumn("MS_RUN_ID")
    private Long msRunId;

    @ResultColumn("SET_ID")
    private Long setId;

    private String operator;

    private int chromCount = -1;

    @ResultColumn("START_TIME")
    private float startTime = Float.NaN;

    @ResultColumn("END_TIME")
    private float endTime = Float.NaN;

    public long getId()
    {
        return id;
    }

    public long getDataSetId()
    {
        return dataSetId;
    }

    public void setDataSetId(long dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public long getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(long experimentId)
    {
        this.experimentId = experimentId;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getRawDataFilePath()
    {
        return rawDataFilePath;
    }

    public void setRawDataFilePath(String rawDataFilePath)
    {
        this.rawDataFilePath = rawDataFilePath;
    }

    public String getRawDataFileName()
    {
        return rawDataFileName;
    }

    public void setRawDataFileName(String rawDataFileName)
    {
        this.rawDataFileName = rawDataFileName;
    }

    public String getInstrumentType()
    {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType)
    {
        this.instrumentType = instrumentType;
    }

    public String getInstrumentManufacturer()
    {
        return instrumentManufacturer;
    }

    public void setInstrumentManufacturer(String instrumentManufacturer)
    {
        this.instrumentManufacturer = instrumentManufacturer;
    }

    public String getInstrumentModel()
    {
        return instrumentModel;
    }

    public void setInstrumentModel(String instrumentModel)
    {
        this.instrumentModel = instrumentModel;
    }

    public String getMethodIonisation()
    {
        return methodIonisation;
    }

    public void setMethodIonisation(String methodIonisation)
    {
        this.methodIonisation = methodIonisation;
    }

    public String getMethodSeparation()
    {
        return methodSeparation;
    }

    public void setMethodSeparation(String methodSeparation)
    {
        this.methodSeparation = methodSeparation;
    }

    public Date getAcquisitionDate()
    {
        return acquisitionDate;
    }

    public void setAcquisitionDate(Date acquisitionDate)
    {
        this.acquisitionDate = acquisitionDate;
    }

    public Long getMsRunId()
    {
        return msRunId;
    }

    public void setMsRunId(Long msRunId)
    {
        this.msRunId = msRunId;
    }

    public Long getSetId()
    {
        return setId;
    }

    public void setSetId(long setId)
    {
        this.setId = setId;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public int getChromCount()
    {
        return chromCount;
    }

    public void setChromCount(int chromCount)
    {
        this.chromCount = chromCount;
    }

    public float getStartTime()
    {
        return startTime;
    }

    public void setStartTime(float startTime)
    {
        this.startTime = startTime;
    }

    public float getEndTime()
    {
        return endTime;
    }

    public void setEndTime(float endTime)
    {
        this.endTime = endTime;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}