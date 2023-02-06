package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("ExternalDataManagementSystemType")
public enum ExternalDataManagementSystemType
{
    OPENBIS, URL, FILE_SYSTEM;
}
