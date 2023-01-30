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
public class V3DeletionIdPredicate extends AbstractPredicate<List<IDeletionId>>
{

    protected final DeletionTechIdCollectionPredicate deletionTechIdCollectionPredicate;

    public V3DeletionIdPredicate()
    {
        this.deletionTechIdCollectionPredicate = new DeletionTechIdCollectionPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        deletionTechIdCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "v3 deletion id object";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<IDeletionId> values)
    {
        assert deletionTechIdCollectionPredicate.initialized : "Predicate has not been initialized";
        List<TechId> valuesAsTechIds = DeletionIdTranslator.translate(values);
        return deletionTechIdCollectionPredicate.doEvaluation(person, allowedRoles, valuesAsTechIds);
    }
}
