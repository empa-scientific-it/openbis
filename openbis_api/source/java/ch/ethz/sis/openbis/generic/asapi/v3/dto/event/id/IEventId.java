package ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Holds information that uniquely identifies an event in openBIS.
 *
 * @author pkupczyk
 */
@JsonObject("as.dto.event.id.IEventId")
public interface IEventId extends IObjectId
{

}
