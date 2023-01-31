def process(context, parameters):
    print(">>> import-test <<<");
    
    sessionToken = context.getSessionToken()
    operation = parameters.get("operation");
    uploadKey = parameters.get("uploadKey");
    typeCode = parameters.get("typeCode");
    asynchrounous = parameters.get("async");
    userEmail = parameters.get("userEmail");
    defaultSpaceIdentifier = parameters.get("defaultSpaceIdentifier");
    spaceIdentifierOverride = parameters.get("spaceIdentifierOverride");
    experimentIdentifierOverride = parameters.get("experimentIdentifierOverride");
    updateExisting = parameters.get("updateExisting");
    ignoreUnregistered = parameters.get("ignoreUnregistered");
    customImportCode = parameters.get("customImportCode");
    
    if operation == "createExperiments":
        return context.getImportService().createExperiments(sessionToken, uploadKey, typeCode, asynchrounous, userEmail);
    elif operation == "updateExperiments":
        return context.getImportService().updateExperiments(sessionToken, uploadKey, typeCode, asynchrounous, userEmail);
    elif operation == "createSamples":
        return context.getImportService().createSamples(sessionToken, uploadKey, typeCode, defaultSpaceIdentifier, spaceIdentifierOverride, experimentIdentifierOverride, updateExisting, asynchrounous, userEmail);
    elif operation == "updateSamples":
        return context.getImportService().updateSamples(sessionToken, uploadKey, typeCode, defaultSpaceIdentifier, spaceIdentifierOverride, experimentIdentifierOverride, asynchrounous, userEmail);
    elif operation == "updateDataSets":
        return context.getImportService().updateDataSets(sessionToken, uploadKey, typeCode, asynchrounous, userEmail);
    elif operation == "createMaterials":
        return context.getImportService().createMaterials(sessionToken, uploadKey, typeCode, updateExisting, asynchrounous, userEmail);
    elif operation == "updateMaterials":
        return context.getImportService().updateMaterials(sessionToken, uploadKey, typeCode, ignoreUnregistered, asynchrounous, userEmail);
    elif operation == "generalImport":
        return context.getImportService().generalImport(sessionToken, uploadKey, defaultSpaceIdentifier, updateExisting, asynchrounous, userEmail);
    elif operation == "customImport":
        return context.getImportService().customImport(sessionToken, uploadKey, customImportCode, asynchrounous, userEmail);

    return None;
