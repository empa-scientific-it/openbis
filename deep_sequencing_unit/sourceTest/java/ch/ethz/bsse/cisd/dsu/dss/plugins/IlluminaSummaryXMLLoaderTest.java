/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class IlluminaSummaryXMLLoaderTest extends AbstractFileSystemTestCase
{
    @Test
    public void test()
    {
        File file = new File(workingDirectory, "test.xml");
        FileUtilities.writeToFile(file, EXAMPLE);
        IlluminaSummary summary =
                IlluminaSummaryReportingPlugin.IlluminaSummaryXMLLoader.readSummaryXML(file);

        ChipResultsSummary chipResultsSummary = summary.getChipResultsSummary();
        LaneResultsSummary laneResultsSummary = summary.getLaneResultsSummary();
        assertEquals(Long.valueOf(98792458L), chipResultsSummary.getClusterCountPF());
        assertEquals(Long.valueOf(158466917L), chipResultsSummary.getClusterCountRaw());
        assertEquals(Long.valueOf(3556528488L), chipResultsSummary.getYield());
        // if not there yhen take the init value
        assertEquals(Double.valueOf(0.0), chipResultsSummary.getDensityRatio());

        assertEquals(Long.valueOf(381287L), laneResultsSummary.getRead().getLanes().get(0)
                .getClusterCountRaw().getMean());
        assertEquals(Long.valueOf(277736L), laneResultsSummary.getRead().getLanes().get(0)
                .getClusterCountPF().getMean());
        assertEquals(Long.valueOf(1199819L), laneResultsSummary.getRead().getLanes().get(0)
                .getLaneYield());
        assertEquals(Double.valueOf(79.37), laneResultsSummary.getRead().getLanes().get(0)
                .getPercentUniquelyAlignedPF().getMean());

        if (summary.getSoftware() == "")
        {
            assertEquals("", summary.getSoftware());
        } else
        {
            assertEquals("CASAVA-1.6.0", summary.getSoftware());
        }
    }

    /**
     *
     */
    protected static final String EXAMPLE = "<?xml version='1.0' ?>\n               "
            + "<?xml-stylesheet type='text/xsl' href='Summary.xsl' ?>\n             "
            + "<Summary>                                                            "
            + "  <ChipResultsSummary>                                               "
            + "    <clusterCountPF>98792458</clusterCountPF>                        "
            + "    <clusterCountRaw>158466917</clusterCountRaw>                     "
            + "    <yield>3556528488</yield>                                        "
            + "  </ChipResultsSummary>                                              "
            + "  <ChipSummary>                                                      "
            + "    <ChipID>unknown</ChipID>                                         "
            + "    <Machine>HWI-EAS264</Machine>                                    "
            + "    <RunFolder>090916_42R0CAAXX</RunFolder>                          "
            + "  </ChipSummary>                                                     "
            + "  <Date>Tue Oct 20 18:15:43 2009</Date>                              "
            + "  <ExpandedLaneSummary>                                              "
            + " <Read>                                                              "
            + "  <readNumber>1</readNumber>                                         "
            + "  <Lane>                                                             "
            + "    <laneNumber>1</laneNumber>                                       "
            + "    <clusterCountRaw>                                                "
            + "      <mean>381286.80</mean>                                         "
            + "      <stdev>28613.35</stdev>                                        "
            + "     <sumsq>98246880000.00</sumsq>                                   "
            + " </clusterCountRaw>                                                  "
            + " <errorPF>                                                           "
            + "   <mean>0.72</mean>                                                 "
            + "   <stdev>0.49</stdev>                                               "
            + "   <sumsq>90.37</sumsq>                                              "
            + " </errorPF>                                                          "
            + " <errorRaw>                                                          "
            + "   <mean>0.83</mean>                                                 "
            + "   <stdev>0.48</stdev>                                               "
            + "   <sumsq>110.63</sumsq>                                             "
            + " </errorRaw>                                                         "
            + " <infoContentPF>                                                     "
            + "   <mean>213494</mean>                                               "
            + "   <stdev>9929</stdev>                                               "
            + "   <sumsq>5481321756576</sumsq>                                      "
            + " </infoContentPF>                                                    "
            + " <infoContentRaw>                                                    "
            + "   <mean>229532</mean>                                               "
            + "   <stdev>11786</stdev>                                              "
            + "   <sumsq>6338734406410</sumsq>                                      "
            + " </infoContentRaw>                                                   "
            + " <percentClustersPF>                                                 "
            + "   <mean>73.03</mean>                                                "
            + "   <stdev>2.72</stdev>                                               "
            + "   <sumsq>890.49</sumsq>                                             "
            + " </percentClustersPF>                                                "
            + " <percentUniquelyAlignedPF>                                          "
            + "   <mean>79.37</mean>                                                "
            + "   <stdev>0.86</stdev>                                               "
            + "   <sumsq>755955.73</sumsq>                                          "
            + " </percentUniquelyAlignedPF>                                         "
            + " <phasingApplied>0.8203</phasingApplied>                             "
            + " <prephasingApplied>0.3881</prephasingApplied>                       "
            + " <signalAverage1to3>                                                 "
            + "   <mean>526.07</mean>                                               "
            + "   <stdev>43.17</stdev>                                              "
            + "   <sumsq>223675.10</sumsq>                                          "
            + " </signalAverage1to3>                                                "
            + " <signalAverage2to4>                                                 "
            + "   <mean>519.91</mean>                                               "
            + "   <stdev>41.72</stdev>                                              "
            + "   <sumsq>208871.60</sumsq>                                          "
            + " </signalAverage2to4>                                                "
            + " <signalLoss10to20>                                                  "
            + "   <mean>1.01</mean>                                                 "
            + "   <stdev>0.40</stdev>                                               "
            + "   <sumsq>19.25</sumsq>                                              "
            + " </signalLoss10to20>                                                 "
            + " <signalLoss2to10>                                                   "
            + "   <mean>0.45</mean>                                                 "
            + "      <stdev>0.53</stdev>                                            "
            + "      <sumsq>33.94</sumsq>                                           "
            + "    </signalLoss2to10>                                               "
            + "  </Lane>                                                            "
            + "   </Read>                                                           "
            + " </ExpandedLaneSummary>                                              "
            + " <LaneParameterSummary/>                                             "
            + " <LaneResultsSummary>                                                "
            + " <Read>                                                              "
            + " <readNumber>1</readNumber>                                          "
            + " <Lane>                                                              "
            + " <laneNumber>1</laneNumber>                                          "
            + " <averageAlignScorePF>                                               "
            + " <mean>94.25</mean>                                                  "
            + " <stdev>3.64</stdev>                                                 "
            + " <sumsq>1067460.47</sumsq>                                           "
            + " </averageAlignScorePF>                                              "
            + " <clusterCountPF>                                                    "
            + " <mean>277736</mean>                                                 "
            + " <stdev>11981</stdev>                                                "
            + " <sumsq>17224950000</sumsq>                                          "
            + " </clusterCountPF>                                                   "
            + " <clusterCountRaw>                                                   "
            + " <mean>381287</mean>                                                 "
            + " <stdev>28613</stdev>                                                "
            + " <sumsq>98246880000</sumsq>                                          "
            + " </clusterCountRaw>                                                  "
            + "<errorPF>                                                            "
            + "<mean>0.72</mean>                                                    "
            + "<stdev>0.49</stdev>                                                  "
            + "<sumsq>90.37</sumsq>                                                 "
            + "</errorPF>                                                           "
            + "<laneYield>1199819</laneYield>                                       "
            + "<oneSig>                                                             "
            + "<mean>536</mean>                                                     "
            + "<stdev>46</stdev>                                                    "
            + "<sumsq>256164</sumsq>                                                "
            + "</oneSig>                                                            "
            + "<percentClustersPF>                                                  "
            + "<mean>73.03</mean>                                                   "
            + "<stdev>2.72</stdev>                                                  "
            + "<sumsq>890.49</sumsq>                                                "
            + "</percentClustersPF>                                                 "
            + "<percentUniquelyAlignedPF>                                           "
            + "<mean>79.37</mean>                                                   "
            + "<stdev>0.86</stdev>                                                  "
            + "<sumsq>755955.73</sumsq>                                             "
            + "</percentUniquelyAlignedPF>                                          "
            + "<signal20AsPctOf1>                                                   "
            + "<mean>82.19</mean>                                                   "
            + "<stdev>5.09</stdev>                                                  "
            + "<sumsq>3103.16</sumsq>                                               "
            + "</signal20AsPctOf1>                                                  "
            + " </Lane>												                "
            + " </Read>                                                             "
            + " </LaneResultsSummary>                                                "
            + "  <TileErrorsByLane/>                                                "
            + "  <Software>CASAVA-1.6.0</Software>                                  "
            + "</Summary>";
}
