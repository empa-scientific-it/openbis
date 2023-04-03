/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class SampleTypeWithVocabularyPropertyExpectations extends Expectations
{

    public SampleTypeWithVocabularyPropertyExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getSampleTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE)))),
                with(any(SampleTypeFetchOptions.class)));

        will(new CustomAction("getting sample types 1")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final Calendar calendar = Calendar.getInstance();

                calendar.set(2023, Calendar.MARCH, 11, 17, 23, 44);
                final Date modificationDate = calendar.getTime();

                final SampleTypeFetchOptions fetchOptions = (SampleTypeFetchOptions) invocation.getParameter(2);
                final SampleType sampleType = new SampleType();
                sampleType.setFetchOptions(fetchOptions);
                sampleType.setPermId(new EntityTypePermId("ANTIBODY", EntityKind.SAMPLE));
                sampleType.setCode("ANTIBODY");
                sampleType.setDescription("Antibody");
                sampleType.setAutoGeneratedCode(true);
                sampleType.setGeneratedCodePrefix("ANT");
                sampleType.setSubcodeUnique(false);
                sampleType.setModificationDate(modificationDate);
                sampleType.setPropertyAssignments(getPropertyAssignments(fetchOptions));
                return Collections.singletonMap(sampleType.getPermId(), sampleType);
            }

        });

        allowing(api).getSampleTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(new EntityTypePermId("VIRUS", EntityKind.SAMPLE)))),
                with(any(SampleTypeFetchOptions.class)));

        will(new CustomAction("getting sample types 2")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final Calendar calendar = Calendar.getInstance();

                calendar.set(2023, Calendar.MARCH, 11, 17, 23, 44);
                final Date modificationDate = calendar.getTime();

                final SampleTypeFetchOptions fetchOptions = (SampleTypeFetchOptions) invocation.getParameter(2);
                final SampleType sampleType = new SampleType();
                sampleType.setFetchOptions(fetchOptions);
                sampleType.setPermId(new EntityTypePermId("VIRUS", EntityKind.SAMPLE));
                sampleType.setCode("VIRUS");
                sampleType.setDescription("Virus");
                sampleType.setAutoGeneratedCode(true);
                sampleType.setGeneratedCodePrefix("VIR");
                sampleType.setSubcodeUnique(false);
                sampleType.setModificationDate(modificationDate);
                sampleType.setPropertyAssignments(getPropertyAssignments(fetchOptions));
                return Collections.singletonMap(sampleType.getPermId(), sampleType);
            }

        });

        if (exportReferred)
        {
            allowing(api).getVocabularies(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                            Collections.singletonList(new VocabularyPermId("ANTIBODY.HOST")))),
                    with(any(VocabularyFetchOptions.class)));

            will(new CustomAction("getting dependent vocabularies")
            {

                @Override
                public Object invoke(final Invocation invocation) throws Throwable
                {
                    final VocabularyFetchOptions fetchOptions = (VocabularyFetchOptions) invocation.getParameter(2);

                    final Vocabulary vocabulary = new Vocabulary();
                    vocabulary.setFetchOptions(fetchOptions);
                    vocabulary.setCode("ANTIBODY.HOST");
                    vocabulary.setDescription("Host organism");

                    vocabulary.setTerms(getVocabularyTerms(fetchOptions));

                    return Collections.singletonMap(new EntityTypePermId("ANTIBODY.HOST"), vocabulary);
                }

                private List<VocabularyTerm> getVocabularyTerms(final VocabularyFetchOptions fetchOptions)
                {
                    final VocabularyTermFetchOptions vocabularyTermFetchOptions = fetchOptions.withTerms();

                    final VocabularyTerm[] vocabularyTerms = new VocabularyTerm[3];

                    vocabularyTerms[0] = new VocabularyTerm();
                    vocabularyTerms[0].setFetchOptions(vocabularyTermFetchOptions);
                    vocabularyTerms[0].setCode("MOUSE");
                    vocabularyTerms[0].setLabel("mouse");
                    vocabularyTerms[0].setDescription("mouse");

                    vocabularyTerms[1] = new VocabularyTerm();
                    vocabularyTerms[1].setFetchOptions(vocabularyTermFetchOptions);
                    vocabularyTerms[1].setCode("RAT");
                    vocabularyTerms[1].setLabel("rat");
                    vocabularyTerms[1].setDescription("rat");

                    vocabularyTerms[2] = new VocabularyTerm();
                    vocabularyTerms[2].setFetchOptions(vocabularyTermFetchOptions);
                    vocabularyTerms[2].setCode("GUINEA_PIG");
                    vocabularyTerms[2].setLabel("guinea pig");
                    vocabularyTerms[2].setDescription("guinea pig");

                    return Arrays.asList(vocabularyTerms);
                }

            });
        }
    }

    private List<PropertyAssignment> getPropertyAssignments(final SampleTypeFetchOptions fetchOptions)
    {
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions =
                fetchOptions.withPropertyAssignments();
        final PropertyTypeFetchOptions propertyTypeFetchOptions =
                propertyAssignmentFetchOptions.withPropertyType();
        propertyTypeFetchOptions.withVocabulary();

        final PluginFetchOptions pluginFetchOptions = propertyAssignmentFetchOptions.withPlugin();
        pluginFetchOptions.withScript();

        final PropertyAssignment[] propertyAssignments = new PropertyAssignment[3];

        propertyAssignments[0] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[0].getPropertyType().setCode("NAME");
        propertyAssignments[0].setMandatory(false);
        propertyAssignments[0].setShowInEditView(true);
        propertyAssignments[0].setSection("General info");
        propertyAssignments[0].getPropertyType().setManagedInternally(true);
        propertyAssignments[0].getPropertyType().setLabel("Name");
        propertyAssignments[0].getPropertyType().setDataType(DataType.VARCHAR);
        propertyAssignments[0].getPropertyType().setDescription("Name");

        propertyAssignments[1] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[1].getPropertyType().setCode("BARCODE");
        propertyAssignments[1].setMandatory(false);
        propertyAssignments[1].setShowInEditView(false);
        propertyAssignments[1].setSection("General info");
        propertyAssignments[1].getPropertyType().setManagedInternally(true);
        propertyAssignments[1].getPropertyType().setLabel("Custom Barcode");
        propertyAssignments[1].getPropertyType().setDataType(DataType.VARCHAR);
        propertyAssignments[1].getPropertyType().setDescription("Custom Barcode");

        propertyAssignments[2] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[2].getPropertyType().setCode("ANTIBODY.HOST");
        propertyAssignments[2].setMandatory(false);
        propertyAssignments[2].setShowInEditView(true);
        propertyAssignments[2].setSection("General info");
        propertyAssignments[2].getPropertyType().setManagedInternally(false);
        propertyAssignments[2].getPropertyType().setLabel("Antibody host");
        propertyAssignments[2].getPropertyType().setDataType(DataType.CONTROLLEDVOCABULARY);
        final Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode("ANTIBODY.HOST");
        propertyAssignments[2].getPropertyType().setVocabulary(vocabulary);
        propertyAssignments[2].getPropertyType().setDescription("Host used to produce the antibody");

        return Arrays.asList(propertyAssignments);
    }

}
