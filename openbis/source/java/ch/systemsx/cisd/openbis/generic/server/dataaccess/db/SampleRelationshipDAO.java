package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.ISampleRelationshipDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;

public class SampleRelationshipDAO extends AbstractGenericEntityDAO<SampleRelationshipPE> implements ISampleRelationshipDAO {

    private final IRelationshipTypeDAO relationshipTypeDAO;

    protected SampleRelationshipDAO(SessionFactory sessionFactory, IRelationshipTypeDAO relationshipTypeDAO, EntityHistoryCreator historyCreator) {
        super(sessionFactory, SampleRelationshipPE.class, historyCreator);
        this.relationshipTypeDAO = relationshipTypeDAO;
    }

    public void persist(Collection<SampleRelationshipPE> sampleRelationships) {
        RelationshipTypePE relationshipType = relationshipTypeDAO.tryFindRelationshipTypeByCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        for (SampleRelationshipPE sampleRelationship : sampleRelationships)
        {
            sampleRelationship.setRelationship(relationshipType);
            currentSession().persist(sampleRelationship);
        }
    }

    public void delete(Collection<SampleRelationshipPE> sampleRelationships) {
        getHibernateTemplate().deleteAll(sampleRelationships);
    }

    public List<SampleRelationshipPE> listSampleParents(List<Long> childrenTechIds) {
        Long typeId = relationshipTypeDAO.getRelationshipTypeId(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq("relationship.id", typeId));
        criteria.add(Restrictions.in("childSample.id", childrenTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }

    public List<SampleRelationshipPE> listSampleChildren(List<Long> parentTechIds) {
        Long typeId = relationshipTypeDAO.getRelationshipTypeId(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq("relationship.id", typeId));
        criteria.add(Restrictions.in("parentSample.id", parentTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }
}
