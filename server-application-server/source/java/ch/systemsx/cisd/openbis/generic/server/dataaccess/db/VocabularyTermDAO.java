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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.support.JdbcAccessor;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * <i>Data Access Object</i> implementation for {@link VocabularyTermPE}.
 * 
 * @author Piotr Buczek
 */
final class VocabularyTermDAO extends AbstractGenericEntityDAO<VocabularyTermPE> implements
        IVocabularyTermDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     */
    @SuppressWarnings("unused")
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            VocabularyTermDAO.class);

    VocabularyTermDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, VocabularyTermPE.class, historyCreator);
    }

    @Override
    public void increaseVocabularyTermOrdinals(VocabularyPE vocabulary, Long fromOrdinal,
            int increment)
    {
        assert vocabulary != null : "Unspecified vocabulary.";
        assert fromOrdinal != null : "Unspecified ordinal.";

        // We could use HQL like in EntityPropertyTypeDAO.increaseOrdinals() instead of SQL
        // but the connection between terms and vocabulary would need to be bidirectional.
        Long vocabularyId = HibernateUtils.getId(vocabulary);
        executeUpdate("UPDATE " + TableNames.CONTROLLED_VOCABULARY_TERM_TABLE
                + " SET ordinal = ordinal + ?" + " WHERE covo_id = ? AND ordinal >= ?", increment,
                vocabularyId, fromOrdinal);
    }

    @Override
    public long getMaximumOrdinal(VocabularyPE vocabulary)
    {
        Long vocabularyId = HibernateUtils.getId(vocabulary);
        Number max =
                (Number) getUniqueResult("select max(ordinal) from "
                        + TableNames.CONTROLLED_VOCABULARY_TERM_TABLE + " where covo_id = %s",
                        vocabularyId);
        return max == null ? 0 : max.longValue();
    }

    @Override
    public void updateRegistrator(VocabularyTermPE term, PersonPE registrator)
    {
        Long termId = HibernateUtils.getId(term);
        Long registratorId = HibernateUtils.getId(registrator);

        term.setRegistrator(registrator);
        // The "registrator" field is normally not updatable via Hibernate, therefore a native SQL query has to be used for rare special cases.
        executeUpdate("UPDATE " + TableNames.CONTROLLED_VOCABULARY_TERM_TABLE + " SET pers_id_registerer = ? WHERE id = ?", registratorId, termId);
    }
}
