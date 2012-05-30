package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;

import ch.systemsx.cisd.common.json.JsonObject;



/**
 * Contains data which uniquely define a dataset.
 * 
 * @author Tomasz Pylak
 */

@SuppressWarnings("unused")
@JsonObject("DatasetIdentifier")
@JsonSubTypes(value={@JsonSubTypes.Type(DatasetReference.class), 
        @JsonSubTypes.Type(MicroscopyImageReference.class), 
        @JsonSubTypes.Type(PlateImageReference.class)})
public class DatasetIdentifier implements Serializable, IDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private String datasetCode;

    // a.k.a. downloadURL
    private String datastoreServerUrl;

    public DatasetIdentifier(String datasetCode, String datastoreServerUrl)
    {
        this.datasetCode = datasetCode;
        this.datastoreServerUrl = datastoreServerUrl;
    }

    /**
     * The code of this dataset.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    public String getPermId()
    {
        return datasetCode;
    }

    public String getDatastoreServerUrl()
    {
        return datastoreServerUrl;
    }

    @Override
    public String toString()
    {
        return datasetCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || obj instanceof DatasetIdentifier == false)
        {
            return false;
        }
        DatasetIdentifier that = (DatasetIdentifier) obj;
        return datasetCode.equals(that.datasetCode);
    }

    @Override
    public int hashCode()
    {
        return datasetCode.hashCode();
    }

    //
    // JSON-RPC
    //

    private DatasetIdentifier()
    {
    }

    private void setDatasetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    private void setPermId(String permId)
    {
        this.datasetCode = permId;
    }

    private void setDatastoreServerUrl(String datastoreServerUrl)
    {
        this.datastoreServerUrl = datastoreServerUrl;
    }

}