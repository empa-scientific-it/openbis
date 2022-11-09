from ch.ethz.sis.openbis.generic.server.xls.export import XLSExport, ExportablePermId, ExportableKind


def process(context, parameters):
    method = parameters.get("method")
    result = None

    if method == "export":
        result = export(context, parameters)

    return result


def export(context, parameters):
    """
        Excel export AS service.
        :param context: Standard Openbis AS Service context object
        :param parameters: Contains the following elements:
                        {
                            ids: [{
                                exportable_kind: [SAMPLE_TYPE | EXPERIMENT_TYPE | DATASET_TYPE | VOCABULARY | SPACE |
                                    PROJECT | SAMPLE | EXPERIMENT | DATASET] - entity kind to export
                                perm_id: permId of the exportable
                            }]

                            export_referred: whether the referred vocabularies and sample properties should be exported as well
                        }
        :return: Openbis's execute operations result string. Contains either the error message or the exported file name
            in the session workspace.
    """
    try:
        file_name = parameters.get("file_name")
        vocabularies = map(lambda id: ExportablePermId(ExportableKind.valueOf(id.get("exportable_kind")),
                                                       id.get("perm_id")),
                           parameters.get("ids", {}))

        export_properties = parameters.get("export_properties", None)
        session_token = context.getSessionToken()
        api = context.getApplicationService()
        text_formatting = XLSExport.TextFormatting.valueOf(parameters.get("text_formatting"))
        xls_import_result = XLSExport.export(file_name, api, session_token, vocabularies,
                                          parameters.get("export_referred"), export_properties, text_formatting)
    except Exception as e:
        return {"status": "error", "message": str(e)}
    return {"status": "OK", "result": {
        "file_name": xls_import_result.getFileName(),
        "warnings": xls_import_result.getWarnings()
    }}
