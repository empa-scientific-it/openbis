package ch.ethz.sis.openbis.generic.asapi.v3.dto.event;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.event.EntityType")
public enum EntityType
{
    ATTACHMENT, DATA_SET, EXPERIMENT, SPACE, MATERIAL, PROJECT, PROPERTY_TYPE, SAMPLE,
    VOCABULARY, AUTHORIZATION_GROUP, TAG;
}
