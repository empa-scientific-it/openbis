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

import net.lemnik.eodsql.AutoGeneratedKeys;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * A class that represents a MSRun in an eicML file.
 */
public class MSRunDTO
{
    @AutoGeneratedKeys
    public long msRunId;

    public String permId;
    
    public String rawDataFilePath;

    public String rawDataFileName;

    public String instrumentType;

    public String instrumentManufacturer;

    public String instrumentModel;

    public String methodIonisation;

    public String methodSeparation;

    public String acquisitionDate;

    public int chromCount = -1;

    public float startTime = Float.NaN;

    public float endTime = Float.NaN;

    void set(String name, String value) throws SAXException
    {
        if ("filePath".equals(name))
        {
            rawDataFilePath = value;
        } else if ("fileName".equals(name))
        {
            rawDataFileName = value;
        } else if ("instrumentType".equals(name))
        {
            instrumentType = value;
        } else if ("instrumentManufacturer".equals(name))
        {
            instrumentManufacturer = value;
        } else if ("instrumentModel".equals(name))
        {
            instrumentModel = value;
        } else if ("methodIonisation".equals(name))
        {
            methodIonisation = value;
        } else if ("methodSeparation".equals(name))
        {
            methodSeparation = value;
        } else if ("acquisitionDate".equals(name))
        {
            acquisitionDate = value;
        } else if ("chromCount".equals(name) && value.length() > 0)
        {
            chromCount = Integer.parseInt(value);
        } else if ("msRunId".equals(name) && value.length() > 0)
        {
            msRunId = Long.parseLong(value);
        } else if ("startTime".equals(name) && value.length() > 0)
        {
            startTime = Float.parseFloat(value);
        } else if ("endTime".equals(name) && value.length() > 0)
        {
            endTime = Float.parseFloat(value);
        }
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}