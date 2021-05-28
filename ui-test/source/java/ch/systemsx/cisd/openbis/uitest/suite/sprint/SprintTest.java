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

package ch.systemsx.cisd.openbis.uitest.suite.sprint;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.RoleAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class SprintTest extends SprintSuite
{

//    @Test
//    public void basic() throws Exception
//    {
//        // 1) Login and authorization
//        assumePage(TopBar.class).admin();
//        assumePage(AdminMenu.class).roles();
//        assertThat(browser(), displays(RoleAssignmentBrowser.class));
//
//        // 2) Space
//        Space space = create(aSpace().withCode("sprint-test"));
//        assertThat(browserEntryOf(space), exists());
//
//        // 3) Sample types and properties
//        create(aSampleType().withCode("sprint test"));
//        assertThat(browser(), displays(AddSampleTypeDialog.class));
//        assumePage(AddSampleTypeDialog.class).cancel();
//
//        // create at least one listable sample type to make sure sample browser dropdown works
//        create(aSampleType().thatIsListable());
//
//        SampleType sampleType =
//                create(aSampleType()
//                        .withCode("sprint_test")
//                        .thatIsNotListable()
//                        .thatCanBeComponent()
//                        .thatShowsParents());
//        assertThat(browserEntryOf(sampleType), exists());
//        assertThat(sampleTypesInSampleBrowser(), doNotContain(sampleType));
//
//        perform(anUpdateOf(sampleType).settingItListable());
//        assertThat(sampleTypesInSampleBrowser(), contain(sampleType));
//
//        Vocabulary vocabulary =
//                create(aVocabulary()
//                        .withCode("SPRINT-TEST-ANIMAL")
//                        .withTerms("mouse", "fly", "tiger")
//                        .withUrl("http://www.ask.com/web?q=${term}"));
//
//        PropertyType varcharPropertyType =
//                create(aVarcharPropertyType()
//                        .withCode("SPRINT-TEST.TEXT")
//                        .withLabel("Sprint Test Text")
//                        .withDescription("some text"));
//
//        PropertyType realPropertyType =
//                create(aRealPropertyType()
//                        .withCode("SPRINT-TEST.REAL")
//                        .withLabel("Sprint Test Real")
//                        .withDescription("some text"));
//
//        PropertyType animalPropertyType =
//                create(aVocabularyPropertyType(vocabulary)
//                        .withCode("SPRINT-TEST.ANIMAL")
//                        .withLabel("Sprint Test Animal")
//                        .withDescription("some text"));
//
//        create(aSamplePropertyTypeAssignment()
//                .with(sampleType)
//                .thatIsMandatory()
//                .with(varcharPropertyType));
//        create(aSamplePropertyTypeAssignment().with(sampleType).with(animalPropertyType));
//
//        assertThat(sampleRegistrationPageFor(sampleType),
//                hasInputsForProperties(varcharPropertyType, animalPropertyType));
//
//        // 4) Sample
//        Sample sample =
//                create(aSample().ofType(sampleType)
//                        .withCode("SPRINT1")
//                        .in(space)
//                        .withProperty(varcharPropertyType, "some text")
//                        .withProperty(animalPropertyType, "mouse"));
//
//        assertThat(browserEntryOf(sample), exists());
//        assertThat(browserEntryOf(sample), containsValue(animalPropertyType.getLabel(), "mouse"));
//        assertThat(browserEntryOf(sample), containsLink(animalPropertyType.getLabel(),
//                "http://www.ask.com/web?q=MOUSE"));
//
//        // 5) Project and experiment
//        Project project = create(aProject().withCode("P1").in(space));
//        ExperimentType experimentType = create(anExperimentType().withCode("SPRINT_TEST"));
//
//        Experiment experiment =
//                create(anExperiment().ofType(experimentType).in(project).withCode("exp1")
//                        .withSamples(sample));
//        assertThat(browserEntryOf(sample), containsValue("Experiment", experiment.getCode()));
//        assertThat(browserEntryOf(sample), containsValue("Project", project.getCode()));
//
//        // 9) Deletion
//        deleteExperimentsFrom(project);
//        emptyTrash();
//        delete(project);
//        delete(space);
//        delete(sampleType);
//        delete(experimentType);
//        delete(varcharPropertyType);
//        delete(realPropertyType);
//        delete(animalPropertyType);
//        delete(vocabulary);
//    }
}
