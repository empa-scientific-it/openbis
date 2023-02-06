package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

@JsonObject("as.dto.externaldms.ExternalDmsAddressType")
public enum ExternalDmsAddressType
{
    OPENBIS, URL, FILE_SYSTEM;
}