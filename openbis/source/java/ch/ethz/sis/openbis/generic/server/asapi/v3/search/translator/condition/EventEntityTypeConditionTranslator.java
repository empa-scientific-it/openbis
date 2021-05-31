package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;

public class EventEntityTypeConditionTranslator extends EnumFieldSearchConditionTranslator
{
    @Override protected String getDatabaseValue(final Enum<?> criterionValue)
    {
        if (EntityType.TAG.equals(criterionValue))
        {
            return EventPE.EntityType.METAPROJECT.name();
        } else if (EntityType.DATA_SET.equals(criterionValue))
        {
            return EventPE.EntityType.DATASET.name();
        } else
        {
            return criterionValue.name();
        }
    }
}
