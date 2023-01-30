/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Implementation of {@link IPermIdDAO}.
 * 
 * @author Izabela Adamczyk
 */
public class PermIdDAO extends AbstractDAO implements IPermIdDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PermIdDAO.class);

    private final static String PERM_ID_DATE_FORMAT_PATTERN = "yyyyMMddHHmmssSSS";

    protected PermIdDAO(final SessionFactory sessionFactory)
    {
        super(sessionFactory);
    }

    @Override
    public String createPermId()
    {
        long id = getNextSequenceId(SequenceNames.PERM_ID_SEQUENCE);
        return DateFormatUtils.format(getTransactionTimeStamp(), PERM_ID_DATE_FORMAT_PATTERN) + "-"
                + Long.toString(id);
    }

    @Override
    public List<String> createPermIds(int n)
    {
        List<String> result = new ArrayList<String>(n);
        for (int i = 0; i < n; i++)
        {
            result.add(createPermId());
        }
        return result;
    }

    @Override
    public IEntityInformationHolderDTO tryToFindByPermId(String permId, EntityKind entityKind)
    {
        assert permId != null : "Unspecified permId";
        final DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getEntityClass());
        criteria.add(Restrictions.eq("permId", permId));
        final List<IEntityInformationHolderDTO> list =
                cast(getHibernateTemplate().findByCriteria(criteria));
        final IEntityInformationHolderDTO entity = tryFindEntity(list, entityKind.name());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s '%s' found for permId '%s'.", entityKind.name(),
                    entity, permId));
        }
        return entity;
    }

}
