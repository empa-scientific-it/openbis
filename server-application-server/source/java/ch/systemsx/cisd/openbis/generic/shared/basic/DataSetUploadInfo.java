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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.Arrays;

/**
 * @author Izabela Adamczyk
 */
public class DataSetUploadInfo
{

    private static final String PARENT_SEPARATOR = "|";

    private static final String SEPARATOR = ",";

    private String sample; // may be null

    private String experiment; // may be null

    private String[] parents; // may be null

    private String dataSetType;

    private String fileType;

    public DataSetUploadInfo()
    {
    }

    public DataSetUploadInfo(String sample, String experiment, String[] parents,
            String dataSetType, String fileType)
    {
        setSample(sample);
        setExperiment(experiment);
        setParents(parents);
        setDataSetType(dataSetType);
        setFileType(fileType);
    }

    public String getSample()
    {
        return sample;
    }

    public void setSample(String sample)
    {
        this.sample = sample;
    }

    public String getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(String dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getExperiment()
    {
        return experiment;
    }

    public void setExperiment(String experiment)
    {
        this.experiment = experiment;
    }

    public String[] getParents()
    {
        return parents;
    }

    public void setParents(String[] parents)
    {
        this.parents = parents;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (sample != null)
        {
            builder.append("sample:").append(sample).append(", ");
        }
        if (experiment != null)
        {
            builder.append("experiment:").append(experiment).append(", ");
        }
        if (parents != null && parents.length > 0)
        {
            builder.append("parent data set:").append(Arrays.asList(parents)).append(", ");
        }
        builder.append("data set type:").append(dataSetType).append(", ");
        builder.append("file type:").append(fileType);
        return builder.toString();
    }

    public static class DataSetUploadInfoHelper
    {

        enum CommentElements
        {
            SAMPLE, EXPERIMENT, PARENTS, DATA_SET_TYPE, FILE_TYPE;
        }

        public static String encodeAsCifexComment(DataSetUploadInfo info)
        {
            String[] commentElements = new String[CommentElements.values().length];
            commentElements[CommentElements.SAMPLE.ordinal()] = info.getSample();
            commentElements[CommentElements.EXPERIMENT.ordinal()] = info.getExperiment();
            commentElements[CommentElements.PARENTS.ordinal()] = tryEncodeParentsAsString(info);
            commentElements[CommentElements.DATA_SET_TYPE.ordinal()] = info.getDataSetType();
            commentElements[CommentElements.FILE_TYPE.ordinal()] = info.getFileType();
            StringBuilder commentBuilder = new StringBuilder();
            for (String el : commentElements)
            {
                commentBuilder.append(el);
                commentBuilder.append(SEPARATOR);
            }
            return cutOffLastSeparator(commentBuilder, SEPARATOR);
        }

        public static DataSetUploadInfo extractFromCifexComment(String comment)
        {
            String[] commentElements = comment.split(asEscapedRegexp(SEPARATOR));
            DataSetUploadInfo result = new DataSetUploadInfo();
            result.setSample(nullify(commentElements[CommentElements.SAMPLE.ordinal()]));
            result.setExperiment(nullify(commentElements[CommentElements.EXPERIMENT.ordinal()]));
            result
                    .setParents(tryExtractParentsFromString(nullify(commentElements[CommentElements.PARENTS
                            .ordinal()])));
            result.setDataSetType(commentElements[CommentElements.DATA_SET_TYPE.ordinal()]);
            result.setFileType(commentElements[CommentElements.FILE_TYPE.ordinal()]);
            return result;
        }

        private static String nullify(String string)
        {
            return string.equals("null") ? null : string;
        }

        private static String[] tryExtractParentsFromString(String parents)
        {
            return parents == null ? null : parents.split(asEscapedRegexp(PARENT_SEPARATOR));
        }

        private static String tryEncodeParentsAsString(DataSetUploadInfo info)
        {
            if (info.getParents() == null)
            {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (String parent : info.getParents())
            {
                sb.append(parent);
                sb.append(PARENT_SEPARATOR);
            }
            return cutOffLastSeparator(sb, PARENT_SEPARATOR);
        }

        private static String asEscapedRegexp(String str)
        {
            return "\\Q" + str + "\\E";
        }

        private static String cutOffLastSeparator(StringBuilder builder, String sep)
        {
            // either builder is empty or it should end with separator
            if (builder.length() == 0)
            {
                return "";
            } else
            {
                assert builder.lastIndexOf(sep) == builder.length() - sep.length();
                return builder.substring(0, builder.length() - sep.length());
            }
        }
    }

}
