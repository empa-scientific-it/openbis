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

                            export_referred: whether the referred entities should be exported as well
                        }
        :return: Openbis's execute operations result string. It should contain report on what was created.
    """
    try:
        file_name = parameters.get("file_name")
        vocabularies = map(lambda id: ExportablePermId(ExportableKind.valueOf(id.get("exportable_kind")),
                                                       id.get("perm_id")),
                           parameters.get("ids", {}))

        session_token = context.getSessionToken()
        api = context.getApplicationService()
        xls_export = XLSExport()
        export_result = xls_export.export(file_name, api, session_token, vocabularies,
                                  parameters.get("export_referred", False))
    except Exception as e:
        return {"status": "error", "message": str(e)}
    return {
        "status": "OK",
        "result": {
            "file_name": file_name,
            "file_type": export_result.getFileType().toString().lower(),
            "content": export_result.getBytes()
        }
    }
