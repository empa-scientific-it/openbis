package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

public class NewETNewPTAssigments implements Serializable
{
    private EntityType entity;

    private List<NewPTNewAssigment> assigments;

    public EntityType getEntity()
    {
        return entity;
    }

    public void setEntity(EntityType entity)
    {
        this.entity = entity;
    }

    public List<NewPTNewAssigment> getAssigments()
    {
        return assigments;
    }

    public void setAssigments(List<NewPTNewAssigment> assigments)
    {
        this.assigments = assigments;
    }

    public boolean isNewPropertyType(String code)
    {
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            if (entity.getAssignedPropertyTypes().get(i).getPropertyType().getCode().equals(code))
            {
                return false == assigments.get(i).isExistingPropertyType();
            }
        }
        return false;
    }

    public void updateCodeFromNewPropertyType(String currentCode, String newCode)
    {
        if (isNewPropertyType(currentCode))
        {
            for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
            {
                if (entity.getAssignedPropertyTypes().get(i).getPropertyType().getCode().equals(currentCode))
                {
                    entity.getAssignedPropertyTypes().get(i).getPropertyType().setCode(newCode);
                    assigments.get(i).getPropertyType().setCode(newCode);
                    assigments.get(i).getAssignment().setPropertyTypeCode(newCode);
                }
            }
        } else
        {
            throw new RuntimeException("A code from an existing property type can't be modified.");
        }
    }

    public void updateOrdinalToDBOrder()
    {
        // Update Ordinal - Internal/External List
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            entity.getAssignedPropertyTypes().get(i).setOrdinal((long) i);
            assigments.get(i).getAssignment().setOrdinal((long) i);
        }
    }

    public void updateOrdinalToGridOrder()
    {
        // Update Ordinal - Internal/External List
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            entity.getAssignedPropertyTypes().get(i).setOrdinal((long) i + 1);
            assigments.get(i).getAssignment().setOrdinal((long) i + 1);
        }
    }

    public void refreshOrderAdd(NewPTNewAssigment newAssigment) throws Exception
    {
        if (isAssigmentFound(newAssigment))
        {
            throw new Exception("A property can't be assigned twice.");
        }

        // Update Ordinal - Internal/External List
        updateOrdinalToDBOrder();
        int insertPos = newAssigment.getAssignment().getOrdinal().intValue();

        //
        // Update Lists - This Reorder the items due to an insert
        //

        // Internal List
        EntityTypePropertyType<?> newEtpt = getEntityTypePropertyType(entity, newAssigment);
        switch (entity.getEntityKind())
        {
            case SAMPLE:
                SampleType sampleType = (SampleType) entity;
                sampleType.getAssignedPropertyTypes().add(insertPos, (SampleTypePropertyType) newEtpt);
                break;
            case EXPERIMENT:
                ExperimentType experimentType = (ExperimentType) entity;
                experimentType.getAssignedPropertyTypes().add(insertPos, (ExperimentTypePropertyType) newEtpt);
                break;
            case DATA_SET:
                DataSetType datasetType = (DataSetType) entity;
                datasetType.getAssignedPropertyTypes().add(insertPos, (DataSetTypePropertyType) newEtpt);
                break;
            case MATERIAL:
                MaterialType materialType = (MaterialType) entity;
                materialType.getAssignedPropertyTypes().add(insertPos, (MaterialTypePropertyType) newEtpt);
                break;
        }

        // External List
        assigments.add(insertPos, newAssigment);

        // Update Ordinal - Internal/External List
        updateOrdinalToGridOrder();
    }

    public void refreshOrderDelete(String code)
    {
        // Update Ordinal - Internal/External List
        updateOrdinalToDBOrder();

        //
        // Delete Code - Internal/External List
        //
        for (int i = 0; i < entity.getAssignedPropertyTypes().size(); i++)
        {
            if (entity.getAssignedPropertyTypes().get(i).getPropertyType().getCode().equals(code))
            {
                entity.getAssignedPropertyTypes().remove(i);
                assigments.remove(i);
                break;
            }
        }

        // Update Ordinal - Internal/External List
        updateOrdinalToGridOrder();
    }

    public void refreshOrderUpdate(NewETPTAssignment toRegister) throws Exception
    {
        updateOrdinalToDBOrder();
        NewPTNewAssigment current = null;
        for (NewPTNewAssigment assigment : assigments)
        {
            if (assigment.getAssignment().getPropertyTypeCode().equals(toRegister.getPropertyTypeCode()))
            {
                current = assigment;
                break;
            }
        }

        // If position to insert is after the current position, the index need to be changed to -1.
        long currentOrdinal = current.getAssignment().getOrdinal();
        long newOrdinal = toRegister.getOrdinal();
        if (newOrdinal > currentOrdinal)
        {
            toRegister.setOrdinal(toRegister.getOrdinal() - 1);
        }
        updateOrdinalToGridOrder();

        refreshOrderDelete(toRegister.getPropertyTypeCode());
        current.setAssignment(toRegister);
        refreshOrderAdd(current);
    }

    public boolean isAssigmentFound(NewPTNewAssigment assigment)
    {
        for (NewPTNewAssigment assigmentFound : assigments)
        {
            if (assigmentFound.getPropertyType().getCode().equals(assigment.getPropertyType().getCode()))
            {
                return true;
            }
        }
        return false;
    }

    public static EntityTypePropertyType<?> getEntityTypePropertyType(EntityType entityType, NewPTNewAssigment propertyTypeAsg)
    {
        EntityTypePropertyType<?> etpt = null;
        switch (entityType.getEntityKind())
        {
            case SAMPLE:
                etpt = new SampleTypePropertyType();
                ((SampleTypePropertyType) etpt).setEntityType((SampleType) entityType);
                break;
            case EXPERIMENT:
                etpt = new ExperimentTypePropertyType();
                ((ExperimentTypePropertyType) etpt).setEntityType((ExperimentType) entityType);
                break;
            case DATA_SET:
                etpt = new DataSetTypePropertyType();
                ((DataSetTypePropertyType) etpt).setEntityType((DataSetType) entityType);
                break;
            case MATERIAL:
                etpt = new MaterialTypePropertyType();
                ((MaterialTypePropertyType) etpt).setEntityType((MaterialType) entityType);
                break;
        }

        etpt.setPropertyType(propertyTypeAsg.getPropertyType());
        etpt.setOrdinal(propertyTypeAsg.getAssignment().getOrdinal());
        etpt.setSection(propertyTypeAsg.getAssignment().getSection());
        etpt.setMandatory(propertyTypeAsg.getAssignment().isMandatory());
        etpt.setDynamic(propertyTypeAsg.getAssignment().isDynamic());
        etpt.setManaged(propertyTypeAsg.getAssignment().isManaged());
        etpt.setShownInEditView(propertyTypeAsg.getAssignment().isShownInEditView());
        etpt.setShowRawValue(propertyTypeAsg.getAssignment().getShowRawValue());
        etpt.setModificationDate(propertyTypeAsg.getAssignment().getModificationDate());

        if (propertyTypeAsg.getAssignment().getScriptName() != null)
        {
            Script scriptNew = new Script();
            scriptNew.setName(propertyTypeAsg.getAssignment().getScriptName());
            etpt.setScript(scriptNew);
        }

        return etpt;
    }

}
