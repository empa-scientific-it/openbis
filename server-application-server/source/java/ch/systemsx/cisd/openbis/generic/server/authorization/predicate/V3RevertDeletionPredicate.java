package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3tov1.DeletionIdTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

@ShouldFlattenCollections(value = false)
public class V3RevertDeletionPredicate extends AbstractPredicate<List<IDeletionId>>
{
    private RevertDeletionPredicate revertDeletionPredicate;

    public V3RevertDeletionPredicate()
    {
        this.revertDeletionPredicate = new RevertDeletionPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        revertDeletionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 deletion id object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<IDeletionId> values)
    {
        List<TechId> valuesAsTechIds = DeletionIdTranslator.translate(values);
        return revertDeletionPredicate.doEvaluation(person, allowedRoles, valuesAsTechIds);
    }
}
