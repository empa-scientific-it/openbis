/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;

/**
 * @author Jakub Straszewski
 */
public class FetchOptionsToStringTest extends AssertJUnit
{
    @Test
    public void testEmptySample()
    {
        SampleFetchOptions fe = new SampleFetchOptions();
        assertEquals("Sample\n"
                + "", fe.toString());
    }

    @Test
    public void testSampleAToE()
    {
        SampleFetchOptions fe = new SampleFetchOptions();
        fe.withAttachments();
        fe.withChildren();
        fe.withComponents();
        fe.withContainer();
        fe.withDataSets();
        fe.withExperiment();
        assertEquals("Sample\n" +
                "    with Experiment\n" +
                "    with Children\n" +
                "    with Container\n" +
                "    with Components\n" +
                "    with DataSets\n" +
                "    with Attachments\n" +
                "", fe.toString());

    }

    @Test
    public void testSampleHToZ()
    {
        SampleFetchOptions fe = new SampleFetchOptions();
        fe.withHistory();
        fe.withMaterialProperties();
        fe.withModifier();
        fe.withParents();
        fe.withProperties();
        fe.withRegistrator();
        fe.withTags();
        fe.withType();
        assertEquals("Sample\n" +
                "    with Type\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "    with Parents\n" +
                "    with History\n" +
                "    with Tags\n" +
                "    with Registrator\n" +
                "    with Modifier\n" +
                "", fe.toString());
    }

    @Test
    public void testSampleDeepAndRecursive()
    {
        SampleFetchOptions fe = new SampleFetchOptions();

        fe.withDataSets().withComponents().withSample().withChildren();
        fe.withDataSets().withComponents().withExperiment().withSamples();
        fe.withDataSets().withComponents().withProperties();
        fe.withDataSets().withComponents().withMaterialProperties();
        PersonFetchOptions author = fe.withHistory().withAuthor();
        fe.withMaterialProperties().withProperties();
        fe.withModifierUsing(author);
        fe.withParents();
        fe.withProperties();
        fe.withRegistrator().withSpace().withProjects();
        fe.withTags().withOwnerUsing(author);
        fe.withType();
        fe.sortBy().code().asc();
        fe.sortBy().modificationDate();
        author.withSpace();

        assertEquals("Sample order by CODE ASC, order by MODIFICATION_DATE ASC\n" +
                "    with Type\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "        with Properties\n" +
                "    with Parents\n" +
                "    with DataSets\n" +
                "        with Components\n" +
                "            with Experiment\n" +
                "                with Samples\n" +
                "            with Sample\n" +
                "                with Children\n" +
                "            with Properties\n" +
                "            with MaterialProperties\n" +
                "    with History\n" +
                "        with Author\n" +
                "            with Space\n" +
                "    with Tags\n" +
                "        with Owner(recursive)\n" +
                "    with Registrator\n" +
                "        with Space\n" +
                "            with Projects\n" +
                "    with Modifier(recursive)\n"
                + "", fe.toString());
    }

    @Test
    public void testDataSetAToM()
    {
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withChildren();
        fe.withComponents();
        fe.withDataStore();
        fe.withLinkedData();
        fe.withExperiment();
        fe.withHistory();
        fe.withMaterialProperties();
        fe.withModifier();
        assertEquals("DataSet\n" +
                "    with DataStore\n" +
                "    with LinkedData\n" +
                "    with Experiment\n" +
                "    with MaterialProperties\n" +
                "    with Children\n" +
                "    with Components\n" +
                "    with History\n" +
                "    with Modifier\n" +
                "", fe.toString());
    }

    @Test
    public void testDataSetPToZ()
    {
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withParents();
        fe.withPhysicalData();
        fe.withProperties();
        fe.withRegistrator();
        fe.withSample();
        fe.withType();
        fe.withTags();
        assertEquals("DataSet\n" +
                "    with Type\n" +
                "    with PhysicalData\n" +
                "    with Sample\n" +
                "    with Properties\n" +
                "    with Parents\n" +
                "    with Tags\n" +
                "    with Registrator\n" +
                "", fe.toString());
    }

    @Test
    public void testDataSetDeepAndRecursive()
    {
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withChildren().withProperties();
        fe.withComponents().withChildrenUsing(fe);
        fe.withDataStore();
        fe.withLinkedData().withExternalDms();
        fe.withExperiment().withDataSetsUsing(fe);
        fe.withHistory().withAuthor();
        fe.withMaterialProperties().withHistory();
        fe.withMaterialProperties().withProperties();
        fe.withMaterialProperties().withRegistrator();
        fe.withModifierUsing(fe.withMaterialProperties().withRegistrator());
        fe.withParents().sortBy().modificationDate().asc();
        fe.withParents().sortBy().code().desc();
        fe.withParents().sortBy().registrationDate().desc();
        fe.withPhysicalData();
        fe.withProperties();
        fe.withRegistrator().withRegistrator().withRegistrator();
        fe.withSample().withAttachments().withContent();
        fe.withType();
        fe.withTags().withOwner();
        assertEquals("DataSet\n" +
                "    with Type\n" +
                "    with DataStore\n" +
                "    with PhysicalData\n" +
                "    with LinkedData\n" +
                "        with ExternalDms\n" +
                "    with Experiment\n" +
                "        with DataSets(recursive)\n" +
                "    with Sample\n" +
                "        with Attachments\n" +
                "            with Content\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "        with History\n" +
                "        with Registrator\n" +
                "        with Properties\n" +
                "    with Parents order by MODIFICATION_DATE ASC, order by CODE DESC, order by REGISTRATION_DATE DESC\n" +
                "    with Children\n" +
                "        with Properties\n" +
                "    with Components\n" +
                "        with Children(recursive)\n" +
                "    with Tags\n" +
                "        with Owner\n" +
                "    with History\n" +
                "        with Author\n" +
                "    with Modifier(recursive)\n" +
                "    with Registrator\n" +
                "        with Registrator\n" +
                "            with Registrator\n" +
                "", fe.toString());
    }

