/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.CommonGridIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.CommonGridIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.CommonGridIDs.LABEL;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.CommonGridIDs.ORDINAL;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.CommonGridIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.IS_OFFICIAL;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.TERM_FOR_DATA_SET_USAGE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.TERM_FOR_EXPERIMENTS_USAGE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.TERM_FOR_MATERIALS_USAGE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.TERM_FOR_SAMPLES_USAGE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.TERM_TOTAL_USAGE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermGridIDs.URL;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class VocabularyTermsProvider extends
        AbstractCommonTableModelProvider<VocabularyTermWithStats>
{
    private final Vocabulary vocabulary;

    public VocabularyTermsProvider(ICommonServer commonServer, String sessionToken,
            Vocabulary vocabulary)
    {
        super(commonServer, sessionToken);
        this.vocabulary = vocabulary;
    }

    @Override
    protected TypedTableModel<VocabularyTermWithStats> createTableModel()
    {
        TypedTableModelBuilder<VocabularyTermWithStats> builder =
                new TypedTableModelBuilder<VocabularyTermWithStats>();
        List<ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats> terms =
                commonServer.listVocabularyTermsWithStatistics(sessionToken, vocabulary);
        builder.addColumn(CODE);
        builder.addColumn(LABEL).withDefaultWidth(200);
        builder.addColumn(DESCRIPTION).withDefaultWidth(300);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(ORDINAL).withDefaultWidth(100).hideByDefault();
        builder.addColumn(URL).withDefaultWidth(200);
        builder.addColumn(IS_OFFICIAL).withDefaultWidth(100).hideByDefault();
        builder.addColumn(REGISTRATOR).withDefaultWidth(200);
        builder.addColumn(TERM_TOTAL_USAGE).withDefaultWidth(100);
        builder.addColumn(TERM_FOR_DATA_SET_USAGE).withDefaultWidth(100).hideByDefault();
        builder.addColumn(TERM_FOR_EXPERIMENTS_USAGE).withDefaultWidth(100).hideByDefault();
        builder.addColumn(TERM_FOR_MATERIALS_USAGE).withDefaultWidth(100).hideByDefault();
        builder.addColumn(TERM_FOR_SAMPLES_USAGE).withDefaultWidth(100).hideByDefault();
        for (VocabularyTermWithStats termWithStats : VocabularyTermTranslator.translate(terms))
        {
            builder.addRow(termWithStats);
            VocabularyTerm term = termWithStats.getTerm();
            builder.column(CODE).addString(term.getCode());
            builder.column(LABEL).addString(term.getLabel());
            builder.column(DESCRIPTION).addString(term.getDescription());
            builder.column(MODIFICATION_DATE).addDate(term.getModificationDate());
            builder.column(ORDINAL).addInteger(term.getOrdinal());
            builder.column(URL).addString(term.getUrl());
            builder.column(IS_OFFICIAL).addString(
                    term.isOfficial() == null ? null
                            : SimpleYesNoRenderer.render(term.isOfficial()));
            builder.column(REGISTRATOR).addPerson(term.getRegistrator());
            builder.column(TERM_TOTAL_USAGE)
                    .addInteger((long) termWithStats.getTotalUsageCounter());
            builder.column(TERM_FOR_DATA_SET_USAGE).addInteger(
                    termWithStats.getUsageCounter(EntityKind.DATA_SET));
            builder.column(TERM_FOR_EXPERIMENTS_USAGE).addInteger(
                    termWithStats.getUsageCounter(EntityKind.EXPERIMENT));
            builder.column(TERM_FOR_MATERIALS_USAGE).addInteger(
                    termWithStats.getUsageCounter(EntityKind.MATERIAL));
            builder.column(TERM_FOR_SAMPLES_USAGE).addInteger(
                    termWithStats.getUsageCounter(EntityKind.SAMPLE));
        }
        return builder.getModel();
    }

}
