package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.externaldms.update.ExternalDmsUpdate")
public class ExternalDmsUpdate implements IUpdate, IObjectUpdate<IExternalDmsId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IExternalDmsId externalDmsId;

    @JsonProperty
    private FieldUpdateValue<String> label = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> address = new FieldUpdateValue<String>();

    @Override
    @JsonIgnore
    public IExternalDmsId getObjectId()
    {
        return getExternalDmsId();
    }

    @JsonIgnore
    public IExternalDmsId getExternalDmsId()
    {
        return externalDmsId;
    }

    @JsonIgnore
    public void setExternalDmsId(IExternalDmsId externalDmsId)
    {
        this.externalDmsId = externalDmsId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getLabel()
    {
        return label;
    }

    @JsonIgnore
    public void setLabel(String label)
    {
        this.label.setValue(label);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getAddress()
    {
        return address;
    }

    @JsonIgnore
    public void setAddress(String address)
    {
        this.address.setValue(address);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("externalDmsId", externalDmsId).toString();
    }

}
