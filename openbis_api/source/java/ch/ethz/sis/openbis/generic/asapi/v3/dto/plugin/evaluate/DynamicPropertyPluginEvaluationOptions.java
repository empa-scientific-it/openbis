package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.plugin.evaluate.DynamicPropertyPluginEvaluationOptions")
public class DynamicPropertyPluginEvaluationOptions extends PluginEvaluationOptions
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IObjectId objectId;

    @JsonIgnore
    public IObjectId getObjectId()
    {
        return objectId;
    }

    public void setObjectId(IObjectId objectId)
    {
        this.objectId = objectId;
    }

    @Override
    protected ObjectToString toObjectToString()
    {
        return super.toObjectToString().append("objectId", objectId);
    }

    @Override
    public String toString()
    {
        return toObjectToString().toString();
    }

}
