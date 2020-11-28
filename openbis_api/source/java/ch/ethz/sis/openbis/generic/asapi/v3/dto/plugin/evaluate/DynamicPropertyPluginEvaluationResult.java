package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.plugin.evaluate.DynamicPropertyPluginEvaluationResult")
public class DynamicPropertyPluginEvaluationResult extends PluginEvaluationResult
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String value;

    @SuppressWarnings("unused")
    private DynamicPropertyPluginEvaluationResult()
    {
    }

    public DynamicPropertyPluginEvaluationResult(String value)
    {
        this.value = value;
    }

    @JsonIgnore
    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("value", value).toString();
    }

}
