package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

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

    private Long parentChildRelationshipId = null;

    protected SampleRelationshipDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, SampleRelationshipPE.class, historyCreator);
    }

    //
    // Helper Methods to obtain relationship ID "fast" and using detached criteria that should not require a session
    //

    private Long getParentChildRelationshipId()
    {
        if (parentChildRelationshipId == null)
        {
            synchronized(SampleRelationshipDAO.class)
            {
                if (parentChildRelationshipId == null)
                {
                    parentChildRelationshipId = getParentChildRelationship().getId();
                }
            }
        }
        return parentChildRelationshipId;
    }

    private RelationshipTypePE getParentChildRelationship()
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(RelationshipTypePE.class);
        criteria.add(Restrictions.eq("code", BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP));
        List<RelationshipTypePE> cast = cast(getHibernateTemplate().findByCriteria(criteria));
        return cast.get(0);
    }

    //
    // DAO Methods
    //

    public void persist(Collection<SampleRelationshipPE> sampleRelationships)
    {
        RelationshipTypePE relationshipType = getParentChildRelationship();
        for (SampleRelationshipPE sampleRelationship : sampleRelationships)
        {
            sampleRelationship.setRelationship(relationshipType);
            currentSession().persist(sampleRelationship);
        }
    }

    public void delete(Collection<SampleRelationshipPE> sampleRelationships)
    {
        getHibernateTemplate().deleteAll(sampleRelationships);
    }

    public List<SampleRelationshipPE> listSampleParents(List<Long> childrenTechIds)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq("relationship.id", getParentChildRelationshipId()));
        criteria.add(Restrictions.in("childSample.id", childrenTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }

    public List<SampleRelationshipPE> listSampleChildren(List<Long> parentTechIds)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq("relationship.id", getParentChildRelationshipId()));
        criteria.add(Restrictions.in("parentSample.id", parentTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }
}
