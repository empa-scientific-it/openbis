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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs.MODIFICATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.IS_MANAGED_INTERNALLY;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.SHOW_IN_CHOOSERS;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyGridColumnIDs.URL_TEMPLATE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * @author Kaloyan Enimanev
 */
public class VocabulariesProvider extends AbstractCommonTableModelProvider<Vocabulary>
{

    private final boolean withTerms;

    private final boolean excludeInternal;

    public VocabulariesProvider(ICommonServer commonServer, String sessionToken, boolean withTerms,
            boolean excludeInternal)
    {
        super(commonServer, sessionToken);
        this.withTerms = withTerms;
        this.excludeInternal = excludeInternal;
    }

    @Override
    protected TypedTableModel<Vocabulary> createTableModel()
    {
        List<Vocabulary> vocabularies =
                commonServer.listVocabularies(sessionToken, withTerms, excludeInternal);

        TypedTableModelBuilder<Vocabulary> builder = new TypedTableModelBuilder<Vocabulary>();
        builder.addColumn(CODE).withDefaultWidth(200);
        builder.addColumn(DESCRIPTION).withDefaultWidth(300);
        builder.addColumn(IS_MANAGED_INTERNALLY).withDefaultWidth(150);
        builder.addColumn(REGISTRATOR).withDefaultWidth(150);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300);
        builder.addColumn(MODIFICATION_DATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(URL_TEMPLATE).withDefaultWidth(300).hideByDefault();
        builder.addColumn(SHOW_IN_CHOOSERS).withDefaultWidth(150).hideByDefault();

        for (Vocabulary vocabulary : vocabularies)
        {
            builder.addRow(vocabulary);
            builder.column(CODE).addString(vocabulary.getCode());
            builder.column(DESCRIPTION).addString(vocabulary.getDescription());
            builder.column(IS_MANAGED_INTERNALLY).addString(
                    SimpleYesNoRenderer.render(vocabulary.isManagedInternally()));
            builder.column(REGISTRATOR).addPerson(vocabulary.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(vocabulary.getRegistrationDate());
            builder.column(MODIFICATION_DATE).addDate(vocabulary.getModificationDate());
            builder.column(URL_TEMPLATE).addString(vocabulary.getURLTemplate());
            builder.column(SHOW_IN_CHOOSERS).addString(
                    SimpleYesNoRenderer.render(vocabulary.isChosenFromList()));
        }
        return builder.getModel();
    }

}
