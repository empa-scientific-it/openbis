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
                    author: null,
                    properties: {},
                    relations: {},
                    timestamp: timestamp,
                }
                timestampToChangesMap[timestamp] = changes
            }

            return changes
        }

        function applyChanges(object, changes) {
            Object.keys(changes).forEach(function (fieldName) {
                var fieldValue = changes[fieldName]
                if (fieldValue === null) {
                    delete object[fieldName]
                } else {
                    object[fieldName] = fieldValue
                }
            })
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

                validFromChanges.author = entry.author.userId

                if (entryType === "PROPERTY") {
                    validFromChanges.properties[entry.propertyName] = entry.propertyValue
                    if (validToChanges) {
                        validToChanges.properties[entry.propertyName] = null
                    }
                } else if (entryType === "RELATION") {
                    validFromChanges.relations[entry.relationType] = getEntryRelatedObjectId(entry)
                    if (validToChanges) {
                        validToChanges.relations[entry.relationType] = null
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
                    applyChanges(fullDocument.properties, changes.properties)
                    applyChanges(fullDocument.relations, changes.relations)
                    changes.fullDocument = fullDocument
                    previousFullDocument = fullDocument
                    return {
                        id: index,
                        author: changes.author,
                        changes: JSON.stringify({
                            properties: changes.properties,
                            relations: changes.relations,
                        }),
                        fullDocument: JSON.stringify(fullDocument),
                        timestamp: Util.getFormatedDate(new Date(changes.timestamp)),
                        $object: changes,
                    }
                })
        } else {
            return []
        }
    }
}
