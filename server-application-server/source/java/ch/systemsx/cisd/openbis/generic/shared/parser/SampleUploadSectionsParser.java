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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.ExcelFileLoader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Helps uploading samples of multiple types from one file. Parses the file.
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 */
public class SampleUploadSectionsParser
{
    public static class BatchSamplesOperation
    {
        private final List<NewSamplesWithTypes> samples;

        private final List<BatchRegistrationResult> resultList;

        private final String[] sampleCodes;

        public BatchSamplesOperation(List<NewSamplesWithTypes> samples,
                List<BatchRegistrationResult> resultList, String[] sampleCodes)
        {
            this.samples = samples;
            this.resultList = resultList;
            this.sampleCodes = sampleCodes;
        }

        public List<NewSamplesWithTypes> getSamples()
        {
            return samples;
        }

        public List<BatchRegistrationResult> getResultList()
        {
            return resultList;
        }

        public String[] getCodes()
        {
            return sampleCodes;
        }
    }

    public interface SampleCodeGenerator
    {
        /** generates 'size' codes for new samples */
        List<String> generateCodes(int size);
    }

    /*
     * This overloaded method has been kept to provide backwards compatibility for a couple of older uses
     */
    public static BatchSamplesOperation prepareSamples(
            final SampleType sampleType,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final Collection<NamedInputStream> files, String defaultGroupIdentifier,
            final SampleCodeGenerator sampleCodeGeneratorOrNull, final boolean allowExperiments,
            String excelSheetName, BatchOperationKind operationKind)
    {
        return prepareSamples(false,
                sampleType,
                spaceIdentifierSilentOverrideOrNull,
                experimentIdentifierSilentOverrideOrNull,
                files, defaultGroupIdentifier,
                sampleCodeGeneratorOrNull, allowExperiments,
                excelSheetName, operationKind);
    }

    public static BatchSamplesOperation prepareSamples(
            final boolean projectSamplesEnabled,
            final SampleType sampleType,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final Collection<NamedInputStream> files, String defaultGroupIdentifier,
            final SampleCodeGenerator sampleCodeGeneratorOrNull, final boolean allowExperiments,
            String excelSheetName, BatchOperationKind operationKind)
    {
        final List<NewSamplesWithTypes> newSamples = new ArrayList<NewSamplesWithTypes>();
        boolean isAutoGenerateCodes = (sampleCodeGeneratorOrNull != null);
        final List<BatchRegistrationResult> results =
                loadSamplesFromFiles(files, sampleType, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull,
                        isAutoGenerateCodes,
                        newSamples,
                        allowExperiments, excelSheetName, operationKind);

        if (defaultGroupIdentifier != null)
        {
            switch (operationKind)
            {
                case REGISTRATION:
                    if (isAutoGenerateCodes)
                    {
                        generateIdentifiers(projectSamplesEnabled, defaultGroupIdentifier, sampleCodeGeneratorOrNull,
                                isAutoGenerateCodes, newSamples);
                    }
                    break;
                case UPDATE:
                    fillIdentifiers(defaultGroupIdentifier, newSamples);
                    break;
            }
        }
        return new BatchSamplesOperation(newSamples, results, parseCodes(newSamples));
    }

    private static String[] parseCodes(final List<NewSamplesWithTypes> newSamples)
    {
        List<String> codes = new ArrayList<String>();
        for (NewSamplesWithTypes st : newSamples)
        {
            for (NewSample s : st.getNewEntities())
            {
                codes.add(SampleIdentifierFactory.parse(s.getIdentifier()).getSampleCode());
            }
        }
        return codes.toArray(new String[0]);
    }

