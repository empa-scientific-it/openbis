/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.MenuCategory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.MenuElement;

/**
 * Allows to define categories which will be displayed in {@link LeftMenu}.
 * 
 * @author Izabela Adamczyk
 */
public class CategoriesBuilder
{
    private static final String IMPORT_LABEL = "Import";

    private static final String LABEL_REGISTER = "New";

    private static final String LABEL_BROWSE = "Browse";

    private static final String BROWSE_TYPES_LABEL = "Types";

    public static enum MenuCategoryKind
    {
        VOCABULARIES, MATERIALS, EXPERIMENTS, PERSONS, PROJECTS, GROUPS, ROLES, SAMPLES, DATA_SETS,
        PROPERTY_TYPES;
    }

    public static enum MenuElementKind
    {
        ADD_ROLE, ASSIGN, ASSIGN_STPT, ASSIGN_ETPT, REGISTER, IMPORT, BROWSE, BROWSE_TYPES, MANAGE,
        LIST_ASSIGNMENTS, SEARCH;
    }

    public final ComponentProvider provider;

    public final List<MenuCategory> categories;

    CategoriesBuilder(final ComponentProvider provider)
    {
        this.provider = provider;
        categories = new ArrayList<MenuCategory>();
        defineCategories();
    }

    private void defineCategories()
    {
        categories.add(createSampleCategory());
        categories.add(createExperimentCategory());
        categories.add(createMaterialCategory());
        categories.add(createPropertyTypesCategory());
        categories.add(createVocabulariesCategory());
        categories.add(createProjectsTypesCategory());
        categories.add(createDataSetsCategory());
        categories.add(createGroupsCategory());
        categories.add(createPersonsCategory());
        categories.add(createRolesCategory());
    }

    private MenuCategory createSampleCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getSampleBrowser()));
        elements.add(new MenuElement(MenuElementKind.REGISTER, LABEL_REGISTER, provider
                .getSampleRegistration()));
        elements.add(new MenuElement(MenuElementKind.IMPORT, IMPORT_LABEL, provider
                .getSampleBatchRegistration()));
        elements.add(new MenuElement(MenuElementKind.BROWSE_TYPES, BROWSE_TYPES_LABEL, provider
                .getSampleTypeBrowser()));
        return new MenuCategory(MenuCategoryKind.SAMPLES, "Samples", elements);
    }

    private MenuCategory createRolesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements
                .add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider.getRolesView()));
        return new MenuCategory(MenuCategoryKind.ROLES, "Roles", elements);
    }

    private MenuCategory createGroupsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements
                .add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider.getGroupsView()));
        return new MenuCategory(MenuCategoryKind.GROUPS, "Groups", elements);
    }

    private MenuCategory createProjectsTypesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getProjectBrowser()));
        elements.add(new MenuElement(MenuElementKind.REGISTER, LABEL_REGISTER, provider
                .getProjectRegistration()));
        return new MenuCategory(MenuCategoryKind.PROJECTS, "Projects", elements);
    }

    private MenuCategory createPersonsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getPersonsView()));
        return new MenuCategory(MenuCategoryKind.PERSONS, "Persons", elements);
    }

    private MenuCategory createPropertyTypesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getPropertyTypeBrowser()));
        elements.add(new MenuElement(MenuElementKind.LIST_ASSIGNMENTS, "Browse Assignments",
                provider.getPropertyTypeAssignmentBrowser()));
        elements.add(new MenuElement(MenuElementKind.REGISTER, LABEL_REGISTER, provider
                .getPropertyTypeRegistration()));
        elements.add(new MenuElement(MenuElementKind.ASSIGN_ETPT, "Assign to Experiment Type",
                provider.getPropertyTypeExperimentTypeAssignmentForm()));
        elements.add(new MenuElement(MenuElementKind.ASSIGN_STPT, "Assign to Sample Type", provider
                .getPropertyTypeSampleTypeAssignmentForm()));
        return new MenuCategory(MenuCategoryKind.PROPERTY_TYPES, "Property Types", elements);
    }

    private MenuCategory createDataSetsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements
                .add(new MenuElement(MenuElementKind.SEARCH, "Search", provider.getDataSetSearch()));
        return new MenuCategory(MenuCategoryKind.DATA_SETS, "Data Sets", elements);
    }

    private MenuCategory createVocabulariesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getVocabularyBrowser()));
        elements.add(new MenuElement(MenuElementKind.REGISTER, LABEL_REGISTER, provider
                .getVocabularyRegistration()));
        return new MenuCategory(MenuCategoryKind.VOCABULARIES, "Vocabularies", elements);
    }

    private MenuCategory createMaterialCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getMaterialBrowser()));
        elements.add(new MenuElement(MenuElementKind.IMPORT, IMPORT_LABEL, provider
                .getMaterialBatchRegistration()));
        elements.add(new MenuElement(MenuElementKind.BROWSE_TYPES, BROWSE_TYPES_LABEL, provider
                .getMaterialTypeBrowser()));
        return new MenuCategory(MenuCategoryKind.MATERIALS, "Materials", elements);
    }

    private MenuCategory createExperimentCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MenuElementKind.BROWSE, LABEL_BROWSE, provider
                .getExperimentBrowser()));
        elements.add(new MenuElement(MenuElementKind.REGISTER, LABEL_REGISTER, provider
                .getExperimentRegistration()));
        elements.add(new MenuElement(MenuElementKind.BROWSE_TYPES, BROWSE_TYPES_LABEL, provider
                .getExperimentTypeBrowser()));
        return new MenuCategory(MenuCategoryKind.EXPERIMENTS, "Experiments", elements);
    }

    public List<MenuCategory> getCategories()
    {
        return categories;
    }
}