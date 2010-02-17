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

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;

/**
 * Description of one datastore server plugin task: key, label, dataset type codes.
 * 
 * @author Tomasz Pylak
 */
public class DatastoreServiceDescription implements IReportInformationProvider, IsSerializable,
        Serializable, Comparable<DatastoreServiceDescription>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String key;

    private String label;

    private String[] datasetTypeCodes;

    private String datastoreCode;

    private String downloadURL;

    @SuppressWarnings("unused")
    // for GWT serialization
    private DatastoreServiceDescription()
    {
    }

    public DatastoreServiceDescription(String key, String label, String[] datasetTypeCodes,
            String datastoreCode)
    {
        this.key = key;
        this.label = label;
        this.datasetTypeCodes = datasetTypeCodes;
        this.datastoreCode = datastoreCode;
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

    public String getDatastoreCode()
    {
        return datastoreCode;
    }

    public String getDownloadURL()
    {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL)
    {
        this.downloadURL = downloadURL;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(getKey());
        sb.append("; ");
        sb.append(getDatastoreCode());
        sb.append("; ");
        sb.append(getLabel());
        sb.append("; ");
        for (String code : getDatasetTypeCodes())
        {
            sb.append(code);
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public static boolean isMatching(DatastoreServiceDescription service, ExternalData dataset)
    {
        return service.getDatastoreCode().equals(dataset.getDataStore().getCode())
                && (Arrays.asList(service.getDatasetTypeCodes())).contains(dataset.getDataSetType()
                        .getCode());
    }

    //
    // Comparable
    //

    public int compareTo(DatastoreServiceDescription o)
    {
        return this.getLabel().compareTo(o.getLabel());
    }
}
