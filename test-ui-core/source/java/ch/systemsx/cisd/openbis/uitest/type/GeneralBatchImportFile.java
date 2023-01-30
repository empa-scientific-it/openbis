/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;

import ch.systemsx.cisd.openbis.uitest.dsl.IdentifiedBy;

/**
 * @author anttil
 */
public class GeneralBatchImportFile implements ImportFile
{

    private List<Sample> samples;

    private Set<SampleType> containedTypes;

    private Set<SampleType> containerTypes;

    private Map<Sample, IdentifiedBy> identifierTypes;

    private List<Material> materials;

    private Set<MaterialType> materialTypes;

    private Space defaultSpace;

    private boolean hasSampleContainerColumn;

    private String fileName;

    public GeneralBatchImportFile(Space defaultSpace, boolean hasSampleContainerColumn,
            String fileName)
    {
        this.samples = new ArrayList<Sample>();
        this.containedTypes = new HashSet<SampleType>();
        this.containerTypes = new HashSet<SampleType>();
        this.identifierTypes = new HashMap<Sample, IdentifiedBy>();
        this.materials = new ArrayList<Material>();
        this.defaultSpace = defaultSpace;
        this.hasSampleContainerColumn = hasSampleContainerColumn;
        this.fileName = fileName;
    }

    @Override
    public String getPath()
    {
        WorkBookWriter workbook = new WorkBookWriter(fileName);
        Sheet sampleSheet = workbook.createSheet("samples");

        if (defaultSpace != null)
        {
            workbook.write(sampleSheet, "[DEFAULT]");
            workbook.write(sampleSheet, "DEFAULT_SPACE", defaultSpace.getCode());
        }

        List<SampleType> types = new ArrayList<SampleType>(containerTypes);
        types.addAll(containedTypes);

        for (SampleType type : types)
        {
            workbook.write(sampleSheet, "[" + type.getCode() + "]");

            Header header = new Header(type, hasSampleContainerColumn);
            workbook.write(sampleSheet, header.getLabels().toArray(new String[0]));

            for (Sample sample : samples)
            {
                if (sample.getType().equals(type))
                {
                    workbook.write(sampleSheet, header.getValuesFor(sample, identifierTypes)
                            .toArray(new String[0]));
                }
            }
        }

        File f = workbook.writeToDisk();
        return f.getAbsolutePath();
    }

    @Override
    public void add(Sample sample, IdentifiedBy idType)
    {
        samples.add(sample);
        identifierTypes.put(sample, idType);
        if (sample.getType().isShowContainer())
        {
            containedTypes.add(sample.getType());
        } else
        {
            containerTypes.add(sample.getType());
        }
    }

    @Override
    public void add(Material material)
    {
        materials.add(material);
        materialTypes.add(material.getType());
    }

    @Override
    public void add(Experiment experiment)
    {
        throw new UnsupportedOperationException(
                "This import file type does not support experiments");
    }

    @Override
    public int getTypeCount()
    {
        return containedTypes.size() + containerTypes.size();
    }

}
