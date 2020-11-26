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

import ch.systemsx.cisd.openbis.uitest.dsl.CommandNotSuccessful;
import ch.systemsx.cisd.openbis.uitest.dsl.IdentifiedBy;
import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.type.GeneralBatchImportFile;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class GeneralBatchImportRegistrationTest extends MainSuite
{

    private static final String SAME_SUBCODE_ERROR_MSG =
            "failed because sample of the same type with the same subcode already exists.";

    @Test
    public void spaceOfSampleIdentifiedWithSpaceAndCodeIsDefinedByIdentifier()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace));

        Sample sample =
                create(in(file), aSample().in(sampleSpace).ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(sample), hasSpace(sampleSpace));
    }

    @Test
    public void spaceOfSampleIdentifiedWithCodeIsDefinedByDefaultSpaceOfImportFile()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile().withDefaultSpace(defaultSpace));
        Sample sample = create(in(file), aSample().ofType(basic), IdentifiedBy.CODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(sample), hasSpace(defaultSpace));
    }

    @Test
    public void spaceOfSampleIdentifiedWithCodeIsDefinedByUserHomeSpaceIfDefaultSpaceOfImportFileIsNotSet()
            throws Exception
    {
        GeneralBatchImportFile file = create(aGeneralBatchImportFile());
        Sample sample = create(in(file), aSample().ofType(basic), IdentifiedBy.CODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(sample), hasSpace(homeSpace));
    }

    @Test(expectedExceptions = CommandNotSuccessful.class)
    public void importFailsIfSampleIsIdentifiedWithCodeAndDefaultSpaceOfImportFileIsNotSetAndHomeSpaceIsNotSet()
            throws Exception
    {
        GeneralBatchImportFile file = create(aGeneralBatchImportFile());
        create(in(file), aSample().ofType(basic), IdentifiedBy.CODE);

        // TODO
        // This is really makes the test hard to read. We need a way to handle this transparently.
        try
        {
            as(user(withoutHomeSpace), generalBatchImport(file, SAME_SUBCODE_ERROR_MSG));
        } finally
        {
            if (SeleniumTest.ADMIN_USER.equals(loggedInAs()) == false)
            {
                user(assume(aUser().withName(SeleniumTest.ADMIN_USER)));
            }
        }
    }

    @Test
    public void spaceOfComponentSampleIdentifiedWithSpaceAndCodeAndContainerColumnIsDefinedByComponentIdentifier()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withDefaultSpace(defaultSpace)
                        .withSampleContainerColumn());

        Sample container =
                create(in(file),
                        aSample()
                                .in(sampleSpace)
                                .ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        Sample component =
                create(in(file),
                        aSample()
                                .in(componentSpace)
                                .ofType(componentType)
                                .containedBy(container),
                        IdentifiedBy.SPACE_AND_CODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(container), hasSpace(sampleSpace));
        assertThat(browserEntryOf(component), hasSpace(componentSpace));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Test
    public void spaceOfComponentSampleIdentifiedWithSpaceAndCodeAndSubcodeIsDefinedByIdentifier()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withDefaultSpace(defaultSpace));

        Sample container =
                create(in(file),
                        aSample()
                                .in(sampleSpace)
                                .ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        Sample component =
                create(in(file),
                        aSample()
                                .ofType(componentType)
                                .containedBy(container)
                                .in(sampleSpace),
                        IdentifiedBy.SPACE_AND_CODE_AND_SUBCODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(component), hasSpace(sampleSpace));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Test(expectedExceptions = CommandNotSuccessful.class)
    public void importFailsIfComponentIsDefinedBySpaceAndCodeAndSubcodeAndContainerColumnExists()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withSampleContainerColumn()
                        .withDefaultSpace(defaultSpace));

        Sample container =
                create(in(file),
                        aSample().in(sampleSpace).ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        create(in(file),
                aSample().ofType(componentType).in(sampleSpace).containedBy(container).in(
                        sampleSpace),
                IdentifiedBy.SPACE_AND_CODE_AND_SUBCODE);

        generalBatchImport(file, SAME_SUBCODE_ERROR_MSG);
    }

    @Test
    public void spaceOfComponentSampleIdentifiedWithSubcodeAndContainerColumnIsSetToDefaultSpaceOfImportFile()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withSampleContainerColumn()
                        .withDefaultSpace(defaultSpace));

        Sample container =
                create(in(file),
                        aSample().in(sampleSpace).ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        Sample component =
                create(in(file),
                        aSample().ofType(componentType).containedBy(container),
                        IdentifiedBy.SUBCODE);

        as(user(withHomeSpace), generalBatchImport(file, null));

        assertThat(browserEntryOf(component), hasSpace(defaultSpace));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Test
    public void containerColumnCanContainOnlyCodeOfContainerIfDefaultSpaceOfImportFileIsSet()
            throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withSampleContainerColumn()
                        .withDefaultSpace(defaultSpace));

        Sample container =
                create(in(file),
                        aSample().ofType(basic).in(defaultSpace),
                        IdentifiedBy.CODE);

        Sample component =
                create(in(file),
                        aSample().ofType(componentType).containedBy(container),
                        IdentifiedBy.SUBCODE);

        generalBatchImport(file, null);

        assertThat(browserEntryOf(component), hasSpace(defaultSpace));
        assertThat(browserEntryOf(component), hasContainer(container));
    }

    @Test(expectedExceptions = CommandNotSuccessful.class)
    public void importFailsIfSampleIsIdentifiedWithCodeAndSubcode() throws Exception
    {
        GeneralBatchImportFile file =
                create(aGeneralBatchImportFile()
                        .withDefaultSpace(defaultSpace));

        Sample container =
                create(in(file),
                        aSample().in(sampleSpace).ofType(basic),
                        IdentifiedBy.SPACE_AND_CODE);

        create(in(file),
                aSample().ofType(componentType).containedBy(container),
                IdentifiedBy.CODE_AND_SUBCODE);

        generalBatchImport(file, SAME_SUBCODE_ERROR_MSG);
    }

    @Test(expectedExceptions = CommandNotSuccessful.class)
    public void importFailsIfComponentSampleTypeHasCodeUniquenessAttributeSetAndSameSpaceHasAnyTwoComponentsWithSameSubcode()
            throws Exception
    {
        SampleType uniqueSubCodesType =
                create(aSampleType().thatCanBeComponent().thatHasUniqueSubcodes());

        GeneralBatchImportFile file = create(aGeneralBatchImportFile().withSampleContainerColumn());
        Sample container1 = create(in(file), aSample().ofType(basic).in(sampleSpace));
        Sample container2 = create(in(file), aSample().ofType(basic).in(sampleSpace));
        create(in(file), aSample().ofType(uniqueSubCodesType).withCode("subcode").containedBy(
                container1).in(componentSpace));
        create(in(file), aSample().ofType(uniqueSubCodesType).withCode("subcode").containedBy(
                container2).in(componentSpace));

        generalBatchImport(file, SAME_SUBCODE_ERROR_MSG);
    }

    @Test
    public void propertiesOfSamplesAreImported() throws Exception
    {
        SampleType sampleType = create(aSampleType());
        PropertyType propertyType = create(aVarcharPropertyType());
        create(aSamplePropertyTypeAssignment().with(sampleType).with(propertyType));

        String propertyValue = randomValue();

        GeneralBatchImportFile file = create(aGeneralBatchImportFile());
        Sample sample =
                create(in(file), aSample().ofType(sampleType).withProperty(propertyType,
                        propertyValue).in(sampleSpace));
        generalBatchImport(file, null);

        assertThat(browserEntryOf(sample), containsValue(propertyType.getLabel(), propertyValue));
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
    }

    SampleType basic;

    SampleType componentType;

    Space sampleSpace;

    Space defaultSpace;

    Space homeSpace;

    Space componentSpace;

    User withHomeSpace;

    User withoutHomeSpace;
}
