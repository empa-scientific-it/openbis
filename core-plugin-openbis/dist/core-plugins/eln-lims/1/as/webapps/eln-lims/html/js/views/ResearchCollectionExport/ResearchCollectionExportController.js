/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function ResearchCollectionExportController(parentController) {
    var researchCollectionExportModel = new ResearchCollectionExportModel();
    var researchCollectionExportView = new ResearchCollectionExportView(this, researchCollectionExportModel);

    this.init = function(views) {
        researchCollectionExportView.repaint(views);
    };

    this.initialiseSubmissionTypesDropdown = function(callback) {
        Util.blockUI();
        mainController.serverFacade.listSubmissionTypes(function(error, result) {
            Util.unblockUI();
            if (error) {
                Util.showError(error);
            } else {
                researchCollectionExportModel.submissionTypes = result.data.map(function (resultItem) {
                    return {
                        value: resultItem.url,
                        label: resultItem.title
                    };
                });
                researchCollectionExportView.refreshSubmissionTypeDropdown();
            }
        });
    };

    this.exportSelected = function() {
        var _this = this;
        var selectedNodes = $(researchCollectionExportModel.tree).fancytree('getTree').getSelectedNodes();

        var selectedStOption = researchCollectionExportView.$submissionTypeDropdown.find(":selected");
        var submissionUrl = selectedStOption.val();
        var submissionType = selectedStOption.text();

        var selectedRpOption = researchCollectionExportView.$retentionPeriodDropdown.find(":selected");
        var retentionPeriod = selectedRpOption.val();

        var groupRows = researchCollectionExportModel.tableModel.getValues();
        var nameColumn = researchCollectionExportModel.tableModel.columns[0];
        var valueColumn = researchCollectionExportModel.tableModel.columns[1];
        var checkedGroups = groupRows.flatMap(row => row[valueColumn.label] ? [row[nameColumn.label]] : []);

        var toExport = [];
        for (var eIdx = 0; eIdx < selectedNodes.length; eIdx++) {
            var node = selectedNodes[eIdx];
            toExport.push({type: node.data.entityType, permId: node.key, expand: !node.expanded});
        }

        if (toExport.length === 0) {
            Util.showInfo('First select something to export.');
        } else if (!this.isValid(toExport)) {
            Util.showInfo('Not only spaces and the root should be selected. It will result in an empty export file.');
        } else if (!submissionUrl) {
            Util.showInfo('Select submission type.');
        } else if (!retentionPeriod) {
            Util.showInfo('Select retention period.');
        } else if (groupRows.length > 0 && checkedGroups.length === 0) {
            Util.showInfo('At least one group should be selected.');
        } else {
            Util.blockUI();

            for (var i = 0; i < checkedGroups.length; i++) {
                var group = checkedGroups[i];
                toExport.push({type: 'GROUP', permId: 'GROUP:' + group, expand: null});
            }

            this.getUserInformation(function(userInformation) {
                mainController.serverFacade.exportRc(toExport, true, false, submissionUrl, submissionType, retentionPeriod, userInformation,
                        function(operationExecutionPermId) {
                            _this.waitForOpExecutionResponse(operationExecutionPermId, function(error, result) {
                                Util.unblockUI();
                                if (result && result.data && result.data.url) {
                                    var win = window.open(result.data.url, '_blank');
                                    win.focus();
                                    mainController.refreshView();
                                } else {
                                    Util.showError(error ? error : 'Returned result format is not correct.');
                                }
                            });
                        });
            });
        }
    };

    this.isValid = function(toExport) {
        for (var i = 0; i < toExport.length; i++) {
            var value = toExport[i];
            if (value.type !== 'ROOT' && value.type !== 'SPACE' || value.expand) {
                return true;
            }
        }
        return false;
    };

    this.waitForOpExecutionResponse = function(operationExecutionPermIdString, callbackFunction) {
        var _this = this;
        require(["as/dto/operation/id/OperationExecutionPermId",
                "as/dto/operation/fetchoptions/OperationExecutionFetchOptions"],
            function(OperationExecutionPermId, OperationExecutionFetchOptions) {
                var operationExecutionPermId = new OperationExecutionPermId(operationExecutionPermIdString);
                var fetchOptions = new OperationExecutionFetchOptions();
                var fetchOptionsDetails = fetchOptions.withDetails();
                fetchOptionsDetails.withResults();
                fetchOptionsDetails.withError();
                mainController.openbisV3.getOperationExecutions([operationExecutionPermId], fetchOptions).done(function(results) {
                    var result = results[operationExecutionPermIdString];
                    var v2Result = null;
                    if (result && result.details && result.details.results) {
                        v2Result = result.details.results[0];
                    }

                    if (result && result.state === 'FINISHED') {
                        mainController.serverFacade.customELNApiCallbackHandler(v2Result, callbackFunction);
                    } else if (!result || result.state === 'FAILED') {
                        mainController.serverFacade.customELNApiCallbackHandler(v2Result, callbackFunction);
                    } else {
                        setTimeout(function() {
                            _this.waitForOpExecutionResponse(operationExecutionPermIdString, callbackFunction);
                        }, 3000);
                    }
                });
            });
    };

    this.getUserInformation = function(callback) {
        var userId = mainController.serverFacade.getUserId();
        mainController.serverFacade.getSessionInformation(function(sessionInfo) {
            var userInformation = {
                firstName: sessionInfo.person.firstName,
                lastName: sessionInfo.person.lastName,
                email: sessionInfo.person.email,
                id: userId,
            };
            callback(userInformation);
        });
    };
}