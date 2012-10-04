/*
 * Copyright 2012 ETH Zuerich, CISD
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
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectDAO extends AbstractGenericEntityDAO<MetaprojectPE> implements
        IMetaprojectDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MetaprojectDAO.class);

    private static final Class<MetaprojectPE> ENTITY_CLASS = MetaprojectPE.class;

    public MetaprojectDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    @Override
    public List<MetaprojectPE> listMetaprojects(PersonPE owner)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(MetaprojectPE.class);
        criteria.add(Restrictions.eq("owner", owner));
        final List<MetaprojectPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d metaproject(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), owner, list.size()));
        }
        return list;
    }

    @Override
    public void createOrUpdateMetaproject(MetaprojectPE metaproject)
    {
        assert metaproject != null : "Missing metaproject.";
        validatePE(metaproject);

        metaproject.setName(CodeConverter.tryToDatabase(metaproject.getName()));
        metaproject.setPrivate(true);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(metaproject);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE: metaproject '%s'.", metaproject));
        }
    }
}
