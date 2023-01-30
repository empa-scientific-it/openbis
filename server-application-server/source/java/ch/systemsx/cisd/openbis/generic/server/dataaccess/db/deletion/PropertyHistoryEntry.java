package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PropertyHistoryEntry extends EntityModification
{
    public final String type = "PROPERTY";

    @JsonProperty("key")
    public String propertyCode;

    public String value;

    @Override
    public String toString()
    {
        return "HistoryPropertyEntry [permId=" + permId + ", propertyCode=" + propertyCode + ", value=" + value
                + ", user_id=" + userId
                + ", valid_from_timestamp=" + validFrom + ", valid_until_timestamp=" + validUntil + "]";
    }
}
