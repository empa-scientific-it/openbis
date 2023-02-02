/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser.CsvFeatureVectorParserConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Converts feature vectors from CSV files to CanonicaFeatureVector objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CsvToCanonicalFeatureVector
{
    private final CsvFeatureVectorParser parser;

    private final Geometry plateGeometry;

    public CsvToCanonicalFeatureVector(DatasetFileLines fileLines,
            CsvFeatureVectorParserConfiguration config, Geometry plateGeometry)
    {
        this(fileLines, config, plateGeometry.getNumberOfRows(), plateGeometry.getNumberOfColumns());
    }

    public CsvToCanonicalFeatureVector(DatasetFileLines fileLines,
            CsvFeatureVectorParserConfiguration config, int maxRow, int maxCol)
    {
        this.parser = new CsvFeatureVectorParser(fileLines, config);
        this.plateGeometry = Geometry.createFromRowColDimensions(maxRow, maxCol);
    }

    public List<CanonicalFeatureVector> convert()
    {
        List<FeatureDefinition> featureDefinitions = parser.parse();
        List<CanonicalFeatureVector> result = new ArrayList<CanonicalFeatureVector>();
        for (FeatureDefinition featureDefinition : featureDefinitions)
        {
            result.add(featureDefinition.getCanonicalFeatureVector(plateGeometry));
        }
        return result;
    }

}
