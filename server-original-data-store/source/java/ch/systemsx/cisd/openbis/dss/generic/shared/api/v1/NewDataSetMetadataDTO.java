/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents optional metadata (data set type and properties) of a new data set that the DSS should register. The server may override the metadata
 * specified here.
 * 
 * @author Piotr Buczek
 */
public class NewDataSetMetadataDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<String, Serializable> properties = new HashMap<String, Serializable>();

    private final Set<String> unmodifiableProperties = new HashSet<String>();

    private String dataSetTypeOrNull;

    private final ArrayList<String> parentDataSetCodes = new ArrayList<String>();

    public NewDataSetMetadataDTO(String dataSetTypeOrNull, Map<String, Serializable> propertiesOrNull)
    {
        this(dataSetTypeOrNull, propertiesOrNull, null);
    }

    public NewDataSetMetadataDTO(String dataSetTypeOrNull, Map<String, Serializable> propertiesOrNull,
            List<String> parentDataSetCodesOrNull)
    {
        this.dataSetTypeOrNull = dataSetTypeOrNull;
        setProperties(propertiesOrNull);
        setParentDataSetCodes(parentDataSetCodesOrNull);
    }

    public NewDataSetMetadataDTO()
    {
        this(null, null);
    }

    /**
     * The code for the type of the new data set. May be null.
     */
    public String tryDataSetType()
    {
        return dataSetTypeOrNull;
    }

    public void setDataSetTypeOrNull(String dataSetTypeOrNull)
    {
        // Capitalize the data set type
        this.dataSetTypeOrNull =
                (dataSetTypeOrNull != null) ? dataSetTypeOrNull.toUpperCase() : null;
    }

    /**
     * The properties for the new data set. Key is the property code, value is the value (as a string).
     */
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    /**
     * The unmodifiable property types (as strings).
     */
    public Collection<String> getUnmodifiableProperties()
    {
        return unmodifiableProperties;
    }

    public void setProperties(Map<String, Serializable> props)
    {
        if (props != null)
        {
            properties.putAll(props);
        }
    }

    public void setUnmodifiableProperties(Set<String> props)
    {
        unmodifiableProperties.clear();
        if (props != null)
        {
            unmodifiableProperties.addAll(props);
        }
    }

    public void setUnmodifiableProperties(Map<String, Serializable> props)
    {
        setProperties(props);
        setUnmodifiableProperties(props.keySet());
    }

    public boolean isUnmodifiableProperty(String property)
    {
        return unmodifiableProperties.contains(property);
    }

    /**
     * The codes of the parent data sets for this new data set. The list may be empty.
     * 
     * @since 1.3
     */
    public List<String> getParentDataSetCodes()
    {
        return Collections.unmodifiableList(parentDataSetCodes);
    }

    /**
     * Sets the parent data sets of this data set.
     * 
     * @param codesOrNull If the value is null, the parents are cleared.
     * @since 1.3
     */
    public void setParentDataSetCodes(List<String> codesOrNull)
    {
        parentDataSetCodes.clear();
        if (codesOrNull != null)
        {
            parentDataSetCodes.addAll(codesOrNull);
        }
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        String myDataSetTypeOrNull = tryDataSetType();
        if (null != myDataSetTypeOrNull)
        {
            sb.append("data set type", myDataSetTypeOrNull);
        }
        for (Entry<String, Serializable> entry : getProperties().entrySet())
        {
            sb.append("property " + entry.getKey(), entry.getValue());
        }
        return sb.toString();
    }
}