    @Test
    public void testExperiment()
    {
        ExperimentFetchOptions fe = new ExperimentFetchOptions();
        fe.withAttachments();
        fe.withDataSets();
        fe.withHistory();
        fe.withMaterialProperties();
        fe.withModifier();
        fe.withProject();
        fe.withProperties();
        fe.withRegistrator();
        fe.withSamples();
        fe.withTags();
        fe.withType();
        assertEquals("Experiment\n" +
                "    with Type\n" +
                "    with Project\n" +
                "    with DataSets\n" +
                "    with Samples\n" +
                "    with History\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "    with Tags\n" +
                "    with Registrator\n" +
                "    with Modifier\n" +
                "    with Attachments\n" +
                "", fe.toString());
    }

    @Test
    public void testExperimentDeepAndRecursive()
    {
        ExperimentFetchOptions fe = new ExperimentFetchOptions();
        fe.withAttachments().withContent();
        fe.withAttachments().withPreviousVersion().withPreviousVersionUsing(fe.withAttachments());
        fe.withDataSets().withProperties();
        fe.withDataSets().withMaterialProperties();
        fe.withDataSets().withExperimentUsing(fe);
        fe.withHistory();
        fe.withMaterialProperties();
        fe.withModifier();
        fe.withProject();
        fe.withProperties();
        fe.withRegistrator().withRegistrator().withSpace().withRegistrator().withSpace().withRegistrator().withSpace().withProjects()
                .withExperimentsUsing(fe);
        fe.withSamples();
        fe.withTags();
        fe.withType();
        assertEquals("Experiment\n" +
                "    with Type\n" +
                "    with Project\n" +
                "    with DataSets\n" +
                "        with Experiment(recursive)\n" +
                "        with Properties\n" +
                "        with MaterialProperties\n" +
                "    with Samples\n" +
                "    with History\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "    with Tags\n" +
                "    with Registrator\n" +
                "        with Registrator\n" +
                "            with Space\n" +
                "                with Registrator\n" +
                "                    with Space\n" +
                "                        with Registrator\n" +
                "                            with Space\n" +
                "                                with Projects\n" +
                "                                    with Experiments(recursive)\n" +
                "    with Modifier\n" +
                "    with Attachments\n" +
                "        with PreviousVersion\n" +
                "            with PreviousVersion(recursive)\n" +
                "        with Content\n" +
                "", fe.toString());
    }

    @Test
    public void testSpaceEmpty()
    {
        SpaceFetchOptions fe = new SpaceFetchOptions();
        assertEquals("Space\n", fe.toString());
    }

    @Test
    public void testSpace()
    {
        SpaceFetchOptions fe = new SpaceFetchOptions();
        fe.withProjects();
        fe.withRegistrator();
        fe.withSamples();
        assertEquals("Space\n" +
                "    with Registrator\n" +
                "    with Samples\n" +
                "    with Projects\n" +
                "", fe.toString());
    }

    @Test
    public void testSpaceDeepAndRecursive()
    {
        SpaceFetchOptions fe = new SpaceFetchOptions();
        fe.withProjects().withLeader().withRegistrator().withSpace().withProjects().withSpaceUsing(fe);
        fe.withProjects().withAttachments();
        fe.withProjects().withRegistrator();
        fe.withRegistrator();
        assertEquals("Space\n" +
                "    with Registrator\n" +
                "    with Projects\n" +
                "        with Registrator\n" +
                "        with Leader\n" +
                "            with Registrator\n" +
                "                with Space\n" +
                "                    with Projects\n" +
                "                        with Space(recursive)\n" +
                "        with Attachments\n" +
                "", fe.toString());
    }

    @Test
    public void testProject()
    {
        ProjectFetchOptions fe = new ProjectFetchOptions();
        fe.withAttachments();
        fe.withExperiments();
        fe.withHistory();
        fe.withLeader();
        fe.withModifier();
        fe.withRegistrator();
        fe.withSpace();
        assertEquals("Project\n" +
                "    with Experiments\n" +
                "    with History\n" +
                "    with Space\n" +
                "    with Registrator\n" +
                "    with Modifier\n" +
                "    with Leader\n" +
                "    with Attachments\n" +
                "", fe.toString());
    }

