import datetime
from java.lang import String

def test(tr):
    
    # PROJECT
    
    project = tr.getProject("/CISD/NOE")
    
    mutableProject1 = tr.getProjectForUpdate("/CISD/NOE");
    addAttachment(mutableProject1, "project_1")

    mutableProject2 = tr.makeProjectMutable(project);
    addAttachment(mutableProject2, "project_2")
    
    projectByPermId = tr.getSearchService().getProjectByPermId("20120814110011738-104")
    mutableProject3 = tr.makeProjectMutable(projectByPermId);
    addAttachment(mutableProject3, "project_3")
    
    if(mutableProject2 is not mutableProject1):
        raise Exception('More than object for the same project in the DB was returned')
    if(mutableProject3 is not mutableProject1):
        raise Exception('More than object for the same project in the DB was returned')
    
    # EXPERIMENT
    
    experiment = tr.getExperiment("/CISD/DEFAULT/EXP-REUSE")
    
    mutableExperiment1 = tr.getExperimentForUpdate("/CISD/DEFAULT/EXP-REUSE")
    addAttachment(mutableExperiment1, "experiment_1")

    mutableExperiment2 = tr.makeExperimentMutable(experiment)
    addAttachment(mutableExperiment2, "experiment_2")
    
    experimentByPermId = tr.getSearchService().getExperimentByPermId("200811050940555-1032")
    mutableExperiment3 = tr.makeExperimentMutable(experimentByPermId)
    addAttachment(mutableExperiment3, "experiment_3")
        
    if(mutableExperiment2 is not mutableExperiment1):
        raise Exception('More than object for the same experiment in the DB was returned')
    
    if(mutableExperiment3 is not mutableExperiment1):
        raise Exception('More than object for the same experiment in the DB was returned')

    # SAMPLE

    sample = tr.getSample("/TEST-SPACE/TEST-PROJECT/FV-TEST")
        
    mutableSample1 = tr.getSampleForUpdate("/TEST-SPACE/TEST-PROJECT/FV-TEST")
    addAttachment(mutableSample1, "sample_1")

    mutableSample2 = tr.makeSampleMutable(sample)
    addAttachment(mutableSample2, "sample_2")

    sampleByPermId = tr.getSearchService().getSampleByPermId("201206191219327-1054")
    mutableSample3 = tr.makeSampleMutable(sampleByPermId)
    addAttachment(mutableSample3, "sample_3")

    if(mutableSample2 is not mutableSample1):
        raise Exception('More than object for the same sample in the DB was returned')

    if(mutableSample3 is not mutableSample1):
        raise Exception('More than object for the same sample in the DB was returned')
    
    # MATERIAL

    material = tr.getMaterial("BACTERIUM1", "BACTERIUM")
    
    mutableMaterial1 = tr.getMaterialForUpdate("BACTERIUM1", "BACTERIUM")
    mutableMaterial1.setPropertyValue("DESCRIPTION", "mutable material description 1");

    mutableMaterial2 = tr.makeMaterialMutable(material)
    mutableMaterial2.setPropertyValue("DESCRIPTION", "mutable material description 2");
    
    if(mutableMaterial2 is not mutableMaterial1):
        raise Exception('More than object for the same material in the DB was returned')
    
    # DATA SET
    
    dataSet = tr.getDataSet("20081105092159188-3")
    
    mutableDataSet1 = tr.getDataSetForUpdate("20081105092159188-3")
    mutableDataSet1.setPropertyValue("COMMENT", "mutable data set comment 1")
    
    mutableDataSet2 = tr.makeDataSetMutable(dataSet)
    mutableDataSet2.setPropertyValue("COMMENT", "mutable data set comment 2")
    
    if(mutableDataSet2 is not mutableDataSet1):
        raise Exception('More than object for the same data set in the DB was returned')

def addAttachment(holder, name):
    timestamp = datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')
    holder.addAttachment(name, name + "_title_" + timestamp, name + "_description_" + timestamp, String(name + "_content_" + timestamp).getBytes("UTF-8"))
