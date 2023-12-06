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
                "method": "export",
                "file_name": "<file name>", - prefix of the file name to be exported
                "ids": [
                    {
                        "exportable_kind": "<SAMPLE_TYPE | EXPERIMENT_TYPE | DATASET_TYPE | VOCABULARY_TYPE |
                SPACE | PROJECT | SAMPLE | EXPERIMENT | DATASET>", - entity kind to export
                        "perm_id": "<permID>" - permId of the exportable
                    },
                    ...
                ],
                "export_referred_master_data": true | false, - whether to export referred vocabularies and the types of
                        the properties of type sample
                "export_fields": { - everything in this section is optional
                    "TYPE": {
                        "<SAMPLE_TYPE | EXPERIMENT_TYPE | DATASET_TYPE | VOCABULARY_TYPE | SPACE | PROJECT>": [
                          {"type": "ATTRIBUTE", "id": "<attribute name>"},
                          ...
                        ] - attributes for each type and entity without types to be exported,
                            if the list is empty no attributes will be exported for the given one
                        ...
                    },
                    "SAMPLE": {
                        "<sampleTypePermID>": [
                          {"type": "PROPERTY", "id": "<property code>"},
                          {"type": "ATTRIBUTE", "id": "<attribute name>"},
                          ...
                        ] - fields of each sample type
                            to be exported, if the list is empty no fields will be exported
                            for the sample type
                    },
                    "EXPERIMENT": {
                        "<experimentTypePermID>": [
                            {"type": "PROPERTY", "id": "<property code>"},
                            {"type": "ATTRIBUTE", "id": "<attribute name>"},
                            ...
                        ] - fields of each experiment type
                            to be exported, if the list is empty no fields will be exported
                            for the experiment type
                    },
                    "DATASET": {
                        "<dataSetTypePermID>": [
                          {"type": "PROPERTY", "id": "<property code>"},
                          {"type": "ATTRIBUTE", "id": "<attribute name>"} ,
                          ...
                        ] - fields of each data set type
                            to be exported, if the list is empty no fields will be exported
                            for the data set type
                    }
                },
                "text_formatting": "<PLAIN, RICH>" - if PLAIN, XML tags will be removed from all properties
                    of type MULTILINE_VARCHAR
                "compatible_with_import": true | false - whether this export is intended to be used for import
            }
        :return: Openbis's execute operations result string. Contains either the error message or the exported file name
            in the session workspace.
            Success result:
            {
                "status": "OK",
                "result": {
                    "file_name": <file name>, - name of the file in the session workspace
                    "warnings": [<message 1>, ...] - warnings produced by the import
                }
            }
            Error result:
            {
                "status": "error",
                "message": <message> - error message produced by the import
            }
    """
    try:
        file_name = parameters.get("file_name")
        vocabularies = map(lambda id: ExportablePermId(ExportableKind.valueOf(id.get("exportable_kind")),
                                                       id.get("perm_id")),
                           parameters.get("ids", {}))

        export_fields = parameters.get("export_fields", None)
        session_token = context.getSessionToken()
        api = context.getApplicationService()
        text_formatting = XLSExport.TextFormatting.valueOf(parameters.get("text_formatting"))
        compatible_with_import = parameters.get("compatible_with_import")
        xls_import_result = XLSExport.export(file_name, api, session_token, vocabularies,
                                             parameters.get("export_referred_master_data"), export_fields,
                                             text_formatting, compatible_with_import)
    except Exception as e:
        return {"status": "error", "message": str(e)}
    return {"status": "OK", "result": {
        "file_name": xls_import_result.getFileName(),
        "warnings": xls_import_result.getWarnings()
    }}
