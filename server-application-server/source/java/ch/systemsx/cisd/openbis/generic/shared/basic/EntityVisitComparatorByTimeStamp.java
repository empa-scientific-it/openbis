package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.Comparator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;

/**
 * Comparator between {@link EntityVisit} instances. Newer visit comes before older visit.
 *
 * @author Franz-Josef Elmer
 */
public class EntityVisitComparatorByTimeStamp implements Comparator<EntityVisit>
{
    @Override
    public int compare(EntityVisit o1, EntityVisit o2)
    {
        long t1 = o1.getTimeStamp();
        long t2 = o2.getTimeStamp();
        return t1 < t2 ? 1 : (t1 > t2 ? -1 : 0);
    }
}