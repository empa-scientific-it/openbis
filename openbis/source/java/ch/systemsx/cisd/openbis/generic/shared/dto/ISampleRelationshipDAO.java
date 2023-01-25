package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

import java.util.Collection;
import java.util.List;

public interface ISampleRelationshipDAO {

    public void delete(Collection<SampleRelationshipPE> relationships);

    public List<SampleRelationshipPE> listSampleParents(List<TechId> childrenTechIds);

    public List<SampleRelationshipPE> listSampleChildren(List<TechId> parentTechIds);
}
