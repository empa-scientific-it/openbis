/*
 * Copyright 2011 ETH Zuerich, CISD
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

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.ICorePluginDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;

/**
 * Hibernate-based implementation of {@link ICorePluginDAO}.
 * 
 * @author Kaloyan Enimanev
 */
public class CorePluginDAO extends AbstractDAO implements ICorePluginDAO
{
    private final static Class<CorePluginPE> ENTITY_CLASS = CorePluginPE.class;

    public CorePluginDAO(SessionFactory sessionFactory)
    {
        super(sessionFactory);
    }

    @Override
    public void createCorePlugins(List<CorePluginPE> corePlugins)
    {
        HibernateTemplate template = getHibernateTemplate();
        for (CorePluginPE plugin : corePlugins)
        {
            template.saveOrUpdate(plugin);
        }
        template.flush();
    }

    @Override
    public List<CorePluginPE> listCorePluginsByName(String name)
    {
        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("name", name));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return cast(criteria.list());
    }

}
