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

package ch.systemsx.cisd.openbis.uitest.suite.main;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.dsl.IdentifiedBy;
import ch.systemsx.cisd.openbis.uitest.type.GeneralBatchImportFile;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class GeneralBatchImportUpdateTest extends MainSuite
{

    @Test
    public void propertiesOfSampleIdentifiedWithSpaceAndCodeCanBeUpdated() throws Exception
    {
        Sample sample = create(aSample().ofType(basic).withProperty(propertyType, randomValue()));
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace));

        String newValue = randomValue();
        create(in(file), anUpdateOf(sample).settingProperty(propertyType, newValue),
                IdentifiedBy.SPACE_AND_CODE);

        generalBatchImport(file, null);

        assertThat(browserEntryOf(sample), containsValue(propertyType.getLabel(), newValue));
    }

    @Test
    public void propertiesOfSampleSampleIdentifiedWithCodeAndDefaultSpaceCanBeUpdated()
            throws Exception
    {
        Sample sample =
                create(aSample().ofType(basic).in(sampleSpace).withProperty(propertyType,
                        randomValue()));

        String newValue = randomValue();

        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(sampleSpace));

        create(in(file), anUpdateOf(sample).settingProperty(propertyType, newValue),
                IdentifiedBy.CODE);

        generalBatchImport(file, null);

        assertThat(browserEntryOf(sample), containsValue(propertyType.getLabel(), newValue));
    }

    @Test
    public void propertiesOfSampleIdentifiedWithCodeAndHomeSpaceCanBeUpdated() throws Exception
    {
        Sample sampleInHomeSpace = create(aSample().ofType(basic).in(homeSpace)
                .withProperty(propertyType, randomValue()));

        String newValue = randomValue();

        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile());

        create(in(file), anUpdateOf(sampleInHomeSpace).settingProperty(propertyType, newValue),
                IdentifiedBy.CODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(sampleInHomeSpace), containsValue(propertyType.getLabel(),
                newValue));
    }

    @Test
    public void propertiesOfComponentSampleIdentifiedWithSpaceAndCodeSubcodeCanBeUpdated()
            throws Exception
    {
        Sample container = create(aSample().ofType(basic).in(sampleSpace));
        Sample component =
                create(aSample().ofType(componentType).containedBy(container).in(sampleSpace)
                        .withProperty(propertyType, randomValue()));

        String newValue = randomValue();

        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace)
                        .withoutSampleContainerColumn());

        create(in(file), anUpdateOf(component).settingProperty(propertyType, newValue),
                IdentifiedBy.SPACE_AND_CODE_AND_SUBCODE);

        generalBatchImport(file, null);

        assertThat(browserEntryOf(component), containsValue(propertyType.getLabel(), newValue));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Test
    public void propertiesOfComponentSampleIdentifiedWithSpaceAndCodeAndContainerColumnCanBeUpdated()
            throws Exception
    {
        Sample container = create(aSample().ofType(basic));
        Sample component =
                create(aSample().ofType(componentType).containedBy(container).withProperty(
                        propertyType, randomValue()));

        String newValue = randomValue();

        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace)
                        .withSampleContainerColumn());

        create(in(file), anUpdateOf(component).settingProperty(propertyType, newValue),
                IdentifiedBy.SPACE_AND_CODE);

        generalBatchImport(file, null);

        assertThat(browserEntryOf(component), containsValue(propertyType.getLabel(), newValue));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Override
    public void fixture()
    {
        defaultSpace = create(aSpace().withCodePrefix("DEFAULT_SPACE"));
        sampleSpace = create(aSpace().withCodePrefix("SAMPLE_SPACE"));
        homeSpace = create(aSpace().withCodePrefix("HOME_SPACE"));
        componentSpace = create(aSpace().withCodePrefix("COMPONENT_SPACE"));

        basic = create(aSampleType().withCodePrefix("BASIC"));
        componentType = create(aSampleType().thatCanBeComponent().withCodePrefix("COMPONENT"));

        withHomeSpace = using(publicApi(), create(aUser().withHomeSpace(homeSpace)));
        withoutHomeSpace = using(publicApi(), create(aUser()));

        propertyType = create(aVarcharPropertyType());

        create(aSamplePropertyTypeAssignment()
                .with(basic)
                .with(propertyType));

        create(aSamplePropertyTypeAssignment()
                .with(componentType)
                .with(propertyType));

    }

    SampleType basic;

    SampleType componentType;

    Space sampleSpace;

    Space defaultSpace;

    Space homeSpace;

    Space componentSpace;

    User withHomeSpace;

    User withoutHomeSpace;

    PropertyType propertyType;

}
