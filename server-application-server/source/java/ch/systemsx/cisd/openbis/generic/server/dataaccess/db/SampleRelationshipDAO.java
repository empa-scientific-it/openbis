package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.ISampleRelationshipDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
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
        criteria.add(Restrictions.eq("simpleCode", BasicConstant.PARENT_CHILD_DB_RELATIONSHIP));
        List<RelationshipTypePE> cast = cast(getHibernateTemplate().findByCriteria(criteria));
        return cast.get(0);
    }

//    private long getNextId() {
//        return getNextSequenceId(SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE);
//    }

    //
    // DAO Methods
    //

    public void persist(Collection<SampleRelationshipPE> sampleRelationships)
    {
        RelationshipTypePE relationshipType = getParentChildRelationship();
        for (SampleRelationshipPE sampleRelationship : sampleRelationships)
        {
            sampleRelationship.setRelationship(relationshipType);
// This alternative implementations attaches the object to the session without flushing it to the database
//            // Set id so PE object can be attached to session, if the id is null an Exception is thrown.
//            if (sampleRelationship.getId() == null) {
//                sampleRelationship.setId(getNextId());
//            }
//            // Attach object to session if is not already, attaching an already attach object results in an Exception.
//            if (getHibernateTemplate().contains(sampleRelationship) == false) {
//                getHibernateTemplate().update(sampleRelationship);
//            }
            getHibernateTemplate().persist(sampleRelationship);
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
