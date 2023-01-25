package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.ISampleRelationshipDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;

public class SampleRelationshipDAO extends AbstractGenericEntityDAO<SampleRelationshipPE> implements ISampleRelationshipDAO {

    private static final Long PARENT_CHILDREN = 1L;

    protected SampleRelationshipDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator) {
        super(sessionFactory, SampleRelationshipPE.class, historyCreator);
    }

    public void delete(Collection<SampleRelationshipPE> sampleRelationships) {
        getHibernateTemplate().deleteAll(sampleRelationships);
    }

    public List<SampleRelationshipPE> listSampleParents(List<Long> childrenTechIds) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq("relationship.id", PARENT_CHILDREN));
        criteria.add(Restrictions.in("childSample.id", childrenTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }

    public List<SampleRelationshipPE> listSampleChildren(List<Long> parentTechIds) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq("relationship.id", PARENT_CHILDREN));
        criteria.add(Restrictions.in("parentSample.id", parentTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }
}
