import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExportExtendedService as XLSExportExtendedService
import ch.systemsx.cisd.common.exceptions.UserFailureException as UserFailureException
import base64
import json
import re
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator as AttributeValidator
import ch.ethz.sis.openbis.generic.server.xls.importer.helper.SampleImportHelper as SampleImportHelper
import ch.systemsx.cisd.common.logging.LogCategory as LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory as LogFactory;
from java.nio.file import Files
from java.io import File

isOpenBIS2020 = True;
enableNewSearchEngine = isOpenBIS2020;

OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION, LogFactory);

##
## Grid related functions
## These functions should be the same as in javascript, currently found on Util.js
##
alphabet = [None,'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
def getLetterForNumber(number): # TODO Generate big numbers
    return alphabet[number];

def getNumberFromLetter(letter): # TODO Generate big numbers
    i = None;
    for alphabetLetter in alphabet:
        if i == None:
            i = 0;
        else:
            i = i + 1;
        if letter == alphabetLetter:
            return i;
    return None;

def process(context, parameters):
    method = parameters.get("method");
    result = None;

    if method == "getServiceProperty":
        result = getServiceProperty(context, parameters);
    elif method == "getNextSequenceForType":
        result = getNextSequenceForType(context, parameters);
    elif method == "getNextExperimentCode":
        result = getNextExperimentCode(context, parameters);
    elif method == "doSpacesBelongToDisabledUsers":
        result = doSpacesBelongToDisabledUsers(context, parameters);
    elif method == "trashStorageSamplesWithoutParents":
        sessionToken = None
        try:
            sessionToken = context.applicationService.loginAsSystem();
            result = trashStorageSamplesWithoutParents(context, parameters, sessionToken);
        finally:
            context.applicationService.logout(sessionToken);
    elif method == "isValidStoragePositionToInsertUpdate":
        sessionToken = None
        try:
            sessionToken = context.applicationService.loginAsSystem();
            result = isValidStoragePositionToInsertUpdate(context, parameters, sessionToken);
        finally:
            context.applicationService.logout(sessionToken);
    elif method == "setCustomWidgetSettings":
        sessionToken = None
        try:
            sessionToken = context.applicationService.loginAsSystem();
            result = setCustomWidgetSettings(context, parameters, sessionToken);
        finally:
            context.applicationService.logout(sessionToken);
    elif method == "getUserManagementMaintenanceTaskConfig":
        result = getUserManagementMaintenanceTaskConfig(context, parameters)
    elif method == "saveUserManagementMaintenanceTaskConfig":
        result = saveUserManagementMaintenanceTaskConfig(context, parameters)
    elif method == "executeUserManagementMaintenanceTask":
        result = executeUserManagementMaintenanceTask(context, parameters)
    elif method == "getUserManagementMaintenanceTaskReport":
        result = getUserManagementMaintenanceTaskReport(context, parameters)
    elif method == "removeUserManagementMaintenanceTaskReport":
        result = removeUserManagementMaintenanceTaskReport(context, parameters)
    elif method == "importSamples":
        result = importSamples(context, parameters)
    elif method == "getSamplesImportTemplate":
        result = getSamplesImportTemplate(context, parameters)
    elif method == "createSpace":
        result = createSpace(context, parameters)
    elif method == "deleteSpace":
        result = deleteSpace(context, parameters)
    elif method == "getCustomImportDefinitions":
        result = getCustomImportDefinitions(context, parameters)
    elif method == "getExport":
        result = getExport(context, parameters)
    return result

def getExport(context, parameters):
    sessionToken = context.getSessionToken()
    exportModel = parameters.get("export-model")
    return XLSExportExtendedService.export(sessionToken, exportModel)

def getCustomImportDefinitions(context, parameters):
    from ch.systemsx.cisd.common.spring import ExposablePropertyPlaceholderConfigurer
    from ch.systemsx.cisd.openbis.generic.shared.util import ServerUtils

    properties = CommonServiceProvider.tryToGetBean(ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME) \
                    .getResolvedProps()
    descriptions = ServerUtils.getCustomImportDescriptions(properties)
    for description in descriptions:
        description.getCode()
        description.getProperties()
    return descriptions

def deleteSpace(context, parameters):
    code = parameters.get("code")
    reason = parameters.get("reason")
    _deleteSpace(context, parameters, code, reason)
    settingsSamples = _getAllSettingsSamples(context)
    return _removeInventorySpace(context, settingsSamples, code)

def _deleteSpace(context, parameters, code, reason):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete import SpaceDeletionOptions

    options = SpaceDeletionOptions()
    options.setReason(reason)
    context.getApplicationService().deleteSpaces(context.getSessionToken(), [SpacePermId(code)], options)

def _getAllSettingsSamples(context):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions

    criteria = SampleSearchCriteria()
    criteria.withType().withCode().thatEquals("GENERAL_ELN_SETTINGS")
    fetchOptions = SampleFetchOptions()
    fetchOptions.withProperties()
    return context.getApplicationService().searchSamples(context.getSessionToken(), criteria, fetchOptions).getObjects()

def _removeInventorySpace(context, settingsSamples, code):
    settingsUpdated = False
    for settingsSample in settingsSamples:
        settings = settingsSample.getProperty("$ELN_SETTINGS")
        if settings is not None:
            removed = False
            settings = json.loads(settings)
            if "inventorySpaces" in settings:
                removed = _removeFromList(settings["inventorySpaces"], code)
            if "inventorySpacesReadOnly" in settings:
                removed = removed or _removeFromList(settings["inventorySpacesReadOnly"], code)
            if removed:
                settingsUpdated = True
                _updateSettings(context, settingsSample, settings)
    return settingsUpdated

def _removeFromList(list, element):
    if list and element in list:
        list.remove(element)
        return True
    return False
    
def createSpace(context, parameters):
    group = parameters.get("group")
    code = parameters.get("postfix")
    if group is not None and len(group) > 0:
        code = "%s_%s" % (group, code)
    spaceIds = _createSpace(context, parameters, code)

    reloadNeeded = False
    if parameters.get("isInventory"):
        settingsSample = _getSettingsSample(context, parameters, group)
        settings = settingsSample.getProperty("$ELN_SETTINGS")
        if settings is None:
            raise UserFailureException("Settings %s not yet defined. Please, edit them first." 
                                       % settingsSample.getIdentifier())
        settings = json.loads(settings)
        isReadOnly = parameters.get("isReadOnly")
        spaces = settings["inventorySpacesReadOnly" if isReadOnly else "inventorySpaces"]
        _addAuthorizations(context, parameters, group, code, isReadOnly, spaces)
        if not code in spaces:
            spaces.append(code)
            _updateSettings(context, settingsSample, settings)
            reloadNeeded = True
    return {"spaceIds" : spaceIds, "reloadNeeded" : reloadNeeded}

def _createSpace(context, parameters, code):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation

    spaceCreation = SpaceCreation()
    description = parameters.get("description")
    spaceCreation.setCode(code);
    if description is not None:
        spaceCreation.setDescription(description)
    return context.getApplicationService().createSpaces(context.getSessionToken(), [spaceCreation])

def _getSettingsSample(context, parameters, group):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions

    settingsIdentifier = "/ELN_SETTINGS/GENERAL_ELN_SETTINGS"
    if group:
        settingsIdentifier = "/%s_ELN_SETTINGS/%s_ELN_SETTINGS" % (group, group)
    settingsIdentifier = SampleIdentifier(settingsIdentifier)
    fetchOptions = SampleFetchOptions()
    fetchOptions.withProperties()
    sessionToken = context.getSessionToken()
    api = context.getApplicationService()
    settingsSample = api.getSamples(sessionToken, [settingsIdentifier], fetchOptions).get(settingsIdentifier)
    if settingsSample is None:
        raise UserFailureException("No settings sample for %s" % settingsIdentifier)
    return settingsSample

def _updateSettings(context, settingsSample, settings):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update import SampleUpdate

    sampleUpdate = SampleUpdate()
    sampleUpdate.setSampleId(settingsSample.getPermId())
    sampleUpdate.setProperty("$ELN_SETTINGS", json.dumps(settings))
    context.getApplicationService().updateSamples(context.getSessionToken(), [sampleUpdate])

def _addAuthorizations(context, parameters, group, code, isReadOnly, spaces):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create import RoleAssignmentCreation
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id import AuthorizationGroupPermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id import PersonPermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import Role
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId

    creations = []
    if group:
        creation = RoleAssignmentCreation()
        creation.setAuthorizationGroupId(AuthorizationGroupPermId(group))
        creation.setSpaceId(SpacePermId(code))
        creation.setRole(Role.OBSERVER if isReadOnly else Role.USER)
        creations.append(creation)
        creationAdmin = RoleAssignmentCreation()
        creationAdmin.setAuthorizationGroupId(AuthorizationGroupPermId("%s_ADMIN" % group))
        creationAdmin.setSpaceId(SpacePermId(code))
        creationAdmin.setRole(Role.ADMIN)
        creations.append(creationAdmin)
    elif spaces:
        users = _getUsers(context, spaces)
        for user in users:
            creation = RoleAssignmentCreation()
            creation.setUserId(PersonPermId(user))
            creation.setSpaceId(SpacePermId(code))
            creation.setRole(Role.OBSERVER if isReadOnly else Role.USER)
            creations.append(creation)
    if creations:
        context.getApplicationService().createRoleAssignments(context.getSessionToken(), creations)

def _getUsers(context, spaces):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import RoleAssignment
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions import RoleAssignmentFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search import RoleAssignmentSearchCriteria

    searchCriteria = RoleAssignmentSearchCriteria()
    searchCriteria.withSpace().withCodes().thatIn(spaces)
    fetchOptions = RoleAssignmentFetchOptions()
    fetchOptions.withUser()
    assignments = context.getApplicationService().searchRoleAssignments(
        context.getSessionToken(), searchCriteria, fetchOptions).getObjects()
    users = set()
    for assignment in assignments:
        user = assignment.getUser()
        if user:
            users.add(user.getUserId());
    return users

def getSamplesImportTemplate(context, parameters):
    from java.io import ByteArrayOutputStream
    from org.apache.poi.xssf.usermodel import XSSFWorkbook
    from org.apache.poi.ss.usermodel import IndexedColors
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin import PluginType

    allowedSampleTypes = [EntityTypePermId(code) for code in parameters.get("allowedSampleTypes")]
    templateType = parameters.get("templateType")
    importMode = parameters.get("importMode")
    sessionToken = context.getSessionToken()
    api = context.getApplicationService()
    fetchOptions = SampleTypeFetchOptions()
    fetchOptions.withPropertyAssignments().withPropertyType()
    fetchOptions.withPropertyAssignments().withPlugin()
    sampleTypes = api.getSampleTypes(sessionToken, allowedSampleTypes, fetchOptions)
    workbook = XSSFWorkbook()
    kind_style = _create_style(workbook, IndexedColors.LIGHT_ORANGE)
    type_style = _create_style(workbook, bold=True)
    header_style = _create_style(workbook, IndexedColors.GREY_25_PERCENT, True)
    for sampleTypeId in allowedSampleTypes:
        sampleTypePermId = sampleTypeId.getPermId()
        sheet = workbook.createSheet(sampleTypePermId)
        row_index = 0
        max_number_of_columns = 0
        row = sheet.createRow(row_index)
        _create_cell(row, 0, kind_style, "SAMPLE")
        row = sheet.createRow(row_index + 1)
        _create_cell(row, 0, None, "Sample type")
        row = sheet.createRow(row_index + 2)
        _create_cell(row, 0, type_style, sampleTypePermId)
        row = sheet.createRow(row_index + 3)
        cell_index = _create_cell(row, 0, header_style, "$")
        if importMode == "UPDATE":
            cell_index = _create_cell(row, cell_index, header_style, "Identifier")
        if templateType == "GENERAL":
            cell_index = _create_cell(row, cell_index, header_style, "Code")
            cell_index = _create_cell(row, cell_index, header_style, "Experiment")
            cell_index = _create_cell(row, cell_index, header_style, "Project")
            cell_index = _create_cell(row, cell_index, header_style, "Space")
        cell_index = _create_cell(row, cell_index, header_style, "Parents")
        cell_index = _create_cell(row, cell_index, header_style, "Children")
        attributeValidator = AttributeValidator(SampleImportHelper.Attribute)
        for propertyAssignment in sampleTypes.get(sampleTypeId).getPropertyAssignments():
            plugin = propertyAssignment.getPlugin()
            if plugin is None or plugin.getPluginType() != PluginType.DYNAMIC_PROPERTY:
                if not attributeValidator.isHeader(propertyAssignment.getPropertyType().getLabel()):
                    cell_index = _create_cell(row, cell_index, header_style, propertyAssignment.getPropertyType().getLabel())
                else:
                    cell_index = _create_cell(row, cell_index, header_style, propertyAssignment.getPropertyType().getCode())

        max_number_of_columns = max(max_number_of_columns, cell_index)
        row_index += 6
        for i in range(max_number_of_columns):
            sheet.autoSizeColumn(i)
    baos = ByteArrayOutputStream()
    workbook.write(baos)
    return baos.toByteArray()

def _create_style(workbook, color=None, bold=None):
    from org.apache.poi.ss.usermodel import FillPatternType

    style = workbook.createCellStyle()
    if color is not None:
        style.setFillForegroundColor(color.getIndex())
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    if bold:
        font = workbook.createFont()
        font.setBold(True)
        style.setFont(font)
    return style

def _create_cell(row, cell_index, style, value):
    cell = row.createCell(cell_index)
    cell.setCellStyle(style)
    cell.setCellValue(value)
    return cell_index + 1

def importSamples(context, parameters):
    sessionKey = parameters.get("sessionKey")
    allowedSampleTypes = parameters.get("allowedSampleTypes")
    experimentsByType = parameters.get("experimentsByType", {})
    spacesByType = parameters.get("spacesByType", {})
    mode = parameters.get("mode")
    barcodeValidationInfo = json.loads(parameters.get("barcodeValidationInfo"))
    sessionManager = CommonServiceProvider.getApplicationContext().getBean("session-manager")
    sessionWorkspaceProvider = CommonServiceProvider.getApplicationContext().getBean("session-workspace-provider")
    workspaceFolder = sessionWorkspaceProvider.getSessionWorkspace(context.getSessionToken())
    uploadedFile = File(workspaceFolder, sessionKey)
    bytes = Files.readAllBytes(uploadedFile.toPath())
    results = importData(context, bytes, sessionKey, experimentsByType, spacesByType, mode, False)
    return results

def validateExperimentOrSpaceDefined(row_number, properties, mode, experiment, space):
    if experiment is None and space is None and not mode.startswith("UPDATE"):
        exp = properties.get("experiment")
        if exp is None:
            raise UserFailureException("Error in row %s: Empty column 'Experiment'" % row_number);

def validateBarcode(row_number, properties, barcodeValidationInfo):
    barcode = properties.get("custom barcode")
    if barcode is None:
        barcode = properties.get("$BARCODE")
    if barcode is not None:
        minBarcodeLength = barcodeValidationInfo['minBarcodeLength']
        if len(barcode) < minBarcodeLength:
            raise UserFailureException("Error in row %s: custom barcode %s is too short. "
                                       "Minimum barcode length has to be %s."
                                       % (row_number, barcode, minBarcodeLength))
        regex = barcodeValidationInfo['barcodePattern']
        pattern = re.compile(regex)
        if pattern.match(barcode) is None:
            raise UserFailureException("Error in row %s: custom barcode %s does not match "
                                       "the regular expression '%s'." % (row_number, barcode, pattern.pattern))

def importData(context, bytes, file_name, experimentsByType, spacesByType, mode, definitionsOnly):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier

    sessionToken = context.getSessionToken()
    api = context.getApplicationService()
    props = CustomASServiceExecutionOptions().withParameter('xls', [bytes])
    props.withParameter('method', 'import')
    props.withParameter('zip', False)
    props.withParameter('xls_name', 'DEFAULT')
    props.withParameter('update_mode', mode)
    props.withParameter('disallow_creations', mode == 'UPDATE_IF_EXISTS')
    props.withParameter('render_result', False)
    props.withParameter('ignore_versioning', True)
    if definitionsOnly:
        props.withParameter('definitions_only', True)
    if experimentsByType is not None:
        props.withParameter('experiments_by_type', experimentsByType)
    if spacesByType is not None:
        props.withParameter('spaces_by_type', spacesByType)
    return api.executeCustomASService(sessionToken, CustomASServiceCode("xls-import"), props)

def getUserManagementMaintenanceTaskConfig(context, parameters):
    from ch.systemsx.cisd.common.filesystem import FileUtilities

    configfile = _getUserManagementMaintenanceTaskConfigFile(context)
    if configfile is not None:
        return FileUtilities.loadToString(configfile)
    return None

def saveUserManagementMaintenanceTaskConfig(context, parameters):
    from ch.systemsx.cisd.common.filesystem import FileUtilities

    configfile = _getUserManagementMaintenanceTaskConfigFile(context)
    config = parameters.get("config")
    if configfile is not None and config is not None:
        FileUtilities.writeToFile(configfile, config)

def executeUserManagementMaintenanceTask(context, parameters):
    task = _getUserManagementMaintenanceTask(context)
    return task.executeAsync()

def getUserManagementMaintenanceTaskReport(context, parameters):
    task = _getUserManagementMaintenanceTask(context)
    id = int(parameters.get("id"))
    report = task.getReportById(id)
    return (report.getLog(), report.getAuditLog(), report.getErrorReport())

def removeUserManagementMaintenanceTaskReport(context, parameters):
    task = _getUserManagementMaintenanceTask(context)
    id = int(parameters.get("id"))
    task.removeReport(id)

def _getUserManagementMaintenanceTaskConfigFile(context):
    task = _getUserManagementMaintenanceTask(context)
    return task.getConfigurationFile() if task is not None else None

def _getUserManagementMaintenanceTask(context):
    from ch.systemsx.cisd.openbis.generic.server import MaintenanceTaskStarter

    if _isInstanceAdmin(context):
        for plugin in CommonServiceProvider.getApplicationContext().getBean(MaintenanceTaskStarter).getPlugins():
            parameters = plugin.getParameters()
            if parameters.getClassName() == "ch.systemsx.cisd.openbis.generic.server.task.UserManagementMaintenanceTask":
                return plugin.getTask()
    return None

def _isInstanceAdmin(context):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id import Me
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions import PersonFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import Role
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import RoleLevel

    me = Me()
    fetchOptions = PersonFetchOptions()
    fetchOptions.withRoleAssignments()
    person = context.applicationService.getPersons(context.sessionToken, [me], fetchOptions)[me]
    for roleAssignment in person.roleAssignments:
        if roleAssignment.roleLevel == RoleLevel.INSTANCE and roleAssignment.role == Role.ADMIN:
            return True
    return False

def setCustomWidgetSettings(context, parameters, sessionToken):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update import PropertyTypeUpdate
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id import PropertyTypePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue import ListUpdateActionAdd
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions

    widgetSettingsById = {PropertyTypePermId(ws["Property Type"]):ws for ws in parameters.get("widgetSettings")}
    searchCriteria = PropertyTypeSearchCriteria()
    fetchOptions = PropertyTypeFetchOptions()
    ptus = [];
    propertyTypes = context.applicationService.searchPropertyTypes(sessionToken, searchCriteria, fetchOptions)
    for propertyType in propertyTypes.getObjects():
        id = propertyType.getPermId()
        ptu = PropertyTypeUpdate();
        ptu.setTypeId(id);
        metaData = ptu.getMetaData()
        if id in widgetSettingsById:
            metaData.add([{"custom_widget" : widgetSettingsById[id]["Widget"] }])
        else:
            metaData.remove(["custom_widget"])
        ptus.append(ptu);

    context.applicationService.updatePropertyTypes(sessionToken, ptus);
    return True

def isValidStoragePositionToInsertUpdate(context, parameters, sessionToken):
    # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - START -" + str(parameters));
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
    from ch.systemsx.cisd.common.exceptions import UserFailureException

    samplePermId = parameters.get("samplePermId");
    sampleProperties = parameters.get("sampleProperties");
    storageCode = sampleProperties.get("$STORAGE_POSITION.STORAGE_CODE");
    storageRackRow = sampleProperties.get("$STORAGE_POSITION.STORAGE_RACK_ROW");
    storageRackColumn = sampleProperties.get("$STORAGE_POSITION.STORAGE_RACK_COLUMN");
    storageBoxName = sampleProperties.get("$STORAGE_POSITION.STORAGE_BOX_NAME");
    storageBoxSize = sampleProperties.get("$STORAGE_POSITION.STORAGE_BOX_SIZE");
    storageBoxPosition = sampleProperties.get("$STORAGE_POSITION.STORAGE_BOX_POSITION");

    storageUser = sampleProperties.get("$STORAGE_POSITION.STORAGE_USER");

    # 1. Obtain Storage to retrieve Storage Validation Level
    if storageCode is None:
        raise UserFailureException("Storage code missing");

    searchCriteria = SampleSearchCriteria();
    searchCriteria.withCode().thatEquals(storageCode);
    searchCriteria.withType().withCode().thatEquals("STORAGE");

    fetchOptions = SampleFetchOptions();
    fetchOptions.withProperties();

    storage = None;
    storageValidationLevel = None;
    sampleSearchResults = context.applicationService.searchSamples(sessionToken, searchCriteria, fetchOptions).getObjects();
    if sampleSearchResults.size() == 1:
        storage = sampleSearchResults.get(0);
        storageValidationLevel = storage.getProperty("$STORAGE.STORAGE_VALIDATION_LEVEL");
    else:
        raise UserFailureException("Found: " + str(sampleSearchResults.size()) + " storages for storage code: " + storageCode);

    # 2. Check that the state of the sample is valid for the Storage Validation Level
    # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 2");
    if storageRackRow is None or storageRackColumn is None:
        raise UserFailureException("Storage rack row or column missing");
    elif storageBoxName is None and (storageValidationLevel == "BOX" or storageValidationLevel == "BOX_POSITION"):
        raise UserFailureException("Storage box name missing");
    elif storageBoxSize is None and (storageValidationLevel == "BOX" or storageValidationLevel == "BOX_POSITION"):
        raise UserFailureException("Storage box size missing");
    elif storageBoxPosition is None and storageValidationLevel == "BOX_POSITION":
        raise UserFailureException("Storage box position missing");
    else:
        pass

    # 3. IF $STORAGE.STORAGE_VALIDATION_LEVEL >= RACK
    # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 3");
    # 3.1 Check the rack exists, it should always be specified as an integer, failing the conversion is a valid error
    storageNumOfRowsAsInt = int(storage.getProperty("$STORAGE.ROW_NUM"));
    storageNumOfColAsInt = int(storage.getProperty("$STORAGE.COLUMN_NUM"));
    storageRackRowAsInt = int(storageRackRow)
    storageRackColAsInt = int(storageRackColumn)
    if storageRackRowAsInt > storageNumOfRowsAsInt or storageRackColAsInt > storageNumOfColAsInt:
        raise UserFailureException("Out of range row or column for the rack");

    # 4. IF $STORAGE.STORAGE_VALIDATION_LEVEL >= BOX
    # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 4");
    if storageBoxName is not None:
        # 4.1 Check that a box with the same name only exist on the given storage and rack position
        # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 4.1");
        searchCriteriaOtherBox = SampleSearchCriteria();
        searchCriteriaOtherBox.withType().withCode().thatEquals("STORAGE_POSITION");
        searchCriteriaOtherBox.withStringProperty("$STORAGE_POSITION.STORAGE_BOX_NAME").thatEquals(storageBoxName);
        searchCriteriaOtherBoxOptions = SampleFetchOptions();
        searchCriteriaOtherBoxOptions.withProperties();

        sampleSearchResults = context.applicationService.searchSamples(sessionToken, searchCriteriaOtherBox, searchCriteriaOtherBoxOptions).getObjects();
        # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 4.1 - LEN: " + str(len(sampleSearchResults)));
        for result in sampleSearchResults:
            if (result.getProperty("$STORAGE_POSITION.STORAGE_CODE") != storageCode) or (result.getProperty("$STORAGE_POSITION.STORAGE_RACK_ROW") != storageRackRow) or (result.getProperty("$STORAGE_POSITION.STORAGE_RACK_COLUMN") != storageRackColumn):
                raise UserFailureException("You entered the name of an already existing box in a different place - Box Name: " + str(storageBoxName) + " Given -> Storage Code: " + str(storageCode) + " Rack Row: " + str(storageRackRow) + " Rack Column: " + str(storageRackColumn) + " - Found -> Storage Code: " + result.getProperty("$STORAGE_POSITION.STORAGE_CODE") + " Rack Row: " + result.getProperty("$STORAGE_POSITION.STORAGE_RACK_ROW") + " Rack Column: " + result.getProperty("$STORAGE_POSITION.STORAGE_RACK_COLUMN"));

    if storageValidationLevel == "BOX" or storageValidationLevel == "BOX_POSITION":
        # 4.2 The number of total different box names on the rack including the given one should be below $STORAGE.BOX_NUM
        # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 4.2");
        searchCriteriaStorageRack = SampleSearchCriteria();
        searchCriteriaStorageRack.withType().withCode().thatEquals("STORAGE_POSITION");
        searchCriteriaStorageRack.withStringProperty("$STORAGE_POSITION.STORAGE_CODE").thatEquals(storageCode);
        searchCriteriaStorageRack.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_ROW").thatEquals(int(storageRackRow));
        searchCriteriaStorageRack.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_COLUMN").thatEquals(int(storageRackColumn));
        searchCriteriaStorageRackResults = context.applicationService.searchSamples(sessionToken, searchCriteriaStorageRack, fetchOptions).getObjects();
        storageRackBoxes = {storageBoxName};
        for sample in searchCriteriaStorageRackResults:
            storageRackBoxes.add(sample.getProperty("$STORAGE_POSITION.STORAGE_BOX_NAME"));
        # 4.3 $STORAGE.BOX_NUM is only checked in is configured
        # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 4.3");
        storageBoxNum = storage.getProperty("$STORAGE.BOX_NUM");
        if storageBoxNum is not None:
            storageBoxNumAsInt = int(storageBoxNum);
            if len(storageRackBoxes) > storageBoxNumAsInt:
                raise UserFailureException("Number of boxes in rack exceeded, use an existing box.");

    # 5. IF $STORAGE.STORAGE_VALIDATION_LEVEL >= BOX_POSITION
    # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - 5");
    if storageValidationLevel == "BOX_POSITION":
        # Storage position format validation (typical mistakes to check before doing any validation requiring database queries)
        if "," in storageBoxPosition:
            raise UserFailureException("Box positions are not separated by ',' but just a white space.");

        if "-" in storageBoxPosition:
            raise UserFailureException("Box positions can't contain ranges '-' .");

        storageBoxPositionRowsAndCols = storageBoxSize.split("X");
        storageBoxPositionNumRows = int(storageBoxPositionRowsAndCols[0]);
        storageBoxPositionNumCols = int(storageBoxPositionRowsAndCols[1]);
        for storageBoxSubPosition in storageBoxPosition.split(" "):
            storageBoxPositionRowNumber = getNumberFromLetter(storageBoxSubPosition[0]);
            if storageBoxPositionRowNumber is None:
                raise UserFailureException("Incorrect format for box position found ''" + storageBoxSubPosition + "'. The first character should be a letter.");
            if storageBoxPositionRowNumber > storageBoxPositionNumRows:
                raise UserFailureException("Row don't fit on the box for position: " + storageBoxSubPosition);
            if not storageBoxSubPosition[1:].isdigit():
                raise UserFailureException("Incorrect format for box position found ''" + storageBoxSubPosition + "'. After the first character only digits are allowed.");
            storageBoxPositionColNumber = int(storageBoxSubPosition[1:]);
            if storageBoxPositionColNumber > storageBoxPositionNumCols:
                raise UserFailureException("Column don't fit on the box for position: " + storageBoxSubPosition);
        #

        for storageBoxSubPosition in storageBoxPosition.split(" "):
            searchCriteriaStorageBoxPosition = SampleSearchCriteria();
            searchCriteriaStorageBoxPosition.withType().withCode().thatEquals("STORAGE_POSITION");
            searchCriteriaStorageBoxPosition.withStringProperty("$STORAGE_POSITION.STORAGE_CODE").thatEquals(storageCode);
            searchCriteriaStorageBoxPosition.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_ROW").thatEquals(int(storageRackRow));
            searchCriteriaStorageBoxPosition.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_COLUMN").thatEquals(int(storageRackColumn));

            if enableNewSearchEngine:
                searchCriteriaStorageBoxPosition.withProperty("$STORAGE_POSITION.STORAGE_BOX_NAME").thatEquals(storageBoxName);
            else: # Patch for Lucene
                import org.apache.lucene.queryparser.classic.QueryParserBase as QueryParserBase
                searchCriteriaStorageBoxPosition.withStringProperty("$STORAGE_POSITION.STORAGE_BOX_NAME").thatEquals(QueryParserBase.escape(storageBoxName));
            searchCriteriaStorageBoxPosition.withStringProperty("$STORAGE_POSITION.STORAGE_BOX_POSITION").thatContains(storageBoxSubPosition);
            searchCriteriaStorageBoxResults = context.applicationService.searchSamples(sessionToken, searchCriteriaStorageBoxPosition, fetchOptions).getObjects();
            # 5.1 If the given box position dont exists (the list is empty), is new
            for sample in searchCriteriaStorageBoxResults:
                if sample.getPermId().getPermId() != samplePermId \
                        and storageBoxSubPosition in sample.getProperty("$STORAGE_POSITION.STORAGE_BOX_POSITION").split(" ") \
                        and sample.getProperty("$STORAGE_POSITION.STORAGE_BOX_NAME") == storageBoxName \
                        and sample.getProperty("$STORAGE_POSITION.STORAGE_CODE") == storageCode:
                    # 5.3 If the given box position already exists, with a different permId -> Is an error
                    raise UserFailureException("You entered an existing box position - Box Name: " + str(storageBoxName) + " Box Position " + storageBoxSubPosition + " is already used by " + sample.getPermId().getPermId());
                else:
                    # 5.2 If the given box position already exists with the same permId -> Is an update
                    pass
    # OPERATION_LOG.info("isValidStoragePositionToInsertUpdate - END");
    return True

def getServiceProperty(context, parameters):
    propertyKey = parameters.get("propertyKey")
    if propertyKey not in ["ui.unarchiving.threshold.relative", "ui.unarchiving.threshold.absolute"]:
        raise UserFailureException("Invalid property: %s" % propertyKey)
    property = CommonServiceProvider.tryToGetBean("propertyConfigurer").getResolvedProps().getProperty(propertyKey)
    if property is None:
        return parameters.get("defaultValue")
    return property

def getNextSequenceForType(context, parameters):
    sampleTypeCode = parameters.get("sampleTypeCode");
    daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
    currentSession = daoFactory.getSessionFactory().getCurrentSession();
    querySampleTypeId = currentSession.createSQLQuery("SELECT id from sample_types WHERE code = :sampleTypeCode");
    querySampleTypeId.setParameter("sampleTypeCode", sampleTypeCode);
    sampleTypeId = querySampleTypeId.uniqueResult();

    querySampleTypePrefix = currentSession.createSQLQuery("SELECT generated_code_prefix from sample_types WHERE code = :sampleTypeCode");
    querySampleTypePrefix.setParameter("sampleTypeCode", sampleTypeCode);
    sampleTypePrefix = querySampleTypePrefix.uniqueResult().upper();
    sampleTypePrefixLengthPlusOneAsString = str((len(sampleTypePrefix) + 1));
    querySampleCount = currentSession.createSQLQuery("SELECT COALESCE(MAX(CAST(substring(code, " + sampleTypePrefixLengthPlusOneAsString + ") as int)), 0) FROM samples_all WHERE saty_id = :sampleTypeId AND code ~ :codePattern");
    querySampleCount.setParameter("sampleTypeId", sampleTypeId);
    querySampleCount.setParameter("codePattern", "^" + sampleTypePrefix + "[0-9]+$");
    sampleCount = querySampleCount.uniqueResult();

    return (sampleCount + 1)

def getNextExperimentCode(context, parameters):
    daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
    currentSession = daoFactory.getSessionFactory().getCurrentSession();

    projectId = int(parameters.get("projectId"));

    queryProjectCode = currentSession.createSQLQuery("SELECT code from projects WHERE id = :projectId");
    queryProjectCode.setParameter("projectId", projectId);
    projectCode = queryProjectCode.uniqueResult();

    experimentPrefix = projectCode + '_EXP_'
    experimentPrefixLengthPlusOneAsString = str((len(experimentPrefix) + 1));

    queryExperimentCount = currentSession.createSQLQuery("SELECT COALESCE(MAX(CAST(substring(code, " + experimentPrefixLengthPlusOneAsString + ") as int)), 0) FROM experiments_all WHERE proj_id = :projectId AND code ~ :codePattern");
    queryExperimentCount.setParameter("projectId", projectId);
    queryExperimentCount.setParameter("codePattern", "^" + experimentPrefix + "[0-9]+$");

    experimentCount = queryExperimentCount.uniqueResult();
    return experimentPrefix + str(experimentCount + 1)

def doSpacesBelongToDisabledUsers(context, parameters):
    daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
    currentSession = daoFactory.getSessionFactory().getCurrentSession();

    spaceCodes = parameters.get("spaceCodes");
    if spaceCodes is None or len(spaceCodes) == 0:
        return []

    disabled_spaces = currentSession.createSQLQuery("SELECT sp.code FROM spaces sp WHERE sp.id IN(SELECT p.space_id FROM persons p WHERE p.space_id IN (SELECT s.id FROM spaces s WHERE s.code IN (:codes)) AND p.is_active = FALSE)");
    disabled_spaces.setParameterList("codes", spaceCodes)
    disabled_spaces_result = disabled_spaces.list()
    return disabled_spaces_result

def trashStorageSamplesWithoutParents(context, parameters, sessionToken):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete import SampleDeletionOptions
    from ch.systemsx.cisd.common.exceptions import UserFailureException

    permIds = [];
    for permId in parameters.get("samplePermIds"):
        permIds.append(SamplePermId(permId));
    fetchOptions = SampleFetchOptions();
    fetchOptions.withType();
    fetchOptions.withParents();
    samplesMapByPermId = context.applicationService.getSamples(sessionToken, permIds, fetchOptions);
    for permId in permIds:
        sample = samplesMapByPermId[permId];
        # Is an storage position
        if sample.getType().getCode() != "STORAGE_POSITION":
            raise UserFailureException("Sample with PermId " + sample.getPermId().getPermId() + " is not an STORAGE_POSITION but instead " + sample.getType().getCode());
        # Doesn't have parents
        if len(sample.getParents()) > 0:
            raise UserFailureException("Sample with PermId " + sample.getPermId().getPermId() + " has " + str(len(sample.getParents())) + " parents.");
    # Delete
    deleteOptions = SampleDeletionOptions();
    deleteOptions.setReason(parameters.get("reason"));
    deletionId = context.applicationService.deleteSamples(sessionToken, permIds, deleteOptions);
    return True