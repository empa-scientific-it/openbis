package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.etlserver.plugins.grouping.Grouping;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;

/**
 * @author Sascha Fedorenko
 */
public class BySpacePolicyTest extends ByPoliceAbstractTest
{
    @Override
    protected BySpacePolicy getPolicy(int min, int max)
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, Integer.toString(min));
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, Integer.toString(max));

        return new BySpacePolicy(props);
    }

    private void assertAllDataSetsAreNotGeneratedLookLikeANumbersAndSumToZero(List<AbstractExternalData> filtered)
    {
        assertAllDataSetsAreNotGenerated(filtered);

        String errorMsg = getErrorMessage(filtered);

        // funny way to check that items from the same project are pairwise
        int counter = 0;

        for (AbstractExternalData dataSet : filtered)
        {
            try
            {
                counter += Integer.parseInt(dataSet.getCode());
            } catch (NumberFormatException nfe)
            {
                fail(errorMsg);
            }
        }

        assertEquals(errorMsg, 0, counter);
    }

    @Test
    public void testEverythingFromProjectIsReturnedIfDatasetsAreSmall()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 2L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e3", "dt1", "ds3", 13L));

        List<AbstractExternalData> filtered = filter(14, 100, dataSets);

        assertEquals("[ds1, ds2, ds3]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetsAreTooSmall()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e3", "dt1", "ds1", 13L));
        dataSets.add(ctx.createDataset("p1", "e3", "dt1", "ds2", 14L));
        dataSets.add(ctx.createDataset("p1", "e3", "dt1", "ds3", 18L));
        dataSets.add(ctx.createDataset("p1", "e3", "dt1", "ds4", 3L));

        List<AbstractExternalData> filtered = filter(50, 100, dataSets);

        assertEquals("[]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetsAreInOneSpaceAndProjectAndFitPerfectly()
    {

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e3", "dt1", "ds1", 13L));
        dataSets.add(ctx.createDataset("p2", "e3", "dt1", "ds2", 14L));
        dataSets.add(ctx.createDataset("p3", "e3", "dt1", "ds3", 18L));
        dataSets.add(ctx.createDataset("p4", "e3", "dt1", "ds4", 3L));

        List<AbstractExternalData> filtered = filter(40, 100, dataSets);

        assertEquals("[ds1, ds2, ds3, ds4]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTwoSpacesOneBigOneOK()
    {

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("tooBigSpace", "p1", "e3", "dt1", "ds1", 1L));
        dataSets.add(ctx.createDataset("tooBigSpace", "p1", "e3", "dt1", "ds2", 1L));
        dataSets.add(ctx.createDataset("tooBigSpace", "p1", "e3", "dt1", "ds3", 1L));
        dataSets.add(ctx.createDataset("tooBigSpace", "p1", "e3", "dt1", "ds4", 1L));
        dataSets.add(ctx.createDataset("tooBigSpace", "p1", "e3", "dt1", "ds5", 1L));
        dataSets.add(ctx.createDataset("okSpace", "p1", "e3", "dt1", "ds6", 1L));
        dataSets.add(ctx.createDataset("okSpace", "p1", "e3", "dt1", "ds7", 1L));
        dataSets.add(ctx.createDataset("okSpace", "p1", "e3", "dt1", "ds8", 1L));
        dataSets.add(ctx.createDataset("smallSpace", "p1", "e3", "dt1", "sometrash", 1L));

        List<AbstractExternalData> filtered = filter(2, 4, dataSets);

        assertEquals("[ds6, ds7, ds8]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testBigSpaceWithSmallProjects()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p1", "e3", "dt1", "1", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p1", "e3", "dt1", "-1", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p2", "e3", "dt1", "2", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p2", "e3", "dt1", "-2", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p3", "e3", "dt1", "3", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p3", "e3", "dt1", "-3", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p4", "e3", "dt1", "4", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "p4", "e3", "dt1", "-4", 1L));
        dataSets.add(ctx.createDataset("smallSpace3", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace3", "p1", "e3", "dt1", null, 1L));

        List<AbstractExternalData> filtered = filter(5, 7, dataSets);
        AssertionUtil.assertSize(filtered, 6);

        assertAllDataSetsAreNotGeneratedLookLikeANumbersAndSumToZero(filtered);

        context.assertIsSatisfied();
    }

    @Test
    public void testBigSpaceWithBigAndOKProjects()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 1L));

        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 1L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 1L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 1L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject3", "e3", "dt1", null, 1L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject3", "e3", "dt1", null, 1L));

        dataSets.add(ctx.createDataset("rightSpace", "bigProject1", "e3", "dt1", null, 7L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject1", "e3", "dt1", null, 7L));

        dataSets.add(ctx.createDataset("rightSpace", "bigProject2", "e3", "dt1", null, 3L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject2", "e3", "dt1", null, 3L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject2", "e3", "dt1", null, 3L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject2", "e3", "dt1", null, 3L));

        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "e3", "dt1", "a", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "e3", "dt1", "b", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "e3", "dt1", "c", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "e3", "dt1", "d", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "e3", "dt1", "e", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "e3", "dt1", "f", 1L));

        List<AbstractExternalData> filtered = filter(5, 8, dataSets);

        assertEquals(Code.extractCodes(filtered).toString(), "[a, b, c, d, e, f]");

        context.assertIsSatisfied();
    }

    @Test
    public void testBigProjectWithManySmallExperiment()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e3", "dt1", null, 100L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e3", "dt1", null, 130L));

        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", "100", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", "-50", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", "-50", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE2", "dt1", "60", 60L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", "-60", 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", "0", 30L));

        List<AbstractExternalData> filtered = filter(100, 140, dataSets);

        assertAllDataSetsAreNotGeneratedLookLikeANumbersAndSumToZero(filtered);

        Set<String> differentExperiments = new HashSet<String>();

        for (AbstractExternalData data : filtered)
        {
            differentExperiments.add(data.getExperiment().getCode());
        }

        assertEquals(getErrorMessage(filtered), 2, differentExperiments.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testBigProjectWithPerfectExperiment()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e3", "dt1", null, 100L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e3", "dt1", null, 130L));

        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", "100", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", "-49", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", "-51", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE2", "dt1", "60", 60L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", "-60", 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", "0", 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "OK", "dt1", "a", 90L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "OK", "dt1", "b", 29L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "OK", "dt1", "c", 1L));

        List<AbstractExternalData> filtered = filter(100, 140, dataSets);

        assertEquals(Code.extractCodes(filtered).toString(), "[a, b, c]");

        context.assertIsSatisfied();
    }

    @Test
    public void testBigProjectWithBigExperimentAndSomeSamples()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e3", "dt1", null, 100L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e3", "dt1", null, 130L));

        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE2", "dt1", null, 60L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", null, 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", null, 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "samp1", "a", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "samp2", "b", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "samp2", "c", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "samp3", "e", 60L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "samp4", "f", 1L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "samp4", "f2", 59L));

        List<AbstractExternalData> filtered = filter(100, 140, dataSets);

        assertAllDataSetsAreNotGenerated(filtered);
        assertTotalDataSetsSize(120, filtered);
        context.assertIsSatisfied();
    }

    @Test
    public void testBigProjectWithBigExperimentAndNoSampleAndManySmallDataSets()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace1", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("smallSpace2", "p1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject1", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));
        dataSets.add(ctx.createDataset("rightSpace", "smallProject2", "e3", "dt1", null, 10L));

        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e1", "dt1", "x", 60L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e1", "dt1", "y", 60L));
        dataSets.add(ctx.createDataset("rightSpace", "bigProject3", "e1", "dt1", "z", 60L));

        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE1", "dt1", null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE2", "dt1", null, 60L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", null, 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "smallE3", "dt1", null, 30L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", "toSmallSample", (String) null, 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", null, "a", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", null, "b", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", null, "c", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", null, "e", 60L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", null, "f", 20L));
        dataSets.add(ctx.createDataset("rightSpace", "rightProject", "bigE", "dt1", null, "f2", 40L));

        List<AbstractExternalData> filtered = filter(100, 139, dataSets);

        assertAllDataSetsAreNotGenerated(filtered);
        assertTotalDataSetsSize(120, filtered);
        assertAllDataSetsInTheSameExperimentAndSample(filtered);
        context.assertIsSatisfied();
    }

    private void assertAllDataSetsInTheSameExperimentAndSample(List<AbstractExternalData> filtered)
    {
        Set<String> keys = new HashSet<String>();
        for (AbstractExternalData data : filtered)
        {
            keys.add(Grouping.Experiment.getGroupKey(data) + "#" + Grouping.Sample.getGroupKey(data));
        }
        System.out.println(keys);
        AssertionUtil.assertSize(keys, 1);
    }
}
