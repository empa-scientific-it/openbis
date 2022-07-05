import base64
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.ethz.sis.openbis.generic.server.xls.importer import XLSImport
from ch.ethz.sis.openbis.generic.server.xls.importer import ImportOptions
from ch.ethz.sis.openbis.generic.server.xls.importer.enums import ImportModes
from java.util import ArrayList

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
    """
        Excel import AS service.
        For extensive documentation of usage and Excel layout,
        please visit https://wiki-bsse.ethz.ch/display/openBISDoc/Excel+import+service

        :param context: Standard Openbis AS Service context object
        :param parameters: Contains two elements
                        {
                            'xls' : excel byte blob,    - mandatory
                            'xls_name': identifier of excel file - mandatory
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
    api, session_token = context.applicationService, context.sessionToken

    xls_byte_arrays = parameters.get('xls', None)
    xls_base64_string = parameters.get('xls_base64', None)
    xls_name = parameters.get('xls_name', None)
    scripts = parameters.get('scripts', {})
    mode = get_update_mode(parameters)
    options = get_import_options(parameters)

    if xls_byte_arrays is None and xls_base64_string is not None:
        xls_byte_arrays = [ base64.b64decode(xls_base64_string) ]

    importXls = XLSImport(session_token, api, scripts, mode, options, xls_name)

    ids = ArrayList()
    for xls_byte_array in xls_byte_arrays:
        ids.addAll(importXls.importXLS(xls_byte_array))

    return ids
