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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * @author pkupczyk
 */
public class PropertyTypeAssignmentGridAssignmentsHolder
{

    private NewETNewPTAssigments originalAssignments;

    private NewETNewPTAssigments assignments;

    public PropertyTypeAssignmentGridAssignmentsHolder(NewETNewPTAssigments assignments)
    {
        this.assignments = assignments;
        this.originalAssignments = copy(assignments);
    }

    private NewETNewPTAssigments copy(NewETNewPTAssigments original)
    {
        if (original == null)
        {
            return null;
        }

        NewETNewPTAssigments copy = new NewETNewPTAssigments();
        if (original.getAssigments() != null)
        {
            List<NewPTNewAssigment> copyAssignments = new ArrayList<NewPTNewAssigment>();
            for (NewPTNewAssigment originalAssignment : original.getAssigments())
            {
                copyAssignments.add(copy(originalAssignment));
            }
            copy.setAssigments(copyAssignments);
        }
        return copy;
    }

    private NewPTNewAssigment copy(NewPTNewAssigment original)
    {
        if (original == null)
        {
            return null;
        }

        NewPTNewAssigment copy = new NewPTNewAssigment();
        copy.setAssignment(copy(original.getAssignment()));
        copy.setPropertyType(copy(original.getPropertyType()));
        return copy;
    }

    private NewETPTAssignment copy(NewETPTAssignment original)
    {
        if (original == null)
        {
            return null;
        }

        NewETPTAssignment copy = new NewETPTAssignment();
        copy.setDefaultValue(original.getDefaultValue());
        copy.setDynamic(original.isDynamic());
        copy.setManaged(original.isManaged());
        copy.setMandatory(original.isMandatory());
        copy.setOrdinal(original.getOrdinal());
        copy.setPropertyTypeCode(original.getPropertyTypeCode());
        copy.setScriptName(original.getScriptName());
        copy.setSection(original.getSection());
        copy.setShownInEditView(original.isShownInEditView());
        copy.setShowRawValue(original.getShowRawValue());
        return copy;
    }

    private PropertyType copy(PropertyType original)
    {
        if (original == null)
        {
            return null;
        }

        PropertyType copy = new PropertyType();
        copy.setCode(original.getCode());
        copy.setDataType(copy(original.getDataType()));
        copy.setDescription(original.getDescription());
        copy.setLabel(original.getLabel());
        copy.setManagedInternally(original.isManagedInternally());
        copy.setMaterialType(copy(original.getMaterialType()));
        copy.setSampleType(copy(original.getSampleType()));
        copy.setSchema(original.getSchema());
        copy.setSimpleCode(original.getSimpleCode());
        copy.setTransformation(original.getTransformation());
        copy.setVocabulary(copy(original.getVocabulary()));
        return copy;
    }

    private DataType copy(DataType original)
    {
        if (original == null)
        {
            return null;
        }

        DataType copy = new DataType();
        copy.setCode(original.getCode());
        return copy;
    }

    private MaterialType copy(MaterialType original)
    {
        if (original == null)
        {
            return null;
        }

        MaterialType copy = new MaterialType();
        copy.setCode(original.getCode());
        return copy;
    }

    private SampleType copy(SampleType original)
    {
        if (original == null)
        {
            return null;
        }

        SampleType copy = new SampleType();
        copy.setCode(original.getCode());
        return copy;
    }

    private Vocabulary copy(Vocabulary original)
    {
        if (original == null)
        {
            return null;
        }

        Vocabulary copy = new Vocabulary();
        copy.setCode(original.getCode());
        return copy;
    }

    private boolean equal(NewETNewPTAssigments o1, NewETNewPTAssigments o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        List<NewPTNewAssigment> o1Assignments = o1.getAssigments();
        List<NewPTNewAssigment> o2Assignments = o2.getAssigments();

        if (o1Assignments == null || o1Assignments.isEmpty() || o2Assignments == null || o2Assignments.isEmpty())
        {
            return (o1Assignments == null || o1Assignments.isEmpty()) && (o2Assignments == null || o2Assignments.isEmpty());
        }

        Iterator<NewPTNewAssigment> o1Iter = o1Assignments.iterator();
        Iterator<NewPTNewAssigment> o2Iter = o2Assignments.iterator();

        while (o1Iter.hasNext() && o2Iter.hasNext())
        {
            if (equal(o1Iter.next(), o2Iter.next()) == false)
            {
                return false;
            }
        }

        return o1Iter.hasNext() == false && o2Iter.hasNext() == false;
    }

    private boolean equal(NewPTNewAssigment o1, NewPTNewAssigment o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getAssignment(), o2.getAssignment()) && equal(o1.getPropertyType(), o2.getPropertyType());
    }

    private boolean equal(NewETPTAssignment o1, NewETPTAssignment o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getDefaultValue(), o2.getDefaultValue()) && equal(o1.isDynamic(), o2.isDynamic())
                && equal(o1.isManaged(), o2.isManaged()) && equal(o1.isMandatory(), o2.isMandatory())
                && equal(o1.getOrdinal(), o2.getOrdinal())
                && equal(o1.getPropertyTypeCode(), o2.getPropertyTypeCode()) && equal(o1.getScriptName(), o2.getScriptName())
                && equal(o1.getSection(), o2.getSection()) && equal(o1.isShownInEditView(), o2.isShownInEditView())
                && equal(o1.getShowRawValue(), o2.getShowRawValue());
    }

    private boolean equal(PropertyType o1, PropertyType o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getCode(), o2.getCode()) && equal(o1.getDataType(), o2.getDataType()) && equal(o1.getDescription(), o2.getDescription())
                && equal(o1.getLabel(), o2.getLabel())
                && equal(o1.isManagedInternally(), o2.isManagedInternally()) && equal(o1.getMaterialType(), o2.getMaterialType())
                && equal(o1.getSchema(), o2.getSchema())
                && equal(o1.getSimpleCode(), o2.getSimpleCode()) && equal(o1.getTransformation(), o2.getTransformation())
                && equal(o1.getVocabulary(), o2.getVocabulary());
    }

    private boolean equal(DataType o1, DataType o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getCode(), o2.getCode());
    }

    private boolean equal(MaterialType o1, MaterialType o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getCode(), o2.getCode());
    }

    private boolean equal(Vocabulary o1, Vocabulary o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getCode(), o2.getCode());
    }

    private boolean equal(Object o1, Object o2)
    {
        if (o1 == null)
        {
            return o2 == null;
        } else
        {
            return o1.equals(o2);
        }
    }

    public NewETNewPTAssigments getAssignments()
    {
        return assignments;
    }

    public boolean isDirty()
    {
        return equal(originalAssignments, assignments) == false;
    }

}
