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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Izabela Adamczyk
 */
public class PropertyType extends Code<PropertyType> implements IsSerializable
{
    private String simpleCode;

    private String label;

    private boolean internalNamespace;

    private DataType dataType;

    private Vocabulary vocabulary;

    public String getSimpleCode()
    {
        return simpleCode;
    }

    public void setSimpleCode(final String code)
    {
        this.simpleCode = code;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    public boolean isInternalNamespace()
    {
        return internalNamespace;
    }

    public void setInternalNamespace(final boolean internalNamespace)
    {
        this.internalNamespace = internalNamespace;
    }

    public DataType getDataType()
    {
        return dataType;
    }

    public void setDataType(final DataType dataType)
    {
        this.dataType = dataType;
    }

    public Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    public void setVocabulary(final Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

}
