package ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dss.dto.dataset.create.UploadedDataSetCreation")
public class UploadedDataSetCreation implements ICreation
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IEntityTypeId typeId;

    @JsonProperty
    private IExperimentId experimentId;

    @JsonProperty
    private ISampleId sampleId;

    @JsonProperty
    private Map<String, String> properties = new HashMap<String, String>();

    @JsonProperty
    private List<? extends IDataSetId> parentIds;

    @JsonProperty
    private String uploadId;

    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    public ISampleId getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId = sampleId;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    public List<? extends IDataSetId> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(List<? extends IDataSetId> parentIds)
    {
        this.parentIds = parentIds;
    }

    public String getUploadId()
    {
        return uploadId;
    }

    public void setUploadId(String uploadId)
    {
        this.uploadId = uploadId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("uploadId", uploadId).append("experimentId", experimentId).append("sampleId", sampleId).toString();
    }

}
