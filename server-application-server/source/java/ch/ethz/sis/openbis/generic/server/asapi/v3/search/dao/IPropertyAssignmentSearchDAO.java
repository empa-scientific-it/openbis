package ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao;

import java.util.Set;

/**
 * Data access interface for ad hoc functionality for assignments.
 */
public interface IPropertyAssignmentSearchDAO
{

    /**
     * Ad doc method for searching assignments with no annotations.
     * <p/>
     * Since no user rights filtering is needed this can be done in one query.
     *
     * @return search result
     */
    Set<Long> findAssignmentsWithoutAnnotations(final Set<Long> semanticAnnotationsPropertyIds,
            final String idsColumnName);

}
