package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Sascha Fedorenko
 */
public class ByExperimentPolicyTest extends ByPoliceAbstractTest
{

    @Override
    protected IAutoArchiverPolicy getPolicy(int min, int max)
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, Integer.toString(min));
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, Integer.toString(max));

        return new ByExperimentPolicy(props);
    }

    @Test
    public void testEverythingFromProjectIsReturnedIfDatasetsAreSmall()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 2L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt2", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e3", "dt3", "ds3", 13L));

        List<AbstractExternalData> filtered = filter(14, 100, dataSets);

        assertEquals("[ds1, ds2, ds3]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1-1", "e112", "t2", "ds1", 10L));
        dataSets.add(ctx.createDataset("p2-1", "e122", "t1", "ds2", 10L));
        dataSets.add(ctx.createDataset("p2-1", "e121", "t2", "ds3", 10L));

        List<AbstractExternalData> filtered = filter(15, 25, dataSets);

        assertEquals("[ds2, ds3]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSubsetIsReturnedIfDatasetsAreTooBig()
    {

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds3", 9L));

        List<AbstractExternalData> filtered = filterWithoutShuffling(6, 10, dataSets);

        assertEquals("[ds1]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTooSmallSetsAreNotArchived()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds3", 9L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 9L));

        List<AbstractExternalData> filtered = filter(500, 1000, dataSets);

        assertEquals("[]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTooBigDataSetsAreArchivedOnTheirOwn()
    {

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7000L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8000L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds3", 9000L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 9000L));

        List<AbstractExternalData> filtered = filter(500, 1000, dataSets);

        AssertionUtil.assertSize(filtered, 1);

        context.assertIsSatisfied();
    }

    @Test
    public void testSameDatatypeIsGroupedSmalls()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds3", 9L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 9L));

        List<AbstractExternalData> filtered = filter(10, 1000, dataSets);

        assertEquals("[ds1, ds2]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSameDatatypeIsGroupedBigs()
    {

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 17L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 18L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds3", 19L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 19L));

        List<AbstractExternalData> filtered = filter(10, 1000, dataSets);

        assertEquals("[ds1, ds2]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSameExperimentIsGroupedSmalls()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt3", "ds3", 9L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt4", "ds4", 9L));
        dataSets.add(ctx.createDataset("p2", "e3", "dt1", "ds5", 9L));

        List<AbstractExternalData> filtered = filter(10, 1000, dataSets);

        assertEquals("[ds1, ds2, ds3]", Code.extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSameExperimentIsGroupedBigs()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 17L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds2", 18L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt3", "ds3", 19L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt4", "ds4", 19L));
        dataSets.add(ctx.createDataset("p2", "e3", "dt1", "ds5", 19L));

        List<AbstractExternalData> filtered = filter(10, 1000, dataSets);

        String value = Code.extractCodes(filtered).toString();

        assertTrue(value.equals("[ds3]") || value.equals("[ds4]"));

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetTypeIsSplitIfTooMany()
    {
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        Sample s1 = new Sample();
        s1.setIdentifier("s1");

        Sample s2 = new Sample();
        s1.setIdentifier("s2");

        Sample s3 = new Sample();
        s1.setIdentifier("s3");

        Sample s4 = new Sample();
        s1.setIdentifier("s4");

        Sample s5 = new Sample();
        s1.setIdentifier("s5"); // this one will be sorted out

        AbstractExternalData ds;
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds1", 10L));
        ds.setSample(s5);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds2", 10L));
        ds.setSample(s4);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds3", 10L));
        ds.setSample(s3);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds4", 10L));
        ds.setSample(s2);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds5", 10L));
        ds.setSample(s1);

        List<AbstractExternalData> filtered = filter(20, 45, dataSets);

        assertEquals(4, filtered.size());
        for (AbstractExternalData dsTest : filtered)
        {
            assertNotSame(s5, dsTest.getSampleIdentifier());
        }

        context.assertIsSatisfied();
    }

}
