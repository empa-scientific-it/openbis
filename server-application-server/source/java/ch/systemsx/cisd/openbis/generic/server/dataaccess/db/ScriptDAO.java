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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * <i>Data Access Object</i> implementation for {@link ScriptPE}.
 * 
 * @author Izabela Adamczyk
 */
final class ScriptDAO extends AbstractGenericEntityDAO<ScriptPE> implements IScriptDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ScriptDAO.class);

    ScriptDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, ScriptPE.class, historyCreator);
    }

    @Override
    public void createOrUpdate(ScriptPE script)
    {
        assert script != null : "Missing script.";
        validatePE(script);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(script);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE: script '%s'.", script));
        }
    }

    @Override
    public ScriptPE tryFindByName(String scriptName)
    {
        assert scriptName != null : "Unspecified script.";

        final Criteria criteria = currentSession().createCriteria(ScriptPE.class);
        criteria.add(Restrictions.eq("name", scriptName));
        return (ScriptPE) criteria.uniqueResult();
    }

    @Override
    public List<ScriptPE> listEntities(ScriptType scriptTypeOrNull, EntityKind entityKindOrNull)
            throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(ScriptPE.class);
        if (scriptTypeOrNull != null)
        {
            criteria.add(Restrictions.or(Restrictions.isNull("scriptType"),
                    Restrictions.eq("scriptType", scriptTypeOrNull)));
        }
        if (entityKindOrNull != null)
        {
            criteria.add(Restrictions.or(Restrictions.isNull("entityKind"),
                    Restrictions.eq("entityKind", entityKindOrNull)));
        }
        final List<ScriptPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d scripts(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), entityKindOrNull, list.size()));
        }
        return list;
    }
}
