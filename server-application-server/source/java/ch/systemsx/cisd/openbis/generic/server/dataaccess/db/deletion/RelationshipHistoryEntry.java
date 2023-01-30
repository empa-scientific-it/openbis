package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RelationshipHistoryEntry extends EntityModification
{
    public final String type = "RELATIONSHIP";

    @JsonProperty("key")
    public String relationType;

    @JsonProperty("value")
    public String relatedEntity;

    public String entityType;

    @Override
    public String toString()
    {
        return "RelationshipHistoryEntry [permId=" + permId + ", relationType=" + relationType + ", relatedEntity="
                + relatedEntity + ", entityType=" + entityType + ", userId=" + userId + ", validFromTimestamp="
                + validFrom + ", validUntilTimestamp=" + validUntil + "]";
    }
}