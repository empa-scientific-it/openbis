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
    protected SampleRelationshipDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator) {
        super(sessionFactory, SampleRelationshipPE.class, historyCreator);
    }

    public void delete(Collection<SampleRelationshipPE> sampleRelationships) {
        getHibernateTemplate().deleteAll(sampleRelationships);
    }

    public List<SampleRelationshipPE> listSampleParents(List<TechId> childrenTechIds) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq(ColumnNames.RELATIONSHIP_COLUMN, 1));
        criteria.add(Restrictions.in(ColumnNames.CHILD_SAMPLE_COLUMN, childrenTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }

    public List<SampleRelationshipPE> listSampleChildren(List<TechId> parentTechIds) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(SampleRelationshipPE.class);
        criteria.add(Restrictions.eq(ColumnNames.RELATIONSHIP_COLUMN, 1));
        criteria.add(Restrictions.in(ColumnNames.PARENT_SAMPLE_COLUMN, parentTechIds));
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }
}
