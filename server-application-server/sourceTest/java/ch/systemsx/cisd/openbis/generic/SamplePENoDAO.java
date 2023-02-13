package ch.systemsx.cisd.openbis.generic;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

import java.util.HashSet;
import java.util.Set;

/*
 * Many tests have never used the DB, they lack a lot of necessary constraints.
 *
 * Under a real scenario those tests will actually throw errors.
 *
 * This abstraction allows them to avoid using the introduced DAO behaving as naively as before introducing it.
 */
public class SamplePENoDAO extends SamplePE {
    @Override
    protected Set<SampleRelationshipPE> getSampleChildRelationships() {
        if (childRelationships == null) {
            childRelationships = new HashSet<>();
        }
        return childRelationships;
    }

    @Override
    protected Set<SampleRelationshipPE> getSampleParentRelationships() {
        if(parentRelationships == null) {
            parentRelationships = new HashSet<>();
        }
        return parentRelationships;
    }

    @Override
    public void addChildRelationship(final SampleRelationshipPE relationship)
    {
        relationship.setParentSample(this);
        getSampleChildRelationships().add(relationship);
    }

    @Override
    public void addParentRelationship(final SampleRelationshipPE relationship)
    {
        relationship.setChildSample(this);
        getSampleParentRelationships().add(relationship);
    }
}
