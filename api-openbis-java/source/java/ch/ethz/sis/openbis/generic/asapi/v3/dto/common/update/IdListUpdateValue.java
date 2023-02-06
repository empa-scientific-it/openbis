package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.update.IdListUpdateValue")
public class IdListUpdateValue<T> extends ListUpdateValue<T, T, T, T>
{
    private static final long serialVersionUID = 1L;

}
