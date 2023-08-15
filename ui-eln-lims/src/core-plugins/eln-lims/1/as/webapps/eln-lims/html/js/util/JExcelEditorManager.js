
var JExcelEditorManager = new function() {
    this.jExcelEditors = {}

    this.getOnChange = function(guid, propertyCode, entity) {
        var _this = this;
        return function(el, record, x, y, value) {
            var jExcelEditor = _this.jExcelEditors[guid];
            if(jExcelEditor) {
                if(value) {
                    // Change column width
                    var columnWidth = parseInt(jExcelEditor.getWidth(x));
                    var td = el.children[1].children[0].children[2].children[y].children[parseInt(x)+1];
                    var columnScrollWidth = td.scrollWidth;

                    if(columnScrollWidth > columnWidth) {
                        jExcelEditor.setWidth(x, columnScrollWidth + 10);
                    }
                }
                // Save Editor
                var headers = jExcelEditor.getHeaders(true);
                var data = jExcelEditor.getData();
                var style = jExcelEditor.getStyle();
                var meta = jExcelEditor.getMeta();
                var width = jExcelEditor.getWidth();
                var jExcelEditorValue = {
                    headers : headers,
                    data : data,
                    style : style,
                    meta : meta,
                    width : width
                }
                entity.properties[propertyCode] = "<DATA>" + window.btoa(JSON.stringify(jExcelEditorValue)) + "</DATA>";
            }
        }
    }

    this.getObjectFunction = function(guid) {
        var _this = this;
        return function() {
            var jExcelEditor = _this.jExcelEditors[guid];
            var x = null;
            var y = null;
            if(jExcelEditor.selectedCell) {
                x = parseInt(jExcelEditor.selectedCell[0]);
                y = parseInt(jExcelEditor.selectedCell[1]);
            } else {
                Util.showInfo("Select a cell first.");
                return;
            }

            var component = "<div>"
                component += "<legend>Insert Object</legend>";
            	component += "<div>";

                component += "<div class='form-group'>";
                component += "<label class='control-label'>Object:</label>";
                component += "<div>";
                component += "<div id='objectSelector'></div>";
                component += "</div>";
                component += "</div>";

                component += "<div class='form-group'>";
                component += "<label class='control-label'>Options:</label>";
                component += "<div class='controls'>";
                component += "<span class='checkbox'><label><input type='checkbox' id='insertHeaders'> Insert Headers </label></span>";
                component += "</div>";
                component += "</div>";

                component += "</div>";
                component += "</div>";
                Util.blockUI(component + "<a class='btn btn-default' id='insertAccept'>Accept</a> <a class='btn btn-default' id='insertCancel'>Cancel</a>", FormUtil.getDialogCss());

                var advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(true, true, "Select Object", false, true, false, false, false);
                advancedEntitySearchDropdown.init($("#objectSelector"));

                $("#insertCancel").on("click", function(event) {
                    Util.unblockUI();
                });

                $("#insertAccept").on("click", function(event) {
                    var insertHeaders = $("#insertHeaders")[0].checked;
                    var selected = advancedEntitySearchDropdown.getSelected();
                    var lastEntityKindType = null;

                    if(selected.length > 0) {
                        for(var sIdx = 0; sIdx < selected.length; sIdx++) {
                            var entity = selected[sIdx];
                            var entityKindType = entity["@type"] + ":" + entity.type.code;
                            var entityTable = _this.getEntityAsTable(entity);

                            if(insertHeaders && lastEntityKindType !== entityKindType) {
                                //Insert Labels
                                for(var lIdx = 0; lIdx < entityTable.label.length; lIdx++) {
                                    var label = entityTable.label[lIdx];
                                    if(label) {
                                        jExcelEditor.setValueFromCoords(x+lIdx, y, label, true);
                                    }
                                }
                                y++;
                            }

                            //Insert Values
                            for(var vIdx = 0; vIdx < entityTable.value.length; vIdx++) {
                                var value = entityTable.value[vIdx];
                                if(value) {
                                    jExcelEditor.setValueFromCoords(x+vIdx, y, value, true);
                                }
                            }
                            y++;
                        }
                        Util.unblockUI();
                    } else {
                        Util.showInfo("Select an object first.", function() {}, true);
                    }
                });
        }
    }

	this.createField = function($container, mode, propertyCode, entity) {
	    $container.attr('style', 'width: 100%; height: 450px; overflow-y: scroll; overflow-x: scroll;');

        var headers = null;
	    var data = [];
	    var style = null;
	    var meta = null;
        var width = null;
	    if(entity.properties && entity.properties[propertyCode]) {
	        var jExcelEditorValueAsStringWithTags = entity.properties[propertyCode];
	        var jExcelEditorValue = null;
	        if(jExcelEditorValueAsStringWithTags) {
	            var jExcelEditorValueAsStringNoTags = jExcelEditorValueAsStringWithTags.substring(6, jExcelEditorValueAsStringWithTags.length - 7);
                jExcelEditorValue = JSON.parse(window.atob(jExcelEditorValueAsStringNoTags));
	        }
	        if(jExcelEditorValue) {
	            headers = jExcelEditorValue.headers;
	            data = jExcelEditorValue.data;
	            style = jExcelEditorValue.style;
	            meta = jExcelEditorValue.meta;
	            width = jExcelEditorValue.width;
	        }
	    }

        var guid = Util.guid();

        var options = {
                    data: data,
                    style: style,
                    meta: meta,
                    editable : mode !== FormMode.VIEW,
                    minDimensions:[30, 30],
                    toolbar: null,
                    onchangeheader: null,
                    onchange: null,
                    onchangestyle: null,
                    onchangemeta: null
        };

        if(headers) {
            options.colHeaders = headers;
        }

        if(width) {
            options.colWidths = width;
        }

        if(mode === FormMode.VIEW) {
            options.allowInsertRow = false;
            options.allowManualInsertRow = false;
            options.allowInsertColumn = false;
            options.allowManualInsertColumn = false;
            options.allowDeleteRow = false;
            options.allowDeleteColumn = false;
            options.allowRenameColumn = false;
            options.allowComments = false;

            options.contextMenu = function(obj, x, y, e) {
                return [];
            }
        } else {
            var onChangeHandler = this.getOnChange(guid, propertyCode, entity);
            options.onundo = onChangeHandler;
            options.onredo = onChangeHandler;
            options.onchange = onChangeHandler; //
            options.onafterchanges = onChangeHandler;
            options.oninsertrow = onChangeHandler;
            options.oninsertcolumn = onChangeHandler;
            options.ondeleterow = onChangeHandler;
            options.ondeletecolumn = onChangeHandler;
            options.onmoverow = onChangeHandler;
            options.onmovecolumn = onChangeHandler;
            options.onresizerow = onChangeHandler;
            options.onresizecolumn = onChangeHandler;
            options.onsort = onChangeHandler;
            options.onpaste = onChangeHandler;
            options.onmerge = onChangeHandler;
            options.onchangeheader = onChangeHandler; //
            options.oneditionend = onChangeHandler;
            options.onchangestyle = onChangeHandler; //
            options.onchangemeta = onChangeHandler; //

            options.toolbar = [
                    { type:'select', k:'font-family', v:['Arial','Verdana'] },
                    { type:'select', k:'font-size', v:['9px','10px','11px','12px','13px','14px','15px','16px','17px','18px','19px','20px'] },
                    { type:'i', content:'format_align_left', k:'text-align', v:'left' },
                    { type:'i', content:'format_align_center', k:'text-align', v:'center' },
                    { type:'i', content:'format_align_right', k:'text-align', v:'right' },
                    { type:'i', content:'format_bold', k:'font-weight', v:'bold' },
                    { type:'color', content:'format_color_text', k:'color' },
                    { type:'color', content:'format_color_fill', k:'background-color' },
                    { type:'i', content:'input', onclick: this.getObjectFunction(guid) },
            ];
        }

        var jexcelField = jspreadsheet($container[0], options);
        this.jExcelEditors[guid] = jexcelField;
	}

	this.getEntityAsTable = function(entity) {
	    var tableModel = {
	        code : [],
	        label : [],
	        value : [],
	        dataType : [],
	    }

	    if(entity["@type"] === "as.dto.sample.Sample") {
            var sampleType = profile.getSampleTypeForSampleTypeCode(entity.type.code);

            tableModel.code.push("");
            tableModel.label.push("Identifier");
            tableModel.value.push(entity.identifier.identifier);
            tableModel.dataType.push("");

            for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
                var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
                for(var j = 0; j < propertyGroup.length; j++) {
            	    var propertyType = propertyGroup[j];
            	    tableModel.code.push(propertyType.code);
            	    tableModel.label.push(propertyType.label);
            	    tableModel.value.push(entity.properties[propertyType.code]);
            	    tableModel.dataType.push(propertyType.dataType);
                }
            }
	    }
	    return tableModel;
	}
}