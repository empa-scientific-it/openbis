/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.CompleteSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.ContentCopySearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.DataSetSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.FileFormatTypeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.LinkedDataSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.LocatorTypeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.PhysicalDataSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.StatusSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.StorageFormatSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.search.ExperimentSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms.search.ExternalDmsSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.search.MaterialSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.search.ModifierSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.search.RegistratorSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project.search.ProjectSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.search.SampleSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.search.SpaceSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.tag.search.TagSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
public class SearchCriteriaTranslatorFactory extends AbstractSearchCriteriaTranslatorFactory
{

    private IDAOFactory daoFactory;

    private IObjectAttributeProviderFactory entityAttributeProviderFactory;

    public SearchCriteriaTranslatorFactory(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        this.daoFactory = daoFactory;
        this.entityAttributeProviderFactory = entityAttributeProviderFactory;
    }

    @Override
    protected List<ISearchCriteriaTranslator> createTranslators()
    {
        List<ISearchCriteriaTranslator> translators = new LinkedList<ISearchCriteriaTranslator>();
        translators.add(new TagSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new RegistratorSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ModifierSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new SpaceSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ProjectSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ExperimentSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new SampleSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new DataSetSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new PhysicalDataSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new LinkedDataSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new StorageFormatSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new LocatorTypeSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new FileFormatTypeSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new CompleteSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new StatusSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ExternalDmsSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new MaterialSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new StringFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new DateFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new NumberFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new BooleanFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new EnumFieldSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new EntityTypeSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new ContentCopySearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        translators.add(new CollectionSearchCriteriaTranslator(getDaoFactory(), getEntityAttributeProviderFactory()));
        return translators;
    }

    public IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    public IObjectAttributeProviderFactory getEntityAttributeProviderFactory()
    {
        return entityAttributeProviderFactory;
    }

}
