import base64
from ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl import MasterDataRegistrationHelper
from ch.ethz.sis.openbis.generic.server.xls.importer import ImportOptions
from ch.ethz.sis.openbis.generic.server.xls.importer import XLSImport
from ch.ethz.sis.openbis.generic.server.xls.importer.enums import ImportModes
from ch.systemsx.cisd.common.exceptions import UserFailureException
from java.util import ArrayList
from org.apache.commons.io import FileUtils
from java.io import File
from java.lang import Long
from java.lang import System
from java.nio.file import Path

def get_update_mode(parameters):
    update_mode = parameters.get('update_mode', 'FAIL_IF_EXISTS')
    if update_mode == "IGNORE_EXISTING":
        return ImportModes.IGNORE_EXISTING
    elif update_mode == "FAIL_IF_EXISTS":
        return ImportModes.FAIL_IF_EXISTS
    elif update_mode == "UPDATE_IF_EXISTS":
        return ImportModes.UPDATE_IF_EXISTS
    else:
        raise UserFailureException('Update mode has to be one of following: IGNORE_EXISTING FAIL_IF_EXISTS UPDATE_IF_EXISTS but was ' + (
            str(update_mode) if update_mode else 'None'))


def get_import_options(parameters):
    options = ImportOptions()
    experiments_by_type = parameters.get('experiments_by_type', None)
    options.setExperimentsByType(experiments_by_type)
    spaces_by_type = parameters.get('spaces_by_type', None)
    options.setSpacesByType(spaces_by_type)
    definitions_only = parameters.get('definitions_only', False)
    options.setDefinitionsOnly(definitions_only)
    disallow_creations = parameters.get("disallow_creations", False)
    options.setDisallowEntityCreations(disallow_creations)
    ignore_versioning = parameters.get('ignore_versioning', False)
    options.setIgnoreVersioning(ignore_versioning)
    render_result = parameters.get('render_result', True)
    options.setRenderResult(render_result)
    return options


def process(context, parameters):
    method = parameters.get("method")
    result = None

    if method == "import":
        zip = parameters.get('zip', False)
        temp = None
        if zip: # Zip mode uses xls_base64 for all multiple XLS + script files
            zip_bytes = base64.b64decode(parameters.get('xls_base64'))
            temp = File.createTempFile("temp", Long.toString(System.nanoTime()))
            temp.delete()
            temp.mkdir()
            tempPath = temp.getAbsolutePath()
            MasterDataRegistrationHelper.extractToDestination(zip_bytes, tempPath)
            if (len(temp.listFiles()) == 1):
                singleFile = temp.listFiles()[0]
                if (singleFile.isDirectory()):
                    temp = singleFile
                    tempPath = singleFile.getAbsolutePath()
            byteArrays = MasterDataRegistrationHelper.getByteArrays(Path.of(tempPath), ".xls")
            if len(byteArrays) == 0:
                raise UserFailureException('No .xls or .xlsx files found on the root folder of the zip file. This error could be caused by the way the zip file was generated.')
            parameters.put('xls', byteArrays)
            allScripts = MasterDataRegistrationHelper.getAllScripts(Path.of(tempPath))
            parameters.put('scripts', allScripts)
        else:
            # Check if xls_base64 is used for a single XLS
            xls_base64_string = parameters.get('xls_base64', None)
            if xls_base64_string is not None:
                parameters.put('xls', [ base64.b64decode(xls_base64_string) ])
        result = _import(context, parameters)
        if temp is not None:
            FileUtils.deleteDirectory(temp)
    return result


def _import(context, parameters):
    """
        Excel import AS service.
        For extensive documentation of usage and Excel layout,
        please visit https://wiki-bsse.ethz.ch/display/openBISDoc/Excel+import+service

        :param context: Standard Openbis AS Service context object
        :param parameters: Contains two elements
            {
                'xls' : excel byte blob,    - optional
                'xls_base64' : base64-encoded excel byte blob - optional
                'xls_name': identifier of excel file - mandatory
                'zip' : True / False - optional (default: False)
                'scripts': {                - optional
                    file path: loaded file
                },
                'experiments_by_type', - optional
                'spaces_by_type', - optional
                'definitions_only', - optional (default: False)
                'disallow_creations', - optional (default: False)
                'ignore_versioning', - optional (default: False)
                'render_result', - optional (default: True)
                'update_mode': [IGNORE_EXISTING|FAIL_IF_EXISTS|UPDATE_IF_EXISTS] - optional, default FAIL_IF_EXISTS
                                                                                 This only takes duplicates that are ON THE SERVER
            }
        :return: Openbis's execute operations result string. It should contain report on what was created.
    """
    session_token = context.sessionToken
    api = context.applicationService
    scripts = parameters.get('scripts', {})
    mode = get_update_mode(parameters)
    options = get_import_options(parameters)
    xls_name = parameters.get('xls_name', None)

    importXls = XLSImport(session_token, api, scripts, mode, options, xls_name)

    ids = ArrayList()
    xls_byte_arrays = parameters.get('xls', None)
    for xls_byte_array in xls_byte_arrays:
        ids.addAll(importXls.importXLS(xls_byte_array))

    return ids
