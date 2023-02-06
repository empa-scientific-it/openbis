package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

/**
 * Holds information that uniquely identifies a content copy in openBIS.
 * 
 * @author anttil
 */
@JsonObject("as.dto.dataset.id.IContentCopyId")
public interface IContentCopyId extends IObjectId
{
}
