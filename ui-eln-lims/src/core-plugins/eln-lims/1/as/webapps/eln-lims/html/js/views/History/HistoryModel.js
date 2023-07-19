/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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

function HistoryModel(entity) {
    var _this = this
    this.entity = entity

    this.getHeader = function () {
        var entityKind = _this._getEntityKind()
        var header = "History of "

        if (entityKind === "PROJECT") {
            header += "Project: " + FormUtil.getProjectName(entity.code)
        } else if (entityKind === "EXPERIMENT") {
            header +=
                Util.getDisplayNameFromCode(entity.type.code) +
                ": " +
                FormUtil.getExperimentName(entity.code, entity.properties)
        } else if (entityKind === "SAMPLE") {
            header +=
                Util.getDisplayNameFromCode(entity.type.code) +
                ": " +
                FormUtil.getSampleName(entity.code, entity.properties, entity.type.code)
        } else if (entityKind === "DATA_SET") {
            header += "Dataset: " + FormUtil.getDataSetName(entity.code, entity.properties)
        } else {
            throw new Error("Unknown entity kind: " + entityKind)
        }

        return header
    }

    this.getData = function () {
        if (entity.history) {
            var entries = Array.from(entity.history).sort(function (e1, e2) {
                // ASC by validFrom, ASC by validTo NULLs last
                if (e1.validFrom - e2.validFrom === 0) {
                    return (e1.validTo || Number.MAX_VALUE) - (e2.validTo || Number.MAX_VALUE)
                } else {
                    return e1.validFrom - e2.validFrom
                }
            })

            var timestampToChangesMap = {}

            entries.forEach(function (entry) {
                var entryType = _this._getEntryType(entry)
                var validFromChanges = _this._getChanges(timestampToChangesMap, entry.validFrom)
                var validToChanges = entry.validTo ? _this._getChanges(timestampToChangesMap, entry.validTo) : null

                if (entry.author) {
                    validFromChanges.author = entry.author.userId
                }

                if (entryType === "PROPERTY") {
                    var validFromPropertyChanges = _this._getPropertyChanges(validFromChanges, entry.propertyName)
                    if(validFromPropertyChanges.propertyType.multiValue) {
//                        validFromPropertyChanges.newValue = (validFromPropertyChanges.newValue ?? []).concat(entry.propertyValue);
                        _this._setMultiValueProperty(validFromPropertyChanges, 'newValue', entry.propertyValue);
                    } else {
                        validFromPropertyChanges.newValue = entry.propertyValue
                    }
                    if (validToChanges) {
                        var validToPropertyChanges = _this._getPropertyChanges(validToChanges, entry.propertyName)
                        if(validToPropertyChanges.propertyType.multiValue) {
//                            validToPropertyChanges.oldValue = (validToPropertyChanges.oldValue ?? []).concat(entry.propertyValue);
                            _this._setMultiValueProperty(validToPropertyChanges, 'oldValue', entry.propertyValue);
                        } else {
                            validToPropertyChanges.oldValue = entry.propertyValue
                        }
                    }
                } else if (entryType === "RELATION") {
                    if (!entry.relationType) {
                        entry.relationType = "UNKNOWN"
                    }

                    var relatedObjectId = _this._getEntryRelatedObjectId(entry)

                    var validFromRelationChanges = _this._getRelationChanges(validFromChanges, entry.relationType)
                    if (_this._isRelationOneToMany(entry.relationType)) {
                        validFromRelationChanges.added.push(relatedObjectId)
                    } else {
                        validFromRelationChanges.newValue = relatedObjectId
                    }

                    if (validToChanges) {
                        var validToRelationChanges = _this._getRelationChanges(validToChanges, entry.relationType)
                        if (_this._isRelationOneToMany(entry.relationType)) {
                            validToRelationChanges.removed.push(relatedObjectId)
                        } else {
                            validToRelationChanges.oldValue = relatedObjectId
                        }
                    }
                } else if (entryType === "CONTENT_COPY") {
                    var contentCopy = {
                        externalCode: entry.externalCode,
                        path: entry.path,
                        gitCommitHash: entry.gitCommitHash,
                        gitRepositoryId: entry.gitRepositoryId,
                        externalDmsId: entry.externalDmsId,
                        externalDmsCode: entry.externalDmsCode,
                        externalDmsLabel: entry.externalDmsLabel,
                        externalDmsAddress: entry.externalDmsAddress,
                    }

                    var validFromContentCopyChanges = _this._getContentCopyChanges(validFromChanges)
                    validFromContentCopyChanges.added.push(contentCopy)

                    if (validToChanges) {
                        var validToContentCopyChanges = _this._getContentCopyChanges(validToChanges)
                        validToContentCopyChanges.removed.push(contentCopy)
                    }
                }
            })

            var previousFullDocument = {}

            return Object.values(timestampToChangesMap)
                .sort(function (ch1, ch2) {
                    // ASC by timestamp
                    return ch1.timestamp - ch2.timestamp
                })
                .map(function (changes, index) {
                    var fullDocument = JSON.parse(JSON.stringify(previousFullDocument))
                    _this._applyChanges(fullDocument, changes)
                    previousFullDocument = fullDocument

                    return {
                        id: index,
                        version: index + 1,
                        changes: changes,
                        fullDocument: fullDocument,
                    }
                })
        } else {
            return []
        }
    }

    this._getEntityKind = function () {
        var type = entity["@type"]

        if (type === "as.dto.project.Project") {
            return "PROJECT"
        } else if (type === "as.dto.experiment.Experiment") {
            return "EXPERIMENT"
        } else if (type === "as.dto.sample.Sample") {
            return "SAMPLE"
        } else if (type === "as.dto.dataset.DataSet") {
            return "DATA_SET"
        } else {
            throw new Error("Unknown entity kind: " + JSON.stringify(entity))
        }
    }

    this._getEntityPropertyType = function (propertyName) {
        if (entity.type && entity.type.propertyAssignments) {
            var propertyAssignment = entity.type.propertyAssignments.find(function (propertyAssignment) {
                return propertyAssignment.propertyType && propertyAssignment.propertyType.code === propertyName
            })
            if (propertyAssignment) {
                return propertyAssignment.propertyType
            }
        }
        return null
    }

    this._getChanges = function (timestampToChangesMap, timestamp) {
        var changes = timestampToChangesMap[timestamp]

        if (!changes) {
            changes = {
                timestamp: timestamp,
            }
            timestampToChangesMap[timestamp] = changes
        }

        return changes
    }

    this._applyChanges = function (object, changes) {
        function getKey(item) {
            return item !== null && typeof item === "object" ? JSON.stringify(item) : item
        }

        function addAll(array, toAdd) {
            var addedMap = {}
            array.forEach(function (item) {
                addedMap[getKey(item)] = true
            })
            toAdd.forEach(function (item) {
                if (!addedMap[getKey(item)]) {
                    array.push(item)
                }
            })
            return array
        }

        function removeAll(array, toRemove) {
            var toRemoveMap = {}
            toRemove.forEach(function (item) {
                toRemoveMap[getKey(item)] = true
            })
            var newArray = []
            array.forEach(function (item) {
                if (!toRemoveMap[getKey(item)]) {
                    newArray.push(item)
                }
            })
            return newArray
        }

        if (changes.properties) {
            if (!object.properties) {
                object.properties = {}
            }
            Object.keys(changes.properties).forEach(function (propertyName) {
                var property = changes.properties[propertyName]
                var propertyKey = property.label + " [" + property.code + "]"

                if (property.newValue === null || property.newValue === undefined) {
                    delete object.properties[propertyKey]
                } else {
                    object.properties[propertyKey] = property.newValue
                }
            })
            if (_.isEmpty(object.properties)) {
                delete object.properties
            }
        }

        if (changes.relations) {
            if (!object.relations) {
                object.relations = {}
            }
            Object.keys(changes.relations).forEach(function (relationType) {
                var relationChanges = changes.relations[relationType]

                if (_this._isRelationOneToMany(relationType)) {
                    var objectRelations = object.relations[relationType]

                    if (!objectRelations) {
                        objectRelations = []
                    }

                    objectRelations = removeAll(objectRelations, relationChanges.removed)
                    objectRelations = addAll(objectRelations, relationChanges.added)

                    if (objectRelations.length > 0) {
                        object.relations[relationType] = objectRelations
                    } else {
                        delete object.relations[relationType]
                    }
                } else {
                    if (relationChanges.newValue === null || relationChanges.newValue === undefined) {
                        delete object.relations[relationType]
                    } else {
                        object.relations[relationType] = relationChanges.newValue
                    }
                }
            })
            if (_.isEmpty(object.relations)) {
                delete object.relations
            }
        }

        if (changes.contentCopies) {
            if (!object.contentCopies) {
                object.contentCopies = []
            }
            object.contentCopies = removeAll(object.contentCopies, changes.contentCopies.removed)
            object.contentCopies = addAll(object.contentCopies, changes.contentCopies.added)
            if (_.isEmpty(object.contentCopies)) {
                delete object.contentCopies
            }
        }
    }

    this._getRelationChanges = function (changes, relationType) {
        if (!changes.relations) {
            changes.relations = {}
        }

        var relationChanges = changes.relations[relationType]

        if (!relationChanges) {
            relationChanges = {
                added: [],
                removed: [],
                oldValue: undefined,
                newValue: undefined,
            }
            changes.relations[relationType] = relationChanges
        }

        return relationChanges
    }

    this._getPropertyChanges = function (changes, propertyName) {
        if (!changes.properties) {
            changes.properties = {}
        }

        var propertyChanges = changes.properties[propertyName]

        if (!propertyChanges) {
            var propertyType = _this._getEntityPropertyType(propertyName)

            propertyChanges = {
                code: propertyType.code,
                label: propertyType.label,
                propertyType: propertyType,
                oldValue: undefined,
                newValue: undefined,
            }
            changes.properties[propertyName] = propertyChanges
        }

        return propertyChanges
    }

    this._getContentCopyChanges = function (changes) {
        if (!changes.contentCopies) {
            changes.contentCopies = {
                added: [],
                removed: [],
            }
        }
        return changes.contentCopies
    }

    this._isRelationOneToMany = function (relationType) {
        var entityKind = _this._getEntityKind()

        if (relationType === "UNKNOWN") {
            return true
        }

        if (entityKind === "PROJECT") {
            return ["EXPERIMENT", "SAMPLE"].includes(relationType)
        } else if (entityKind === "EXPERIMENT") {
            return ["SAMPLE", "DATA_SET"].includes(relationType)
        } else if (entityKind === "SAMPLE") {
            return ["PARENT", "CHILD", "COMPONENT", "DATA_SET"].includes(relationType)
        } else if (entityKind === "DATA_SET") {
            return ["PARENT", "CHILD", "CONTAINER", "COMPONENT"].includes(relationType)
        } else {
            throw new Error("Unknown entity kind: " + entityKind)
        }
    }

    this._getEntryType = function (entry) {
        var type = entry["@type"]

        if (type === "as.dto.history.PropertyHistoryEntry") {
            return "PROPERTY"
        } else if (type === "as.dto.history.RelationHistoryEntry") {
            return "RELATION"
        } else if (type === "as.dto.history.ContentCopyHistoryEntry") {
            return "CONTENT_COPY"
        } else {
            throw new Error("Unknown entry type: " + JSON.stringify(entry))
        }
    }

    this._getEntryRelatedObjectId = function (entry) {
        var relatedObjectId = entry.relatedObjectId

        if (relatedObjectId === null || relatedObjectId === undefined) {
            return null
        } else if (relatedObjectId["@type"] === "as.dto.history.id.UnknownRelatedObjectId") {
            return relatedObjectId.relatedObjectId + " (" + relatedObjectId.relationType + ")"
        } else {
            return relatedObjectId.permId
        }
    }

    this._setMultiValueProperty = function(propertyChange, valueType, value) {
        if(propertyChange[valueType]) {
            if(propertyChange.propertyType.dataType == "CONTROLLEDVOCABULARY") {
                var lastVal = propertyChange[valueType][propertyChange[valueType].length-1];
                lastVal = lastVal.substring(0, lastVal.lastIndexOf(' ['));
                propertyChange[valueType][propertyChange[valueType].length-1] = lastVal;
            }
            propertyChange[valueType] = propertyChange[valueType].concat(value);
        } else {
            propertyChange[valueType] = [value];
        }
    }

}
