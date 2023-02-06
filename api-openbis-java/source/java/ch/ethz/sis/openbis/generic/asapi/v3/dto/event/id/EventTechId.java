package ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectTechId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.event.id.EventTechId")
public class EventTechId extends ObjectTechId implements IEventId
{

    private static final long serialVersionUID = 1L;

    public EventTechId(Long techId)
    {
        super(techId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private EventTechId()
    {
        super();
    }

}