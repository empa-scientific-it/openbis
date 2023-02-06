package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.plugin.evaluate.PluginEvaluationOptions")
public abstract class PluginEvaluationOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IPluginId pluginId;

    @JsonProperty
    private String pluginScript;

    @JsonIgnore
    public IPluginId getPluginId()
    {
        return pluginId;
    }

    public void setPluginId(IPluginId pluginId)
    {
        this.pluginId = pluginId;
    }

    @JsonIgnore
    public String getPluginScript()
    {
        return pluginScript;
    }

    public void setPluginScript(String pluginScript)
    {
        this.pluginScript = pluginScript;
    }

    protected ObjectToString toObjectToString()
    {
        return new ObjectToString(this).append("pluginId", pluginId).append("pluginScript", pluginScript);
    }

    @Override
    public String toString()
    {
        return toObjectToString().toString();
    }

}
