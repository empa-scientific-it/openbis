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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Izabela Adamczyk
 */
public class DataSetSearchCriterion implements IsSerializable
{
    private DataSetSearchField field;

    private String value;

    public static class DataSetSearchField implements IsSerializable
    {
        private DataSetSearchFieldKind kind;

        private String propertyCodeOrNull;

        private List<String> allExperimentPropertyCodesOrNull;

        private List<String> allSamplePropertyCodesOrNull;

        public static DataSetSearchField createAnyField(List<String> allExperimentPropertyCodes,
                List<String> allSamplePropertyCodes)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.ANY_FIELD, null,
                    allExperimentPropertyCodes, allSamplePropertyCodes);
        }

        public static DataSetSearchField createAnyExperimentProperty(
                List<String> allExperimentPropertyCodes)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.ANY_EXPERIMENT_PROPERTY, null,
                    allExperimentPropertyCodes, null);
        }

        public static DataSetSearchField createAnySampleProperty(List<String> allSamplePropertyCodes)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.ANY_SAMPLE_PROPERTY, null, null,
                    allSamplePropertyCodes);
        }

        public static DataSetSearchField createExperimentProperty(String propertyCode)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.EXPERIMENT_PROPERTY, propertyCode);
        }

        public static DataSetSearchField createSampleProperty(String propertyCode)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.SAMPLE_PROPERTY, propertyCode);
        }

        public static DataSetSearchField createSimpleField(DataSetSearchFieldKind fieldKind)
        {
            assert fieldKind.isComplex == false : "only simple field can be created with this method";

            return new DataSetSearchField(fieldKind, null);
        }

        // GWT only
        private DataSetSearchField()
        {
            this(null, null);
        }

        private DataSetSearchField(DataSetSearchFieldKind kind, String propertyCodeOrNull)
        {
            this(kind, propertyCodeOrNull, null, null);
        }

        private DataSetSearchField(DataSetSearchFieldKind kind, String propertyCodeOrNull,
                List<String> allExperimentPropertyCodesOrNull,
                List<String> allSamplePropertyCodesOrNull)
        {
            this.kind = kind;
            this.propertyCodeOrNull = propertyCodeOrNull;
            this.allExperimentPropertyCodesOrNull = allExperimentPropertyCodesOrNull;
            this.allSamplePropertyCodesOrNull = allSamplePropertyCodesOrNull;
        }

        public DataSetSearchFieldKind getKind()
        {
            return kind;
        }

        public String getPropertyCode()
        {
            assert kind == DataSetSearchFieldKind.EXPERIMENT_PROPERTY
                    || kind == DataSetSearchFieldKind.SAMPLE_PROPERTY;
            return propertyCodeOrNull;
        }

        public List<String> getAllExperimentPropertyCodes()
        {
            assert kind == DataSetSearchFieldKind.ANY_EXPERIMENT_PROPERTY
                    || kind == DataSetSearchFieldKind.ANY_FIELD;
            return allExperimentPropertyCodesOrNull;
        }

        public List<String> getAllSamplePropertyCodesOrNull()
        {
            assert kind == DataSetSearchFieldKind.ANY_SAMPLE_PROPERTY
                    || kind == DataSetSearchFieldKind.ANY_FIELD;
            return allSamplePropertyCodesOrNull;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(getKind());
            if (getKind().equals(DataSetSearchFieldKind.EXPERIMENT_PROPERTY)
                    || getKind().equals(DataSetSearchFieldKind.SAMPLE_PROPERTY))
            {
                sb.append(".");
                sb.append(getPropertyCode());
            }
            return sb.toString();
        }

    }

    public enum DataSetSearchFieldKind implements IsSerializable
    {

        ANY_FIELD("Any Field", true),

        ANY_EXPERIMENT_PROPERTY("Any Experiment Property", true),

        ANY_SAMPLE_PROPERTY("Any Sample Property", true),

        DATA_SET_CODE("Data Set Code"),

        DATA_SET_TYPE("Data Set Type"),

        EXPERIMENT("Experiment Code"),

        EXPERIMENT_TYPE("Experiment Type"),

        FILE_TYPE("File Type"),

        GROUP("Group Code"),

        PROJECT("Project Code"),

        SAMPLE("Sample Code"),

        SAMPLE_TYPE("Sample Type"),

        EXPERIMENT_PROPERTY("Experiment Property", true),

        SAMPLE_PROPERTY("Sample Property", true);

        private final String description;

        // if field is complex, it needs some additional information to be interpreted (e.g.
        // property code)
        private final boolean isComplex;

        private DataSetSearchFieldKind(String description)
        {
            this(description, false);
        }

        private DataSetSearchFieldKind(String description, boolean isComplex)
        {
            this.description = description;
            this.isComplex = isComplex;
        }

        public String description()
        {
            return description;
        }

        public static List<DataSetSearchFieldKind> getSimpleFields()
        {
            List<DataSetSearchFieldKind> result = new ArrayList<DataSetSearchFieldKind>();
            for (DataSetSearchFieldKind field : DataSetSearchFieldKind.values())
            {
                if (field.isComplex == false)
                {
                    result.add(field);

                }
            }
            return result;
        }
    }

    public DataSetSearchCriterion()
    {
    }

    public DataSetSearchCriterion(DataSetSearchField field, String value)
    {
        this.field = field;
        this.value = value;
    }

    public DataSetSearchField getField()
    {
        return field;
    }

    public void setField(DataSetSearchField field)
    {
        this.field = field;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getField());
        sb.append(": ");
        sb.append(getValue());
        return sb.toString();
    }

}