    @Test
    public void testProjectDeepAndRecursive()
    {
        ProjectFetchOptions fe = new ProjectFetchOptions();
        fe.withAttachments().withContent();
        fe.withAttachments().withRegistrator().withSpace().withProjectsUsing(fe);
        fe.withExperiments().withProjectUsing(fe);
        fe.withHistory().withAuthor().withSpace().withProjectsUsing(fe);
        fe.withLeader().withSpace().withProjects().withExperiments().withProperties();
        fe.withModifier();
        assertEquals("Project\n" +
                "    with Experiments\n" +
                "        with Project(recursive)\n" +
                "    with History\n" +
                "        with Author\n" +
                "            with Space\n" +
                "                with Projects(recursive)\n" +
                "    with Modifier\n" +
                "    with Leader\n" +
                "        with Space\n" +
                "            with Projects\n" +
                "                with Experiments\n" +
                "                    with Properties\n" +
                "    with Attachments\n" +
                "        with Registrator\n" +
                "            with Space\n" +
                "                with Projects(recursive)\n" +
                "        with Content\n" +
                "", fe.toString());
    }

    @Test
    public void testMaterial()
    {
        MaterialFetchOptions fe = new MaterialFetchOptions();
        fe.withHistory();
        fe.withMaterialProperties();
        fe.withProperties();
        fe.withRegistrator();
        fe.withTags();
        fe.withType();
        assertEquals("Material\n" +
                "    with Type\n" +
                "    with History\n" +
                "    with Registrator\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "    with Tags\n" +
                "", fe.toString());
    }

    @Test
    public void testMaterialDeepAndRecursive()
    {
        MaterialFetchOptions fe = new MaterialFetchOptions();
        fe.withHistory().withAuthor();
        fe.withMaterialProperties().withMaterialProperties().withMaterialProperties().withMaterialPropertiesUsing(fe);
        fe.withProperties();
        fe.withRegistrator().withSpace().withProjects().withExperiments().withMaterialProperties().withMaterialPropertiesUsing(fe);
        fe.withRegistrator().withSpace().withProjects().withExperiments().withRegistrator();
        fe.withTags();
        fe.withType();
        assertEquals("Material\n" +
                "    with Type\n" +
                "    with History\n" +
                "        with Author\n" +
                "    with Registrator\n" +
                "        with Space\n" +
                "            with Projects\n" +
                "                with Experiments\n" +
                "                    with MaterialProperties\n" +
                "                        with MaterialProperties(recursive)\n" +
                "                    with Registrator\n" +
                "    with Properties\n" +
                "    with MaterialProperties\n" +
                "        with MaterialProperties\n" +
                "            with MaterialProperties\n" +
                "                with MaterialProperties(recursive)\n" +
                "    with Tags\n" +
                "", fe.toString());
    }

    @Test
    public void testTag()
    {
        TagFetchOptions fe = new TagFetchOptions();
        fe.withOwner().withRegistrator();
        assertEquals("Tag\n" +
                "    with Owner\n" +
                "        with Registrator\n" +
                "", fe.toString());
    }

    @Test
    public void testTagDeepAndRecursive()
    {
        TagFetchOptions fe = new TagFetchOptions();
        fe.withOwner().withSpace().withSamples().withTagsUsing(fe);
        assertEquals("Tag\n" +
                "    with Owner\n" +
                "        with Space\n" +
                "            with Samples\n" +
                "                with Tags(recursive)\n" +
                "", fe.toString());
    }

    @Test
    public void testEmptyTag()
    {
        TagFetchOptions fe = new TagFetchOptions();
        assertEquals("Tag\n", fe.toString());
    }

    @Test
    public void testHistory()
    {
        HistoryEntryFetchOptions fe = new HistoryEntryFetchOptions();
        fe.withAuthor();
        assertEquals("HistoryEntry\n" +
                "    with Author\n" +
                "", fe.toString());
    }

    @Test
    public void testHistoryDeepAndRecursive()
    {
        HistoryEntryFetchOptions fe = new HistoryEntryFetchOptions();
        fe.withAuthor().withSpace().withProjects().withHistoryUsing(fe);
        fe.withAuthor().withSpace().withProjects().withExperiments().withHistory();
        assertEquals("HistoryEntry\n" +
                "    with Author\n" +
                "        with Space\n" +
                "            with Projects\n" +
                "                with Experiments\n" +
                "                    with History\n" +
                "                with History(recursive)\n" +
                "", fe.toString());
    }

    @Test
    public void testPerson()
    {
        PersonFetchOptions fe = new PersonFetchOptions();
        fe.withRegistrator();
        fe.withSpace();
        assertEquals("Person\n" +
                "    with Space\n" +
                "    with Registrator\n" +
                "", fe.toString());
    }

    @Test
    public void testPersonDeepAndRecursive()
    {
        PersonFetchOptions fe = new PersonFetchOptions();
        fe.withRegistrator();
        fe.withSpace().withRegistratorUsing(fe.withRegistrator());
        assertEquals("Person\n" +
                "    with Space\n" +
                "        with Registrator\n" +
                "    with Registrator(recursive)\n" +
                "", fe.toString());
    }

}