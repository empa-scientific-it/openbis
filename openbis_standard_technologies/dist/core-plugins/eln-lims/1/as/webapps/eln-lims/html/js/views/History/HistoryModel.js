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
        var dataList = []

        function getType(entry) {
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

        function getRelatedObjectId(entry) {
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
            dataList = entity.history.map(function (entry, index) {
                return {
                    id: index,
                    author: entry.author.userId,
                    type: getType(entry),
                    propertyName: entry.propertyName,
                    propertyValue: entry.propertyValue,
                    relationType: entry.relationType,
                    relatedObjectId: getRelatedObjectId(entry),
                    externalCode: entry.externalCode,
                    path: entry.path,
                    gitCommitHash: entry.gitCommitHash,
                    gitRepositoryId: entry.gitRepositoryId,
                    externalDmsId: entry.externalDmsId,
                    externalDmsCode: entry.externalDmsCode,
                    externalDmsLabel: entry.externalDmsLabel,
                    externalDmsAddress: entry.externalDmsAddress,
                    validFrom: Util.getFormatedDate(new Date(entry.validFrom)),
                    validTo: Util.getFormatedDate(new Date(entry.validTo)),
                }
            })
        }

        return dataList
    }
}
