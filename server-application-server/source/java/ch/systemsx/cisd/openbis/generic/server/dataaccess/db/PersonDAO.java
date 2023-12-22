/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Implementation of {@link IPersonDAO} for databases.
 * 
 * @author Franz-Josef Elmer
 */
public final class PersonDAO extends AbstractGenericEntityDAO<PersonPE> implements IPersonDAO
{
    private static final Class<PersonPE> ENTITY_CLASS = PersonPE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    public static final String ACTIVE_PERSONS_QUERY =
            "select count(*) from (                                                       "
                    + "    select distinct p.user_id from persons p                       "
                    + "    where p.is_active = true                                       "
                    + "  union                                                            "
                    + "    select distinct p.user_id from persons p                       "
                    + "      left join role_assignments ra on ra.pers_id_grantee=p.id     "
                    + "    where ra.role_code != 'ETL_SERVER'                             "
                    + "  union                                                            "
                    + "    select distinct p.user_id from persons p                       "
                    + "      left join authorization_group_persons agp on agp.pers_id=p.id"
                    + "      left join authorization_groups ag on ag.id=agp.ag_id         "
                    + "      left join role_assignments ra on ra.ag_id_grantee=ag.id      "
                    + "    where ra.role_code != 'ETL_SERVER'                             "
                    + ") as active_users                                                  ";

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    public static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PersonDAO.class);

    PersonDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, ENTITY_CLASS, historyCreator);
    }

    //
    // IPersonDAO
    //

    @Override
    public final void createPerson(final PersonPE person) throws DataAccessException
    {
        assert person != null : "Given person can not be null.";
        person.setEmail(StringUtils.trim(person.getEmail()));
        person.setActive(true);
        validatePE(person);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(person);
        template.flush();
        if (person.getPersonDisplaySettings() != null)
        {
            person.getPersonDisplaySettings().setId(person.getId());
            template.update(person.getPersonDisplaySettings());
            template.flush();
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: person '%s'.", person));
        }
    }

    @Override
    public final void updatePerson(final PersonPE person) throws DataAccessException
    {
        assert person != null : "Given person can not be null.";
        validatePE(person);

        final HibernateTemplate template = getHibernateTemplate();
        if (person.getPersonDisplaySettings() != null)
        {
            if (person.getPersonDisplaySettings().getId() == null)
            {
                person.getPersonDisplaySettings().setId(person.getId());
            }
            template.merge(person.getPersonDisplaySettings()); // cannot be update - look below
        }
        template.merge(person); // WORKAROUND update cannot be used - see LMS-1603
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: person '%s'.", person));
        }
    }

    @Override
    public final PersonPE getPerson(final long id) throws DataAccessException
    {
        final PersonPE person = (PersonPE) getHibernateTemplate().load(ENTITY_CLASS, id);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("getPerson(" + id + "): '" + person + "'.");
        }
        return person;
    }

    @Override
    public List<PersonPE> getPersons(Collection<Long> ids) throws DataAccessException {
        final List<PersonPE> list = DAOUtils.listByCollection(getHibernateTemplate(), ENTITY_CLASS, "id", ids);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d persons(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public final PersonPE tryFindPersonByUserId(final String userId) throws DataAccessException
    {
        assert userId != null : "Unspecified user id";

        final List<PersonPE> persons =
                cast(getHibernateTemplate().find(
                        String.format("from %s p where lower(p.userId) = ? ", TABLE_NAME),
                        toArray(userId.toLowerCase())));
        final PersonPE person = tryFindPerson(persons, userId);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), userId, person));
        }
        return person;
    }

    /**
     * Checks given <var>persons</var> and throws a {@link IncorrectResultSizeDataAccessException} if it contains more than one item and no person is
     * found that exactly matches the <var>userId</var>.
     * 
     * @return <code>null</code> or the entity found at index <code>0</code>.
     */
    private final static PersonPE tryFindPerson(final List<PersonPE> persons, final String userId)
            throws IncorrectResultSizeDataAccessException
    {
        final int size = persons.size();
        switch (size)
        {
            case 0:
                return null;
            case 1:
                return persons.get(0);
            default:
                for (PersonPE p : persons)
                {
                    if (p.getUserId().equals(userId))
                    {
                        return p;
                    }
                }
                throw new IncorrectResultSizeDataAccessException(String.format(
                        "%d persons found for user id '%s'. Expected: 1 or 0.", size, userId), 1,
                        size);
        }
    }

    @Override
    public final PersonPE tryFindPersonByEmail(final String emailAddress)
            throws DataAccessException
    {
        assert emailAddress != null : "Unspecified email address";

        // Can't limit the number of results directly in the query because we are using a shared
        // hibernate template
        final List<PersonPE> persons =
                cast(getHibernateTemplate().find(
                        String.format(
                                "from %s p where p.email = ?",
                                TABLE_NAME),
                        toArray(emailAddress)));
        int numberOfResults = persons.size();
        final PersonPE person;
        // Take the first result
        if (numberOfResults > 0)
        {
            person = persons.get(0);
        } else
        {
            person = null;
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d found, taking '%s'.", MethodUtils
                    .getCurrentMethod().getName(), emailAddress, numberOfResults, person));
        }
        return person;
    }

    @Override
    public final List<PersonPE> listPersons() throws DataAccessException
    {
        final List<PersonPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s p", TABLE_NAME)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d person(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public final List<PersonPE> listActivePersons() throws DataAccessException
    {
        final List<PersonPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s p where p.active = true",
                                TABLE_NAME)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d person(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public final int countActivePersons() throws DataAccessException
    {
        return ((BigInteger) executeStatelessAction(new StatelessHibernateCallback()
            {
                @Override
                public Object doInStatelessSession(StatelessSession session)
                {
                    return session.createSQLQuery(ACTIVE_PERSONS_QUERY).uniqueResult();
                }
            })).intValue();
    }

    @Override
    public final List<PersonPE> listByCodes(Collection<String> userIds) throws DataAccessException
    {
        if (userIds.size() == 0)
            return new ArrayList<PersonPE>();
        final Criteria criteria = currentSession().createCriteria(PersonPE.class);
        criteria.add(Restrictions.in("userId", userIds));
        return cast(criteria.list());
    }

    @Override
    public void lock(PersonPE person)
    {
        if(person != null && person.getId() != null)
        {
            currentSession().createNativeQuery("SELECT * FROM " + TableNames.PERSONS_TABLE + " WHERE id = " + person.getId() + " FOR NO KEY UPDATE");
        }
    }
}
