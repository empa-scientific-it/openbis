/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateSampleTypeGui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateSampleTypeRmi;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class SampleTypeBuilder implements Builder<SampleType>
{

    private String code;

    private String description;

    private boolean listable;

    private boolean showsContainer;

    private boolean showsParents;

    private boolean hasUniqueSubcodes;

    private boolean generatesCodes;

    private boolean showsParentMetadata;

    private String generatedCodePrefix;

    private Collection<PropertyTypeAssignment> propertyTypeAssignments;

    private Script validationScript;

    public SampleTypeBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
        this.description = "";
        this.listable = true;
        this.showsContainer = false;
        this.showsParents = true;
        this.hasUniqueSubcodes = false;
        this.generatesCodes = false;
        this.showsParentMetadata = false;
        this.generatedCodePrefix = "S";
        this.propertyTypeAssignments = new HashSet<PropertyTypeAssignment>();
    }

    public SampleTypeBuilder thatIsListable()
    {
        this.listable = true;
        return this;
    }

    public SampleTypeBuilder thatIsNotListable()
    {
        this.listable = false;
        return this;
    }

    public SampleTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    public SampleTypeBuilder withCodePrefix(String prefix)
    {
        this.code = prefix + "_" + UUID.randomUUID().toString();
        return this;
    }

    public SampleTypeBuilder thatShowsParents()
    {
        this.showsParents = true;
        return this;
    }

    public SampleTypeBuilder thatCanBeComponent()
    {
        this.showsContainer = true;
        return this;
    }

    public SampleTypeBuilder thatHasUniqueSubcodes()
    {
        this.hasUniqueSubcodes = true;
        return this;
    }

    public SampleTypeBuilder withPrefix(String prefix)
    {
        this.generatedCodePrefix = prefix;
        return this;
    }

    public SampleTypeBuilder validatedBy(Script script)
    {
        this.validationScript = script;
        return this;
    }

    public SampleTypeBuilder thatGeneratesCodes()
    {
        this.generatesCodes = true;
        return this;
    }

    @Override
    public SampleType build(Application openbis, Ui ui)
    {
        SampleType type =
                new SampleTypeDsl(code, description, listable, showsContainer, showsParents,
                        hasUniqueSubcodes, generatesCodes, showsParentMetadata,
                        generatedCodePrefix, propertyTypeAssignments, validationScript);
        if (Ui.WEB.equals(ui))
        {
            return openbis.execute(new CreateSampleTypeGui(type));
        } else if (Ui.PUBLIC_API.equals(ui))
        {
            return openbis.execute(new CreateSampleTypeRmi(type));
        } else
        {
            return type;
        }
    }
}
