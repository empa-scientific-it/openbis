package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Collection;
import java.util.List;

public interface ISampleRelationshipDAO {

    public void persist(Collection<SampleRelationshipPE> sampleRelationships);

    public void delete(Collection<SampleRelationshipPE> relationships);

    public List<SampleRelationshipPE> listSampleParents(List<Long> childrenTechIds);

    public List<SampleRelationshipPE> listSampleChildren(List<Long> parentTechIds);
}
