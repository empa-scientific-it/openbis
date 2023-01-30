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

package ch.systemsx.cisd.openbis.generic.shared;

import org.apache.commons.lang3.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo.DataSetUploadInfoHelper;

/**
 * @author Izabela Adamczyk
 */
public class DataSetInfoHelperTest extends AssertJUnit
{
    @Test
    public void testDataSetUploadInfoContructorWithSample()
    {
        // sample but no experiment and parents
        String sample = "sampleA";
        String experiment = null;
        String dataSetType = "dataSetTypeC";
        String fileType = "fileTypeD";
        String[] parents = null;
        DataSetUploadInfo info =
                new DataSetUploadInfo(sample, experiment, parents, dataSetType, fileType);
        checkInfo(info, sample, experiment, parents, dataSetType, fileType);
    }

    @Test
    public void testDataSetUploadInfoConstructorNoSample()
    {
        // no sample but experiment and parents
        String sample = null;
        String experiment = "experimentB";
        String dataSetType = "dataSetTypeC";
        String fileType = "fileTypeD";
        String[] parents =
        { "p1", "p2", "p3" };
        DataSetUploadInfo info =
                new DataSetUploadInfo(sample, experiment, parents, dataSetType, fileType);
        checkInfo(info, sample, experiment, parents, dataSetType, fileType);
    }

    private void checkInfo(DataSetUploadInfo info, String sample, String experiment,
            String[] parents, String dataSetType, String fileType)
    {
        assertEquals(info.getSample(), sample);
        assertEquals(info.getExperiment(), experiment);
        assertEquals(StringUtils.join(info.getParents(), ";"), StringUtils.join(parents, ";"));
        assertEquals(info.getFileType(), fileType);
        assertEquals(info.getDataSetType(), dataSetType);
    }

    @Test
    public void testExtractFromCommentWithSample() throws Exception
    {
        DataSetUploadInfo info =
                DataSetUploadInfoHelper.extractFromCifexComment("sA,null,null,dsC,ftD");
        assertEquals("sA", info.getSample());
        assertNull(info.getExperiment());
        assertNull(info.getParents());
        assertEquals("dsC", info.getDataSetType());
        assertEquals("ftD", info.getFileType());
    }

    @Test
    public void testExtractFromCommentNoSample() throws Exception
    {
        DataSetUploadInfo info =
                DataSetUploadInfoHelper.extractFromCifexComment("null,eB,p1|p2|p3,dsC,ftD");
        assertNull(info.getSample());
        assertEquals("eB", info.getExperiment());
        assertEquals("p1;p2;p3", StringUtils.join(info.getParents(), ";"));
        assertEquals("dsC", info.getDataSetType());
        assertEquals("ftD", info.getFileType());
    }

    @Test
    public void testEncodeAsCommentWithSample()
    {
        String sample = "sA";
        String experiment = null;
        String[] parents = null;
        String dataSetType = "dsC";
        String fileType = "ftD";
        DataSetUploadInfo info =
                new DataSetUploadInfo(sample, experiment, parents, dataSetType, fileType);
        assertEquals("sA,null,null,dsC,ftD", DataSetUploadInfoHelper.encodeAsCifexComment(info));
    }

    @Test
    public void testEncodeAsCommentNoSample()
    {
        String sample = null;
        String experiment = "eB";
        String[] parents =
        { "p1", "p2", "p3" };
        String dataSetType = "dsC";
        String fileType = "ftD";
        DataSetUploadInfo info =
                new DataSetUploadInfo(sample, experiment, parents, dataSetType, fileType);
        assertEquals("null,eB,p1|p2|p3,dsC,ftD", DataSetUploadInfoHelper.encodeAsCifexComment(info));
    }
}
