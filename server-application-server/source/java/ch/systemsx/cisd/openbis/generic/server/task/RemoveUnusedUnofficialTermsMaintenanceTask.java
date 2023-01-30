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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;

/**
 * @author Pawel Glyzewski
 */
public class RemoveUnusedUnofficialTermsMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RemoveUnusedUnofficialTermsMaintenanceTask.class);

    private static final String OLDER_THAN_DAYS_PROPERTY_NAME = "older-than-days";

    private static final Double DEFAULT_OLDER_THAN_DAYS = 7.0;

    private long olderThan;

    private final double day = 86400000.0;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        double olderThanDouble =
                PropertyUtils.getDouble(properties, OLDER_THAN_DAYS_PROPERTY_NAME,
                        DEFAULT_OLDER_THAN_DAYS);
        this.olderThan = Math.round(olderThanDouble * day); // in milliseconds
        operationLog.info("Unused unofficial terms older than " + olderThanDouble + " days ("
                + olderThan + " milliseconds) will be removed.");
    }

    @Override
    public void execute()
    {
        ICommonServerForInternalUse server = CommonServiceProvider.getCommonServer();
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull != null)
        {
            final String sessionToken = contextOrNull.getSessionToken();
            List<Vocabulary> vocabularies = server.listVocabularies(sessionToken, false, true);
            for (Vocabulary vocabulary : vocabularies)
            {
                List<VocabularyTermWithStats> termsWithStats =
                        server.listVocabularyTermsWithStatistics(sessionToken, vocabulary);

                List<VocabularyTerm> termsToBeDeleted = new ArrayList<VocabularyTerm>();
                for (VocabularyTermWithStats term : termsWithStats)
                {
                    if (!term.getTerm().isOfficial()
                            && new Date().getTime()
                                    - term.getTerm().getRegistrationDate().getTime() > olderThan)
                    {
                        int usage = 0;
                        for (EntityKind entityKind : EntityKind.values())
                        {
                            usage += term.getUsageCounter(entityKind);
                        }

                        if (usage == 0)
                        {
                            VocabularyTerm vocabularyTerm =
                                    VocabularyTermTranslator.translate(term.getTerm());
                            operationLog.info("Term '" + vocabularyTerm + "' will be deleted.");
                            termsToBeDeleted.add(vocabularyTerm);
                        }
                    }
                }
                if (termsToBeDeleted.size() > 0)
                {
                    server.deleteVocabularyTerms(sessionToken, TechId.create(vocabulary),
                            termsToBeDeleted, new ArrayList<VocabularyTermReplacement>());
                }
            }
        } else
        {
            operationLog.error("authentication failed");
        }
        operationLog.info("task executed");
    }

}
