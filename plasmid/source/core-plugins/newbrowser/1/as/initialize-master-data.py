#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()

# The default ELN Experiment
elnExperimentTypeE = tr.getOrCreateNewExperimentType("SYSTEM_EXPERIMENT")
elnExperimentTypeE.setDescription("Lab Experiment")
elnExperimentTypeS = tr.getOrCreateNewSampleType("SYSTEM_EXPERIMENT")
elnExperimentTypeS.setDescription("Lab Experiment")
elnExperimentTypeS.setListable(True)
elnExperimentTypeS.setSubcodeUnique(False)
elnExperimentTypeS.setAutoGeneratedCode(False)
elnExperimentTypeS.setGeneratedCodePrefix('C')

# The default experiment used by the UI, assigned automatically to new samples
folderType = tr.getOrCreateNewExperimentType("ELN_FOLDER")
folderType.setDescription("Folder")

# Preview Image
elnDataSetPreviewType = tr.getOrCreateNewDataSetType("ELN_PREVIEW")
elnDataSetPreviewType.setDescription("ELN Preview");
elnDataSetPreviewType.setDataSetKind("PHYSICAL");