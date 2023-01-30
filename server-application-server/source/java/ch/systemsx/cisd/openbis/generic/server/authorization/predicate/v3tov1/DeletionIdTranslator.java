package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3tov1;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public class DeletionIdTranslator
{

    public static TechId translate(IDeletionId v3deletionId)
    {
        if (v3deletionId instanceof DeletionTechId)
        {
            return new TechId(((DeletionTechId) v3deletionId).getTechId());
        }
        return null;
    }

    public static List<TechId> translate(List<IDeletionId> values)
    {
        List<TechId> valuesAsTechIds = new ArrayList<TechId>();
        for (IDeletionId value : values)
        {
            valuesAsTechIds.add(translate(value));
        }
        return valuesAsTechIds;
    }
}