    private static BisTabFileLoader<NewSample> createSampleLoader(final SampleType sampleType,
            final boolean isAutoGenerateCodes, final boolean allowExperiments,
            final BatchOperationKind operationKind)
    {
        final BisTabFileLoader<NewSample> tabFileLoader =
                new BisTabFileLoader<NewSample>(new IParserObjectFactoryFactory<NewSample>()
                    {
                        @Override
                        public final IParserObjectFactory<NewSample> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            switch (operationKind)
                            {
                                case REGISTRATION:
                                    return new NewSampleParserObjectFactory(sampleType,
                                            propertyMapper, isAutoGenerateCodes == false,
                                            allowExperiments);
                                case UPDATE:
                                    return new UpdatedSampleParserObjectFactory(sampleType,
                                            propertyMapper, isAutoGenerateCodes == false,
                                            allowExperiments);
                            }
                            throw new UnsupportedOperationException(operationKind
                                    + " is not supported");
                        }
                    }, true);
        return tabFileLoader;
    }

    private static BisExcelFileLoader<NewSample> createSampleLoaderFromExcel(
            final SampleType sampleType, final boolean isAutoGenerateCodes,
            final boolean allowExperiments, final BatchOperationKind operationKind)
    {
        final BisExcelFileLoader<NewSample> tabFileLoader =
                new BisExcelFileLoader<NewSample>(new IParserObjectFactoryFactory<NewSample>()
                    {
                        @Override
                        public final IParserObjectFactory<NewSample> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            switch (operationKind)
                            {
                                case REGISTRATION:
                                    return new NewSampleParserObjectFactory(sampleType,
                                            propertyMapper, isAutoGenerateCodes == false,
                                            allowExperiments);
                                case UPDATE:
                                    return new UpdatedSampleParserObjectFactory(sampleType,
                                            propertyMapper, isAutoGenerateCodes == false,
                                            allowExperiments);
                            }
                            throw new UnsupportedOperationException(operationKind
                                    + " is not supported");
                        }
                    }, true);
        return tabFileLoader;
    }

    public static String getIdentifierOrNull(String identifierOrNullOrDelete)
    {
        switch (identifierOrNullOrDelete)
        {
            case "--DELETE--":
                return null;
            case "__DELETE__":
                return null;
            default:
                return identifierOrNullOrDelete;
        }
    }

    private static void setSilentOverrides(String spaceIdentifierSilentOverrideOrNull,
            String experimentIdentifierSilentOverrideOrNull, List<NewSample> newSamples)
    {
        for (NewSample newSample : newSamples)
        {
            if (spaceIdentifierSilentOverrideOrNull != null)
            {
                if (newSample.getIdentifier() != null)
                {
                    int endOfSpaceIdentfier = StringUtils.ordinalIndexOf(newSample.getIdentifier(), "/", 2);
                    String sampleIdentifierSilentOverrideOrNull =
                            spaceIdentifierSilentOverrideOrNull + newSample.getIdentifier().substring(endOfSpaceIdentfier);
                    newSample.setIdentifier(sampleIdentifierSilentOverrideOrNull);
                }
                newSample.setDefaultSpaceIdentifier(getIdentifierOrNull(spaceIdentifierSilentOverrideOrNull));
            }
            if (experimentIdentifierSilentOverrideOrNull != null)
            {
                newSample.setExperimentIdentifier(getIdentifierOrNull(experimentIdentifierSilentOverrideOrNull));
            }
        }
    }

    private static List<BatchRegistrationResult> loadSamplesFromFiles(
            Collection<NamedInputStream> uploadedFiles, SampleType sampleType,
            String spaceIdentifierSilentOverrideOrNull,
            String experimentIdentifierSilentOverrideOrNull,
            boolean isAutoGenerateCodes, final List<NewSamplesWithTypes> newSamples,
            boolean allowExperiments, String excelSheetName, BatchOperationKind operationKind)
    {
        final List<BatchRegistrationResult> results =
                new ArrayList<BatchRegistrationResult>(uploadedFiles.size());
        for (final NamedInputStream multipartFile : uploadedFiles)
        {
            final String fileName = multipartFile.getOriginalFilename();
            final String loweredFileName = fileName.toLowerCase();
            if (loweredFileName.endsWith("xls") || loweredFileName.endsWith("xlsx"))
            {
                List<ExcelFileSection> sampleSections = new ArrayList<ExcelFileSection>();
                if (sampleType.isDefinedInFileEntityTypeCode())
                {
                    sampleSections.addAll(ExcelFileSection.extractSections(
                            multipartFile.getInputStream(), excelSheetName, loweredFileName));
                } else
                {
                    sampleSections.add(ExcelFileSection.createFromInputStream(
                            multipartFile.getInputStream(), sampleType.getCode(), loweredFileName));
                }
                int sampleCounter = 0;
                Map<String, String> defaults = new HashMap<String, String>();
                for (ExcelFileSection fs : sampleSections)
                {
                    if (fs.getSectionName().equals("DEFAULT"))
                    {
                        defaults.putAll(ExcelFileLoader.parseDefaults(fs.getSheet(), fs.getBegin(),
                                fs.getEnd()));
                    } else
                    {
                        SampleType typeFromSection = new SampleType();
                        typeFromSection.setCode(fs.getSectionName());
                        final BisExcelFileLoader<NewSample> excelFileLoader =
                                createSampleLoaderFromExcel(typeFromSection, isAutoGenerateCodes,
                                        allowExperiments, operationKind);
                        String sectionInFile =
                                sampleSections.size() == 1 ? ""
                                        : " (section:"
                                                + fs.getSectionName() + ")";
                        final List<NewSample> loadedSamples =
                                excelFileLoader.load(fs.getSheet(), fs.getBegin(), fs.getEnd(),
                                        fileName + sectionInFile, defaults);
                        setSilentOverrides(spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull, loadedSamples);
                        if (loadedSamples.size() > 0)
                        {
                            newSamples.add(new NewSamplesWithTypes(typeFromSection, loadedSamples));
                            sampleCounter += loadedSamples.size();
                        }
                    }
                }
                results.add(new BatchRegistrationResult(fileName, String.format(
                        "%s of %d sample(s) is complete.", operationKind.getDescription(),
                        sampleCounter)));
            } else
            {
                List<FileSection> sampleSections = new ArrayList<FileSection>();
                if (sampleType.isDefinedInFileEntityTypeCode())
                {
                    sampleSections.addAll(FileSection.extractSections(multipartFile
                            .getUnicodeReader()));
                } else
                {
                    sampleSections.add(FileSection.createFromInputStream(
                            multipartFile.getInputStream(), sampleType.getCode()));
                }
                int sampleCounter = 0;
                Map<String, String> defaults = new HashMap<String, String>();
                for (FileSection fs : sampleSections)
                {
                    if (fs.getSectionName().equals("DEFAULT"))
                    {
                        defaults.putAll(TabFileLoader.parseDefaults(fs.getContentReader()));
                    } else
                    {
                        Reader reader = fs.getContentReader();
                        SampleType typeFromSection = new SampleType();
                        typeFromSection.setCode(fs.getSectionName());
                        final BisTabFileLoader<NewSample> tabFileLoader =
                                createSampleLoader(typeFromSection, isAutoGenerateCodes,
                                        allowExperiments, operationKind);
                        String sectionInFile =
                                sampleSections.size() == 1 ? ""
                                        : " (section:"
                                                + fs.getSectionName() + ")";
                        final List<NewSample> loadedSamples =
                                tabFileLoader.load(new DelegatedReader(reader, fileName
                                        + sectionInFile), defaults);
                        setSilentOverrides(spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull, loadedSamples);
                        if (loadedSamples.size() > 0)
                        {
                            newSamples.add(new NewSamplesWithTypes(typeFromSection, loadedSamples));
                            sampleCounter += loadedSamples.size();
                        }
                    }
                }
                results.add(new BatchRegistrationResult(fileName, String.format(
                        "%s of %d sample(s) is complete.", operationKind.getDescription(),
                        sampleCounter)));
            }
        }
        return results;
    }

    static void generateIdentifiers(boolean projectSamplesEnabled,
            String defaultGroupIdentifier,
            SampleCodeGenerator sampleCodeGenerator, boolean isAutoGenerateCodes,
            List<NewSamplesWithTypes> newSamplesWithTypes)
    {
        assert sampleCodeGenerator != null;
        assert isAutoGenerateCodes == true;
        for (NewSamplesWithTypes st : newSamplesWithTypes)
        {
            final List<NewSample> newSamples = st.getNewEntities();
            List<String> codes = sampleCodeGenerator.generateCodes(newSamples.size());
            for (int i = 0; i < newSamples.size(); i++)
            {
                NewSample sample = newSamples.get(i);
                String spaceCodeOrNull = null;
                if (StringUtils.isBlank(sample.getDefaultSpaceIdentifier()))
                {
                    spaceCodeOrNull = defaultGroupIdentifier;
                } else
                {
                    spaceCodeOrNull = sample.getDefaultSpaceIdentifier();
                }
                spaceCodeOrNull = spaceCodeOrNull.substring(1);
                String projectCodeOrNull = null;
                if (projectSamplesEnabled && StringUtils.isNotBlank(sample.getExperimentIdentifier()))
                {
                    String[] experimentIdentifierParts = sample.getExperimentIdentifier().split("/");
                    if (experimentIdentifierParts.length != 4)
                    {
                        throw new UserFailureException("Incorrect format for the experiment identifier: " + sample.getExperimentIdentifier());
                    }
                    spaceCodeOrNull = experimentIdentifierParts[experimentIdentifierParts.length - 3];
                    projectCodeOrNull = experimentIdentifierParts[experimentIdentifierParts.length - 2];
                }
                sample.setIdentifier(createIdentifier(spaceCodeOrNull, projectCodeOrNull, codes.get(i)));
            }
        }
    }

    private static String createIdentifier(String spaceCodeOrNull, String projectCodeOrNull, String sampleCode)
    {
        StringBuilder builder = new StringBuilder("/");
        if (spaceCodeOrNull != null)
        {
            builder.append(spaceCodeOrNull).append("/");
        }
        if (projectCodeOrNull != null)
        {
            builder.append(projectCodeOrNull).append("/");
        }
        return builder.append(sampleCode).toString();
    }

    private static void fillIdentifiers(String defaultGroupIdentifier,
            List<NewSamplesWithTypes> newSamplesWithTypes)
    {
        for (NewSamplesWithTypes st : newSamplesWithTypes)
        {
            final List<NewSample> newSamples = st.getNewEntities();
            for (int i = 0; i < newSamples.size(); i++)
            {
                final String identifierFromFile = newSamples.get(i).getIdentifier();
                // Leave identifier specified in the file if it contains information about group,
                // otherwise fill default group.
                if (identifierFromFile.contains("/"))
                {
                    continue;
                } else
                {
                    newSamples.get(i).setIdentifier(
                            defaultGroupIdentifier + "/" + identifierFromFile);
                }
            }
        }
    }
}
