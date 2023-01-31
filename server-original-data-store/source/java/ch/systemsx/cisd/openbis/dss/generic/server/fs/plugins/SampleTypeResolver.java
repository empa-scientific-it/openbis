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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins;

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
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver.DataSetContentResolver;

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
public class SampleTypeResolver implements IResolverPlugin
{
    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        if (subPath.length == 0)
        {
            return listSampleTypes(context);
        }

        String sampleType = subPath[0];
        if (subPath.length == 1)
        {
            return listSamplesOfGivenType(sampleType, context);
        }

        String sampleCode = subPath[1];
        if (subPath.length == 2)
        {
            return listDataSetsForGivenSampleTypeAndCode(sampleType, sampleCode, context);
        }

        String dataSetCode = subPath[2];
        String[] remaining = Arrays.copyOfRange(subPath, 3, subPath.length);
        return new DataSetContentResolver(dataSetCode).resolve(remaining, context);
    }

    private IFileSystemViewResponse listDataSetsForGivenSampleTypeAndCode(String sampleTypeCode, String sampleCode, IResolverContext context)
    {
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withSample().withType().withCode().thatEquals(sampleTypeCode);
        searchCriteria.withSample().withCode().thatEquals(sampleCode);
        List<DataSet> dataSets = context.getApi().searchDataSets(context.getSessionToken(), searchCriteria, new DataSetFetchOptions()).getObjects();

        IDirectoryResponse result = context.createDirectoryResponse();
        for (DataSet dataSet : dataSets)
        {
            result.addDirectory(dataSet.getCode(), dataSet.getModificationDate());
        }
        return result;
    }

    private IFileSystemViewResponse listSamplesOfGivenType(String sampleType, IResolverContext context)
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(sampleType);
        List<Sample> samples = context.getApi().searchSamples(context.getSessionToken(), searchCriteria, new SampleFetchOptions()).getObjects();

        // as codes can overlap, we want to create only one entry per code
        HashSet<String> sampleCodes = new HashSet<>();

        IDirectoryResponse result = context.createDirectoryResponse();
        for (Sample sample : samples)
        {
            if (false == sampleCodes.contains(sample.getCode()))
            {
                result.addDirectory(sample.getCode(), sample.getModificationDate());
                sampleCodes.add(sample.getCode());
            }
        }
        return result;
    }

    private IFileSystemViewResponse listSampleTypes(IResolverContext context)
    {
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withListable().thatEquals(true);
        List<SampleType> sampleTypes =
                context.getApi().searchSampleTypes(context.getSessionToken(), searchCriteria, new SampleTypeFetchOptions())
                        .getObjects();

        IDirectoryResponse response = context.createDirectoryResponse();
        for (SampleType type : sampleTypes)
        {
            response.addDirectory(type.getCode());
        }
        return response;
    }

    @Override
    public void initialize(String name, String code)
    {
    }

}