def validate(experiment, isNew):
    try:
        assertEntitiesCount(experiment.samples(), 7, 'samples')
        assertEntities(experiment.samplesOfType('CELL_PLATE'), ['CP1-A1','CP1-A2','CP1-B1','CP2-A1'], 'samples of type CELL_PLATE')
        assertEntities(experiment.samplesOfType('REINFECT_PLATE'), ['RP1-A2X','RP1-B1X','RP2-A1X'], 'samples of type REINFECT_PLATE')
    except Exception, ex:
        return ex.args[0]
