/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.NewMaterialParserObjectFactory;

public class MaterialLoader
{

    private List<BatchRegistrationResult> results;

    private List<NewMaterial> newMaterials;

    private BisTabFileLoader<NewMaterial> tabFileLoader;

    public void load(Collection<NamedInputStream> files)
    {
        tabFileLoader =
                new BisTabFileLoader<NewMaterial>(new IParserObjectFactoryFactory<NewMaterial>()
                    {
                        @Override
                        public final IParserObjectFactory<NewMaterial> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            return new NewMaterialParserObjectFactory(propertyMapper);
                        }
                    }, false);
        newMaterials = new ArrayList<NewMaterial>();
        results = new ArrayList<BatchRegistrationResult>(files.size());
        for (final NamedInputStream file : files)
        {
            final Reader reader = file.getUnicodeReader();
            final Map<String, String> defaults = Collections.emptyMap();
            final List<NewMaterial> loadedMaterials =
                    tabFileLoader.load(new DelegatedReader(reader, file.getOriginalFilename()), defaults);
            newMaterials.addAll(loadedMaterials);
            results.add(new BatchRegistrationResult(file.getOriginalFilename(), String.format(
                    "%d material(s) found and registered.", loadedMaterials.size())));
        }
    }

    public List<BatchRegistrationResult> getResults()
    {
        return new ArrayList<BatchRegistrationResult>(results);
    }

    public List<NewMaterial> getNewMaterials()
    {
        return new ArrayList<NewMaterial>(newMaterials);
    }

}