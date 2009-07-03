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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Description of one datastore server plugin task: key, label, dataset type codes.
 * 
 * @author Tomasz Pylak
 */
public class PluginTaskDescription implements IsSerializable
{
    private String key;

    private String label;

    private String[] datasetTypeCodes;

    @SuppressWarnings("unused")
    // for serialization
    private PluginTaskDescription()
    {
    }

    public PluginTaskDescription(String key, String label, String[] datasetTypeCodes)
    {
        this.key = key;
        this.label = label;
        this.datasetTypeCodes = datasetTypeCodes;
    }

    /** the unique key of the plugin */
    public String getKey()
    {
        return key;
    }

    /** the user friendly name of the plugin */
    public String getLabel()
    {
        return label;
    }

    /** codes of dataset types which are handled by this plugin */
    public String[] getDatasetTypeCodes()
    {
        return datasetTypeCodes;
    }

    // for serialization

    @SuppressWarnings("unused")
    private void setKey(String key)
    {
        this.key = key;
    }

    @SuppressWarnings("unused")
    private void setLabel(String label)
    {
        this.label = label;
    }

    @SuppressWarnings("unused")
    private void setDatasetTypeCodes(String[] datasetTypeCodes)
    {
        this.datasetTypeCodes = datasetTypeCodes;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(getKey());
        sb.append("; ");
        sb.append(getLabel());
        sb.append("; ");
        for (String code : getDatasetTypeCodes())
        {
            sb.append(code);
            sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}