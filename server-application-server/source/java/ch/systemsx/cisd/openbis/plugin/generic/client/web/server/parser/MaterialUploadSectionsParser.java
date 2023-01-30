/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.ExcelFileLoader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisExcelFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.ExcelFileSection;
import ch.systemsx.cisd.openbis.generic.shared.parser.FileSection;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;

/**
 * @author Pawel Glyzewski
 */
public class MaterialUploadSectionsParser
{
    public static class BatchMaterialsOperation
    {
        private final List<NewMaterialsWithTypes> materials;

        private final List<BatchRegistrationResult> resultList;

        private final String[] materialCodes;

        public BatchMaterialsOperation(List<NewMaterialsWithTypes> materials,
                List<BatchRegistrationResult> resultList, String[] materialCodes)
        {
            this.materials = materials;
            this.resultList = resultList;
            this.materialCodes = materialCodes;
        }

        public List<NewMaterialsWithTypes> getMaterials()
        {
            return materials;
        }

        public List<BatchRegistrationResult> getResultList()
        {
            return resultList;
        }

        public String[] getCodes()
        {
            return materialCodes;
        }
    }

    public static BatchMaterialsOperation prepareMaterials(final MaterialType materialType,
            final Collection<NamedInputStream> files, String excelSheetName)
    {
        final List<NewMaterialsWithTypes> newSamples = new ArrayList<NewMaterialsWithTypes>();
        final List<BatchRegistrationResult> results =
                loadMaterialsFromFiles(files, materialType, newSamples, excelSheetName);

        return new BatchMaterialsOperation(newSamples, results, parseCodes(newSamples));
    }

    private static String[] parseCodes(final List<NewMaterialsWithTypes> newMaterials)
    {
        List<String> codes = new ArrayList<String>();
        for (NewMaterialsWithTypes st : newMaterials)
        {
            for (NewMaterial s : st.getNewEntities())
            {
                codes.add(s.getCode());
            }
        }
        return codes.toArray(new String[0]);
    }

    private static List<BatchRegistrationResult> loadMaterialsFromFiles(
            Collection<NamedInputStream> uploadedFiles, MaterialType materialType,
            final List<NewMaterialsWithTypes> newMaterials, String excelSheetName)
    {
        final List<BatchRegistrationResult> results =
                new ArrayList<BatchRegistrationResult>(uploadedFiles.size());

        for (final NamedInputStream multipartFile : uploadedFiles)
        {
            final String fileName = multipartFile.getOriginalFilename();
            final String loweredFileName = fileName.toLowerCase();
            String registrationMessage = "Registration/update of %d material(s) is complete.";
            int materialCounter = 0;
            if (loweredFileName.endsWith("xls") || loweredFileName.endsWith("xlsx"))
            {
                List<ExcelFileSection> materialSections = new ArrayList<ExcelFileSection>();
                if (materialType.isDefinedInFileEntityTypeCode())
                {
                    materialSections.addAll(ExcelFileSection.extractSections(
                            multipartFile.getInputStream(), excelSheetName, loweredFileName));
                } else
                {
                    materialSections
                            .add(ExcelFileSection.createFromInputStream(
                                    multipartFile.getInputStream(), materialType.getCode(),
                                    loweredFileName));
                }
                Map<String, String> defaults = new HashMap<String, String>();
                for (ExcelFileSection fs : materialSections)
                {
                    if (fs.getSectionName().equals("DEFAULT"))
                    {
                        defaults.putAll(ExcelFileLoader.parseDefaults(fs.getSheet(), fs.getBegin(),
                                fs.getEnd()));
                    } else
                    {
                        MaterialType typeFromSection = new MaterialType();
                        typeFromSection.setCode(fs.getSectionName());
                        final BisExcelFileLoader<NewMaterial> excelFileLoader =
                                new BisExcelFileLoader<NewMaterial>(
                                        new IParserObjectFactoryFactory<NewMaterial>()
                                            {
                                                @Override
                                                public final IParserObjectFactory<NewMaterial> createFactory(
                                                        final IPropertyMapper propertyMapper)
                                                        throws ParserException
                                                {
                                                    return new NewMaterialParserObjectFactory(
                                                            propertyMapper);
                                                }
                                            }, false);
                        String sectionInFile =
                                materialSections.size() == 1 ? "" : " (section:"
                                        + fs.getSectionName() + ")";
                        final List<NewMaterial> loadedMaterials =
                                excelFileLoader.load(fs.getSheet(), fs.getBegin(), fs.getEnd(),
                                        fileName + sectionInFile, defaults);
                        if (loadedMaterials.size() > 0)
                        {
                            newMaterials.add(new NewMaterialsWithTypes(typeFromSection,
                                    loadedMaterials));
                            materialCounter += loadedMaterials.size();
                        }
                    }
                }
            } else
            {

                List<FileSection> materialSections = new ArrayList<FileSection>();
                if (materialType.isDefinedInFileEntityTypeCode())
                {
                    materialSections.addAll(FileSection.extractSections(multipartFile
                            .getUnicodeReader()));
                } else
                {
                    materialSections.add(FileSection.createFromInputStream(
                            multipartFile.getInputStream(), materialType.getCode()));
                }
                Map<String, String> defaults = new HashMap<String, String>();
                for (FileSection fs : materialSections)
                {
                    if (fs.getSectionName().equals("DEFAULT"))
                    {
                        defaults.putAll(TabFileLoader.parseDefaults(fs.getContentReader()));
                    } else
                    {
                        Reader reader = fs.getContentReader();
                        MaterialType typeFromSection = new MaterialType();
                        typeFromSection.setCode(fs.getSectionName());
                        BisTabFileLoader<NewMaterial> tabFileLoader =
                                new BisTabFileLoader<NewMaterial>(
                                        new IParserObjectFactoryFactory<NewMaterial>()
                                            {
                                                @Override
                                                public final IParserObjectFactory<NewMaterial> createFactory(
                                                        final IPropertyMapper propertyMapper)
                                                        throws ParserException
                                                {
                                                    return new NewMaterialParserObjectFactory(
                                                            propertyMapper);
                                                }
                                            }, false);
                        String sectionInFile =
                                materialSections.size() == 1 ? "" : " (section:"
                                        + fs.getSectionName() + ")";
                        final List<NewMaterial> loadedMaterials =
                                tabFileLoader.load(new DelegatedReader(reader, fileName
                                        + sectionInFile), defaults);
                        if (loadedMaterials.size() > 0)
                        {
                            newMaterials.add(new NewMaterialsWithTypes(typeFromSection,
                                    loadedMaterials));
                            materialCounter += loadedMaterials.size();
                        }
                    }
                }
            }
            results.add(new BatchRegistrationResult(fileName, String.format(
                    registrationMessage, materialCounter)));
        }
        return results;
    }
}
