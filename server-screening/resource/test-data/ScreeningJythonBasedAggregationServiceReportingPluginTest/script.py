from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import ExperimentIdentifier

PLATE = "Plate"

def aggregate(parameters, tableBuilder):
    experimentIdentifier = parameters.get('experiment-identifier')
    plates = screeningFacade.listPlates(ExperimentIdentifier.createFromAugmentedCode(experimentIdentifier))
    tableBuilder.addHeader(PLATE)
    for plate in plates:
        row = tableBuilder.addRow()
        row.setCell(PLATE, plate.plateCode)
