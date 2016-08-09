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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.plugins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.V3DataSetContentResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.V3Resolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpFile;

/**
 * Resolves paths of type "/SAMPLE_TYPE/SAMPLE_CODE/DATASET_CODE/data set content <br>
 * / - list all listable sample types<br>
 * /PLATE - list all sample codes of samples of type plate<br>
 * /PLATE/TEST-1 - list all datasets belonging to some sample of type PLATE and code TEST-1<br>
 * /PLATE/TEST-1/20183213123-43 - list file contents of data set 20183213123-43 <br>
 * /PLATE/TEST-1/20183213123-43/original - list contents of a directory inside a data set<br>
 * /PLATE/TEST-1/20183213123-43/original/file.txt - download content of file.txt<br>
 * 
 * @author Jakub Straszewski
 */
public class V3SampleTypeResolver implements V3Resolver
{
    @Override
    public V3FtpFile resolve(String fullPath, String[] subPath, FtpPathResolverContext context)
    {
        if (subPath.length == 0)
        {
            return listSampleTypes(fullPath, context);
        }

        String sampleType = subPath[0];
        if (subPath.length == 1)
        {
            return listSamplesOfGivenType(fullPath, sampleType, context);
        }

        String sampleCode = subPath[1];
        if (subPath.length == 2)
        {
            return listDataSetsForGivenSampleTypeAndCode(fullPath, sampleType, sampleCode, context);
        }

        String dataSetCode = subPath[2];
        String[] remaining = Arrays.copyOfRange(subPath, 3, subPath.length);
        return new V3DataSetContentResolver(dataSetCode).resolve(fullPath, remaining, context);
    }

    private V3FtpFile listDataSetsForGivenSampleTypeAndCode(String fullPath, String sampleTypeCode, String sampleCode, FtpPathResolverContext context)
    {
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withSample().withType().withCode().thatEquals(sampleTypeCode);
        searchCriteria.withSample().withCode().thatEquals(sampleCode);
        List<DataSet> dataSets = context.getV3Api().searchDataSets(context.getSessionToken(), searchCriteria, new DataSetFetchOptions()).getObjects();

        V3FtpDirectoryResponse result = new V3FtpDirectoryResponse(fullPath);
        for (DataSet dataSet : dataSets)
        {
            result.addDirectory(dataSet.getCode());
        }
        return result;
    }

    private V3FtpFile listSamplesOfGivenType(String fullPath, String sampleType, FtpPathResolverContext context)
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(sampleType);
        List<Sample> samples = context.getV3Api().searchSamples(context.getSessionToken(), searchCriteria, new SampleFetchOptions()).getObjects();

        // as codes can overlap, we want to create only one entry per code
        HashSet<String> sampleCodes = new HashSet<>();

        V3FtpDirectoryResponse result = new V3FtpDirectoryResponse(fullPath);
        for (Sample sample : samples)
        {
            if (false == sampleCodes.contains(sample.getCode()))
            {
                result.addDirectory(sample.getCode());
                sampleCodes.add(sample.getCode());
            }
        }
        return result;
    }

    private V3FtpFile listSampleTypes(String fullPath, FtpPathResolverContext context)
    {
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withListable().thatEquals(true);
        List<SampleType> sampleTypes =
                context.getV3Api().searchSampleTypes(context.getSessionToken(), searchCriteria, new SampleTypeFetchOptions())
                        .getObjects();

        V3FtpDirectoryResponse response = new V3FtpDirectoryResponse(fullPath);
        for (SampleType type : sampleTypes)
        {
            response.addDirectory(type.getCode());
        }
        return response;
    }

}