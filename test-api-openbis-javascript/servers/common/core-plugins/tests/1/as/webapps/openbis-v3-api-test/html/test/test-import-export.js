/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/dtos' ],
  function($, _, openbis, openbisExecuteOperations, common, dtos) {
    var fileContent = "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";

    var executeModule = function(moduleName, facade, dtos) {
      QUnit.module(moduleName);

      var testAction = function(c, fAction, fCheck) {
        c.start();

        c.login(facade).then(function() {
          c.ok("Login");
          return fAction(facade).then(function(result) {
            c.ok("Got results");
            var token = fCheck(facade, result);
            if (token) {
              token.then(function() {
                c.finish()
              });
            } else {
              c.finish();
            }
          });
        }).fail(function(error) {
          c.fail(error.message);
          c.finish();
        });
      }

      QUnit.test("executeImport()", function(assert) {
        var c = new common(assert, dtos);

        var fAction = function(facade) {
          var importData = new dtos.UncompressedImportData();
          importData.setFormat("XLS");
          importData.setFile(fileContent);

          var importOptions = new dtos.ImportOptions();
          importOptions.setMode("UPDATE_IF_EXISTS");

          return facade.executeImport(importData, importOptions);
        }

        var fCheck = function(facade) {
          var criteria = new dtos.VocabularySearchCriteria();
          criteria.withCode().thatEquals("VOCAB");

          var vocabularyFetchOptions = c.createVocabularyFetchOptions()
          vocabularyFetchOptions.withTerms();

          return facade.searchVocabularies(criteria, vocabularyFetchOptions).then(function(results) {
            c.assertEqual(results.getTotalCount(), 1)
            var vocabulary = (results.getObjects())[0];

            c.assertEqual(vocabulary.code, "VOCAB");

            var terms = vocabulary.getTerms();

            c.assertEqual(terms.length, 3)

            var codes = terms.map(function(object) {
              return object.code;
            }).sort();

            var labels = terms.map(function(object) {
              return object.label;
            }).sort();

            c.assertEqual(codes[0], "TERM_A");
            c.assertEqual(codes[1], "TERM_B");
            c.assertEqual(codes[2], "TERM_C");

            c.assertEqual(labels[0], "A");
            c.assertEqual(labels[1], "B");
            c.assertEqual(labels[2], "C");
          }).fail(function(error) {
            c.fail("Error searching vocabularies. error=" + error.message);
          });
        }

        testAction(c, fAction, fCheck);
      });
    }

    return function() {
        executeModule("Export/import tests (RequireJS)", new openbis(), dtos);
        executeModule("Export/import tests (RequireJS - executeOperations)", new openbisExecuteOperations(new openbis(), dtos), dtos);
        executeModule("Export/import tests (module VAR)", new window.openbis.openbis(), window.openbis);
        executeModule("Export/import tests (module VAR - executeOperations)", new openbisExecuteOperations(new window.openbis.openbis(), window.openbis), window.openbis);
        executeModule("Export/import tests (module ESM)", new window.openbisESM.openbis(), window.openbisESM);
        executeModule("Export/import tests (module ESM - executeOperations)", new openbisExecuteOperations(new window.openbisESM.openbis(), window.openbisESM), window.openbisESM);
    }
  });
