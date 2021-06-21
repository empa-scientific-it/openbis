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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import junit.framework.Assert;

/**
 * Tests various service methode which generates new unique IDs.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class CodeGenerationTest extends SystemTestCase
{
    @BeforeMethod
    public void setSequences()
    {
        setCodeSequence(8);
        setExperimentCodeSequence(100);
        setSampleCodeSequence(200);
    }

    private void setCodeSequence(int value)
    {
        jdbcTemplate.queryForObject("select setval('code_seq', " + value + ")", Long.class);
    }

    private void setExperimentCodeSequence(int value)
    {
        jdbcTemplate.queryForObject("select setval('experiment_code_seq', " + value + ")", Long.class);
    }

    private void setSampleCodeSequence(int value)
    {
        jdbcTemplate.queryForObject("select setval('sample_code_seq', " + value + ")", Long.class);
    }

    @Test
    public void testCommonClientServiceGenerateCodeForExperimentsAndSamples()
    {
        logIntoCommonClientService();

        assertEquals("E-101", commonClientService.generateCode("E-", EntityKind.EXPERIMENT));
        assertEquals("E-102", commonClientService.generateCode("E-", EntityKind.EXPERIMENT));
        assertEquals("S-201", commonClientService.generateCode("S-", EntityKind.SAMPLE));
        assertEquals("S-202", commonClientService.generateCode("S-", EntityKind.SAMPLE));
    }

    @Test
    public void testCommonClientServiceGenerateCodeWhenSomeCodesAreAlreadyUsed()
    {
        logIntoCommonClientService();

        // Experiments EXP10 and EXP11 already exist
        setExperimentCodeSequence(10);
        assertEquals("EXP12", commonClientService.generateCode("EXP", EntityKind.EXPERIMENT));
        assertEquals("EXP13", commonClientService.generateCode("EXP", EntityKind.EXPERIMENT));

        // Samples A10 .. A22 already exist
        setSampleCodeSequence(10);
        assertEquals("A23", commonClientService.generateCode("A", EntityKind.SAMPLE));
        assertEquals("A24", commonClientService.generateCode("A", EntityKind.SAMPLE));

        // Materials AD3 and AD5 already exist
        setCodeSequence(3);
        assertEquals("AD4", commonClientService.generateCode("AD", EntityKind.MATERIAL));
        assertEquals("AD6", commonClientService.generateCode("AD", EntityKind.MATERIAL));

        // Data sets 20120628092259000-22 .. 20120628092259000-25 already exist
        setCodeSequence(22);
        assertEquals("20120628092259000-26",
                commonClientService.generateCode("20120628092259000-", EntityKind.DATA_SET));
        assertEquals("20120628092259000-27",
                commonClientService.generateCode("20120628092259000-", EntityKind.DATA_SET));
    }

    @Test
    public void testGenericServerGenerateCodes()
    {
        assertEquals("[E-101, E-102]",
                genericServer.generateCodes(systemSessionToken, "E-", EntityKind.EXPERIMENT, 2)
                        .toString());
        assertEquals("[S-201, S-202, S-203]",
                genericServer.generateCodes(systemSessionToken, "S-", EntityKind.SAMPLE, 3)
                        .toString());
        assertEquals("[DS-9, DS-10]",
                genericServer.generateCodes(systemSessionToken, "DS-", EntityKind.DATA_SET, 2)
                        .toString());
        assertEquals("[M-11, M-12]",
                genericServer.generateCodes(systemSessionToken, "M-", EntityKind.MATERIAL, 2)
                        .toString());
    }

    @Test
    public void testGenericServerGenerateManyCodesWhenSomeCodesAreAlreadyUsed()
    {
        setSampleCodeSequence(10);
        genericServer.generateCodes(systemSessionToken, "A", EntityKind.SAMPLE, 1000);
    }

    @Test
    public void testGenericServerGenerateCodesWhenSomeCodesAreAlreadyUsed()
    {
        // Samples A10 .. A22 already exist
        setSampleCodeSequence(10);
        List<String> codes =
                genericServer.generateCodes(systemSessionToken, "A", EntityKind.SAMPLE, 5);
        Assert.assertEquals(Arrays.asList("A23", "A24", "A25", "A26", "A27"), codes);
    }

    @Test
    public void testETLServiceGenerateCodes()
    {
        assertEquals("[E-101, E-102]",
                etlService.generateCodes(systemSessionToken, "E-", EntityKind.EXPERIMENT, 2)
                        .toString());
    }

    @Test
    public void testAutomaticCreationOfSampleCodesInBatchSampleRegistration()
    {
        // Samples A10 .. A22 already exist
        setSampleCodeSequence(10);

        String sessionID = logIntoCommonClientService().getSessionID();
        uploadFile(sessionID, "testAutomaticCreationOfSampleCodesInBatchSampleRegistration.txt",
                "experiment\tCOMMENT\n" + "/CISD/NEMO/EXP1\tA\n" + "/CISD/NEMO/EXP1\tB\n");
        SampleType sampleType = new SampleType();
        sampleType.setGeneratedCodePrefix("A");
        sampleType.setCode("CELL_PLATE");

        genericClientService.registerSamples(sampleType, SESSION_KEY, false, null, "/CISD", false);

        Experiment experiment =
                commonServer.getExperimentInfo(sessionID,
                        ExperimentIdentifierFactory.parse("/CISD/NEMO/EXP1"));
        List<Sample> samples =
                commonServer.listSamples(sessionID,
                        ListSampleCriteria.createForExperiment(new TechId(experiment)));
        Collections.sort(samples, new Comparator<Sample>()
            {
                @Override
                public int compare(Sample s1, Sample s2)
                {
                    return s1.getCode().compareTo(s2.getCode());
                }
            });
        StringBuilder builder = new StringBuilder();
        for (Sample sample : samples)
        {
            builder.append(sample.getIdentifier()).append(":").append(sample.getProperties());
            builder.append("\n");
        }
        assertEquals("/CISD/NEMO/A23:[COMMENT: A]\n" + "/CISD/NEMO/A24:[COMMENT: B]\n", builder.toString());
    }
}
