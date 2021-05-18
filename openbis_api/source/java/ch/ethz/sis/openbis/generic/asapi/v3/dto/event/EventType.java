package ch.ethz.sis.openbis.generic.asapi.v3.dto.event;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.event.EventType")
public enum EventType
{
    DELETION, MOVEMENT, FREEZING
}
