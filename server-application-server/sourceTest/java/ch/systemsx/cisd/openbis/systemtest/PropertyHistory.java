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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.Date;

/**
 * Helper class with property history.
 * 
 * @author Franz-Josef Elmer
 */
public class PropertyHistory
{
    private String propertyTypeCode;

    private String value;

    private String term;

    private String material;

    private Long persIdAuthor;

    private Date validFromTimeStamp;

    private Date validUntilTimeStamp;

    public Date getValidUntilTimeStamp()
    {
        return validUntilTimeStamp;
    }

    public void setValidUntilTimeStamp(Date validUntilTimeStamp)
    {
        this.validUntilTimeStamp = validUntilTimeStamp;
    }

    public Long getPersIdAuthor()
    {
        return persIdAuthor;
    }

    public void setPersIdAuthor(Long persIdRegisterer)
    {
        this.persIdAuthor = persIdRegisterer;
    }

    public Date getValidFromTimeStamp()
    {
        return validFromTimeStamp;
    }

    public void setValidFromTimeStamp(Date validFromTimeStamp)
    {
        this.validFromTimeStamp = validFromTimeStamp;
    }

    public void setPropertyTypeCode(String propertyTypeCode)
    {
        this.propertyTypeCode = propertyTypeCode;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setTerm(String term)
    {
        this.term = term;
    }

    public void setMaterial(String material)
    {
        this.material = material;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(propertyTypeCode).append(":");
        if (value != null)
        {
            builder.append(' ').append(value);
        }
        if (term != null)
        {
            builder.append(" term:").append(term);
        }
        if (material != null)
        {
            builder.append(" material:").append(material);
        }
        builder.append("<a:" + persIdAuthor + ">");
        return builder.toString();
    }

}