import ch.systemsx.cisd.openbis.generic.shared.dto.identifier as identifier
import java.io as io
import ch.systemsx.cisd.openbis.generic.shared.basic.dto as dto

execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/all-hooks.py")

# Create the Experiment Identifier
identifier = identifier.ExperimentIdentifierFactory("/SPACE/PROJECT/EXP-CODE").createIdentifier()

# Register data set 1
registrationDetails = factory.createRegistrationDetails()
dataSetInformation = registrationDetails.getDataSetInformation()
dataSetInformation.setExperimentIdentifier(identifier)
registrationDetails.setDataSetType(dto.DataSetType("O1"));

None.non_existant_function()
