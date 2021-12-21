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
    this.entity = entity

    this.getData = function () {
        function getChanges(timestampToChangesMap, timestamp) {
            var changes = timestampToChangesMap[timestamp]

            if (!changes) {
                changes = {
                    author: undefined,
                    properties: {},
                    relations: {},
                    timestamp: timestamp,
                }
                timestampToChangesMap[timestamp] = changes
            }

            return changes
        }

        function applyChanges(object, changes) {
            function addAll(array, toAdd) {
                var addedMap = {}
                array.forEach(function (item) {
                    addedMap[item] = true
                })
                toAdd.forEach(function (item) {
                    if (!addedMap[item]) {
                        array.push(item)
                    }
                })
                return array
            }

            function removeAll(array, toRemove) {
                var toRemoveMap = {}
                toRemove.forEach(function (item) {
                    toRemoveMap[item] = true
                })
                var newArray = []
                array.forEach(function (item) {
                    if (!toRemoveMap[item]) {
                        newArray.push(item)
                    }
                })
                return newArray
            }

            Object.keys(changes.properties).forEach(function (propertyName) {
                var propertyValue = changes.properties[propertyName]
                if (propertyValue === null) {
                    delete object.properties[propertyName]
                } else {
                    object.properties[propertyName] = propertyValue
                }
            })
            Object.keys(changes.relations).forEach(function (relationType) {
                var relationChanges = changes.relations[relationType]

                if (isRelationOneToMany(relationType)) {
                    var objectRelations = object.relations[relationType]
                    if (!objectRelations) {
                        objectRelations = []
                    }
                    objectRelations = removeAll(objectRelations, relationChanges.removed)
                    objectRelations = addAll(objectRelations, relationChanges.added)
                    object.relations[relationType] = objectRelations
                } else {
                    object.relations[relationType] = relationChanges.set
                }
            })
        }

        function getRelationChanges(changes, relationType) {
            var relationChanges = changes.relations[relationType]

            if (!relationChanges) {
                relationChanges = {
                    added: [],
                    removed: [],
                    set: undefined,
                }
                changes.relations[relationType] = relationChanges
            }

            return relationChanges
        }

        function isRelationOneToMany(relationType) {
            var entityKind = getEntityKind()

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

        function getEntityKind() {
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

        function getEntityPropertyType(propertyName) {
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

        function getEntryType(entry) {
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

        function getEntryRelatedObjectId(entry) {
            var relatedObjectId = entry.relatedObjectId

            if (relatedObjectId === null || relatedObjectId === undefined) {
                return null
            } else if (relatedObjectId["@type"] === "as.dto.history.id.UnknownRelatedObjectId") {
                return relatedObjectId.relatedObjectId + " (" + relatedObjectId.relationType + ")"
            } else {
                return relatedObjectId.permId
            }
        }

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
                var entryType = getEntryType(entry)
                var validFromChanges = getChanges(timestampToChangesMap, entry.validFrom)
                var validToChanges = entry.validTo ? getChanges(timestampToChangesMap, entry.validTo) : null

                if (entry.author) {
                    validFromChanges.author = entry.author.userId
                }

                if (entryType === "PROPERTY") {
                    var propertyType = getEntityPropertyType(entry.propertyName)

                    var propertyFullName = propertyType
                        ? propertyType.label + " [" + propertyType.code + "]"
                        : entry.propertyName

                    validFromChanges.properties[propertyFullName] = entry.propertyValue
                    if (validToChanges) {
                        validToChanges[propertyFullName] = null
                    }
                } else if (entryType === "RELATION") {
                    if (!entry.relationType) {
                        entry.relationType = "UNKNOWN"
                    }

                    var relatedObjectId = getEntryRelatedObjectId(entry)

                    var validFromRelationChanges = getRelationChanges(validFromChanges, entry.relationType)
                    if (isRelationOneToMany(entry.relationType)) {
                        validFromRelationChanges.added.push(relatedObjectId)
                    } else {
                        validFromRelationChanges.set = relatedObjectId
                    }

                    if (validToChanges) {
                        var validToRelationChanges = getRelationChanges(validToChanges, entry.relationType)
                        if (isRelationOneToMany(entry.relationType)) {
                            validToRelationChanges.removed.push(relatedObjectId)
                        } else {
                            validToRelationChanges.set = null
                        }
                    }
                }
            })

            var previousFullDocument = {
                properties: {},
                relations: {},
            }

            return Object.values(timestampToChangesMap)
                .sort(function (ch1, ch2) {
                    // ASC by timestamp
                    return ch1.timestamp - ch2.timestamp
                })
                .map(function (changes, index) {
                    var fullDocument = JSON.parse(JSON.stringify(previousFullDocument))
                    applyChanges(fullDocument, changes)
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
}
