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

package ch.systemsx.cisd.openbis.uitest.page;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.rmi.Identifiers;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class SampleBatchRegistrationFile
{

    private final List<List<Sample>> samples;

    private File file;

    public SampleBatchRegistrationFile(List<Sample> sampleList)
    {
        Map<String, List<Sample>> map = new HashMap<String, List<Sample>>();
        for (Sample sample : sampleList)
        {
            List<Sample> samplesOfType = map.get(sample.getType().getCode());
            if (samplesOfType == null)
            {
                samplesOfType = new ArrayList<Sample>();
                map.put(sample.getType().getCode(), samplesOfType);
            }
            samplesOfType.add(sample);
        }
        samples = new ArrayList<List<Sample>>(map.values());

        // Containers have to be specified before components.
        Collections.sort(samples, new Comparator<List<Sample>>()
            {
                @Override
                public int compare(List<Sample> o1, List<Sample> o2)
                {
                    boolean isContainer1 = o1.get(0).getType().isShowContainer();
                    boolean isContainer2 = o2.get(0).getType().isShowContainer();

                    if (isContainer1 == isContainer2)
                    {
                        return 0;
                    } else
                    {
                        return isContainer1 ? -1 : 1;
                    }
                }
            });

    }

    public boolean hasMultipleTypes()
    {
        return samples.size() > 1;
    }

    public List<Integer> getSizes()
    {
        List<Integer> sizes = new ArrayList<Integer>();
        for (List<Sample> list : samples)
        {
            sizes.add(list.size());
        }
        return sizes;
    }

    public void writeToDisk() throws IOException
    {
        File dir = new File("targets/tmp");
        dir.mkdirs();

        file = new File(dir, UUID.randomUUID().toString() + "_sample_batch_registration.csv");
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (List<Sample> samplesOfType : samples)
        {
            if (hasMultipleTypes())
            {
                writer.write("[" + samplesOfType.get(0).getType().getCode() + "]");
                writer.newLine();
            }

            String header = "identifier\tcontainer\tparents\texperiment\tdefault_space";
            List<String> propertyColumns = new ArrayList<String>();

            for (PropertyTypeAssignment assignment : samplesOfType.get(0).getType()
                    .getPropertyTypeAssignments())
            {
                propertyColumns.add(assignment.getPropertyType().getCode());
            }

            for (String column : propertyColumns)
            {
                header += "\t" + column;
            }

            writer.write(header);
            writer.newLine();
            for (Sample sample : samplesOfType)
            {
                writer.write(sample.getCode() + "\t");

                if (sample.getContainer() != null)
                {
                    writer.write(Identifiers.get(sample.getContainer()).toString());
                }

                writer.write("\t\t");
                if (sample.getExperiment() != null)
                {
                    writer.write(Identifiers.get(sample.getExperiment()).toString());
                }
                writer.write("\t");

                if (sample.getSpace() != null)
                {
                    writer.write(sample.getSpace().getCode());
                } else if (sample.getExperiment() != null)
                {
                    writer.write(sample.getExperiment().getProject().getSpace().getCode());
                }

                for (PropertyTypeAssignment assignment : samplesOfType.get(0).getType()
                        .getPropertyTypeAssignments())
                {
                    writer.write("\t" + assignment.getPropertyType().getCode() + ":"
                            + UUID.randomUUID().toString());
                }

                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }

    public String getAbsolutePath()
    {
        return file.getAbsolutePath();
    }
}
