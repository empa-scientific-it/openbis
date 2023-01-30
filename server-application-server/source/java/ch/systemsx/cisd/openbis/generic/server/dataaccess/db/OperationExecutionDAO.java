/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IOperationExecutionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;

/**
 * <i>Data Access Object</i> implementation for {@link OperationExecutionPE}.
 * 
 * @author pkupczyk
 */
final class OperationExecutionDAO extends AbstractGenericEntityDAO<OperationExecutionPE> implements IOperationExecutionDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OperationExecutionDAO.class);

    OperationExecutionDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, OperationExecutionPE.class, historyCreator);
    }

    @Override
    public void createOrUpdate(OperationExecutionPE execution)
    {
        validatePE(execution);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(execution);
        template.flush();

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Created or updated operation execution '%s'.", execution));
        }
    }

    @Override
    public OperationExecutionPE tryFindByCode(String code)
    {
        List<OperationExecutionPE> list = findByCodes(Arrays.asList(code));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<OperationExecutionPE> findByCodes(Collection<String> codes)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(OperationExecutionPE.class);
        criteria.add(Restrictions.in("code", codes));
        criteria.addOrder(Order.asc("code"));

        final List<OperationExecutionPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d executions(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<OperationExecutionPE> findByIds(Collection<Long> ids)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(OperationExecutionPE.class);
        criteria.add(Restrictions.in("id", ids));

        final List<OperationExecutionPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d executions(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeFailedAfterServerRestart(Date serverStartDate)
    {
        DetachedCriteria criteria = DetachedCriteria.forClass(OperationExecutionPE.class);
        criteria.add(Restrictions.in("state",
                Arrays.asList(OperationExecutionState.NEW, OperationExecutionState.SCHEDULED, OperationExecutionState.RUNNING)));
        criteria.add(Restrictions.lt("creationDate", serverStartDate));

        final List<OperationExecutionPE> executions = new ArrayList<OperationExecutionPE>();
        executions.addAll(findByCriteria(criteria));
        sortFromOldestToNewest(executions);
        return executions;
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeTimeOutPending()
    {
        DetachedCriteria criteria = DetachedCriteria.forClass(OperationExecutionPE.class);
        criteria.add(Restrictions.in("state", Arrays.asList(OperationExecutionState.FAILED, OperationExecutionState.FINISHED)));
        criteria.add(Restrictions.or(Restrictions.eq("availability", OperationExecutionAvailability.AVAILABLE),
                Restrictions.eq("summaryAvailability", OperationExecutionAvailability.AVAILABLE),
                Restrictions.eq("detailsAvailability", OperationExecutionAvailability.AVAILABLE)));

        final List<OperationExecutionPE> executions = new ArrayList<OperationExecutionPE>();

        for (OperationExecutionPE execution : findByCriteria(criteria))
        {
            boolean matches = false;

            if (OperationExecutionAvailability.AVAILABLE.equals(execution.getAvailability())
                    && execution.getAvailabilityTimeLeft() != null
                    && execution.getAvailabilityTimeLeft() <= 0)
            {
                matches = true;
            } else if (OperationExecutionAvailability.AVAILABLE.equals(execution.getSummaryAvailability())
                    && execution.getSummaryAvailabilityTimeLeft() != null
                    && execution.getSummaryAvailabilityTimeLeft() <= 0)
            {
                matches = true;
            } else if (OperationExecutionAvailability.AVAILABLE.equals(execution.getDetailsAvailability())
                    && execution.getDetailsAvailabilityTimeLeft() != null
                    && execution.getDetailsAvailabilityTimeLeft() <= 0)
            {
                matches = true;
            }

            if (matches)
            {
                executions.add(execution);
            }
        }

        sortFromOldestToNewest(executions);
        return executions;
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeTimedOut()
    {
        DetachedCriteria criteria = DetachedCriteria.forClass(OperationExecutionPE.class);
        criteria.add(Restrictions.in("state", Arrays.asList(OperationExecutionState.FAILED, OperationExecutionState.FINISHED)));
        criteria.add(Restrictions.or(Restrictions.eq("availability", OperationExecutionAvailability.TIME_OUT_PENDING),
                Restrictions.eq("summaryAvailability", OperationExecutionAvailability.TIME_OUT_PENDING),
                Restrictions.eq("detailsAvailability", OperationExecutionAvailability.TIME_OUT_PENDING)));

        List<OperationExecutionPE> executions = findByCriteria(criteria);
        sortFromOldestToNewest(executions);
        return executions;
    }

    @Override
    public List<OperationExecutionPE> getExecutionsToBeDeleted()
    {
        DetachedCriteria criteria = DetachedCriteria.forClass(OperationExecutionPE.class);
        criteria.add(Restrictions.in("state", Arrays.asList(OperationExecutionState.FAILED, OperationExecutionState.FINISHED)));
        criteria.add(Restrictions.or(Restrictions.eq("availability", OperationExecutionAvailability.DELETE_PENDING),
                Restrictions.eq("summaryAvailability", OperationExecutionAvailability.DELETE_PENDING),
                Restrictions.eq("detailsAvailability", OperationExecutionAvailability.DELETE_PENDING)));

        List<OperationExecutionPE> executions = findByCriteria(criteria);
        sortFromOldestToNewest(executions);
        return executions;
    }

    private void sortFromOldestToNewest(List<OperationExecutionPE> executions)
    {
        Collections.sort(executions, new Comparator<OperationExecutionPE>()
            {
                @Override
                public int compare(OperationExecutionPE o1, OperationExecutionPE o2)
                {
                    return o1.getId().compareTo(o2.getId());
                }
            });
    }

    private List<OperationExecutionPE> findByCriteria(DetachedCriteria criteria)
    {
        return cast(getHibernateTemplate().findByCriteria(criteria));
    }

}
