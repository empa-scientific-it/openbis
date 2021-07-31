import TypeSearchComponent from '@srcTest/js/components/types/search/TypeSearchComponent.js'
import TypeSearchTestData from '@srcTest/js/components/types/search/TypeSearchTestData.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new TypeSearchComponent()
  common.beforeEach()
})

describe(TypeSearchComponent.SUITE, () => {
  test('load with searchText (results found)', async () =>
    await testLoadWithSearchText(true))
  test('load with searchText (no results)', async () =>
    await testLoadWithSearchText(false))
  test('load with objectType (results found)', async () =>
    await testLoadWithObjectType(true))
  test('load with objectType (no results)', async () =>
    await testLoadWithObjectType(false))
})

async function testLoadWithSearchText(resultsFound) {
  const {
    testObjectType,
    anotherObjectType,
    testCollectionType,
    anotherCollectionType,
    testDataSetType,
    anotherDataSetType,
    testMaterialType,
    anotherMaterialType,
    testVocabularyType,
    anotherVocabularyType
  } = TypeSearchTestData

  openbis.mockSearchSampleTypes(
    resultsFound ? [testObjectType, anotherObjectType] : []
  )
  openbis.mockSearchExperimentTypes(
    resultsFound ? [testCollectionType, anotherCollectionType] : []
  )
  openbis.mockSearchDataSetTypes(
    resultsFound ? [testDataSetType, anotherDataSetType] : []
  )
  openbis.mockSearchMaterialTypes(
    resultsFound ? [testMaterialType, anotherMaterialType] : []
  )
  openbis.mockSearchVocabularies(
    resultsFound ? [testVocabularyType, anotherVocabularyType] : []
  )

  const form = await common.mount({ searchText: 'test' })

  if (resultsFound) {
    form.expectJSON({
      messages: [],
      objectTypes: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'validationPlugin',
            label: 'Validation Plugin'
          },
          {
            name: 'generatedCodePrefix',
            label: 'Generated code prefix'
          },
          {
            name: 'autoGeneratedCode',
            label: 'Generate Codes'
          },
          {
            name: 'subcodeUnique',
            label: 'Unique Subcodes'
          },
          {
            name: 'showParents',
            label: 'Show Parents'
          },
          {
            name: 'showContainer',
            label: 'Show Container'
          }
        ],
        rows: [
          {
            values: {
              code: testObjectType.getCode(),
              description: testObjectType.getDescription(),
              validationPlugin: testObjectType.validationPlugin.name,
              generatedCodePrefix: testObjectType.getGeneratedCodePrefix(),
              autoGeneratedCode: String(testObjectType.isAutoGeneratedCode()),
              subcodeUnique: String(testObjectType.isSubcodeUnique()),
              showParents: String(testObjectType.isShowParents()),
              showContainer: String(testObjectType.isShowContainer())
            }
          }
        ]
      },
      collectionTypes: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'validationPlugin',
            label: 'Validation Plugin'
          }
        ],
        rows: [
          {
            values: {
              code: testCollectionType.getCode(),
              description: testCollectionType.getDescription(),
              validationPlugin: testCollectionType.validationPlugin.name
            }
          }
        ]
      },
      dataSetTypes: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'validationPlugin',
            label: 'Validation Plugin'
          },
          {
            name: 'mainDataSetPattern',
            label: 'Main Data Set Pattern'
          },
          {
            name: 'mainDataSetPath',
            label: 'Main Data Set Path'
          },
          {
            name: 'disallowDeletion',
            label: 'Disallow Deletion'
          }
        ],
        rows: [
          {
            values: {
              code: testDataSetType.getCode(),
              description: testDataSetType.getDescription(),
              validationPlugin: testDataSetType.validationPlugin.name,
              mainDataSetPattern: testDataSetType.getMainDataSetPattern(),
              mainDataSetPath: testDataSetType.getMainDataSetPath(),
              disallowDeletion: String(testDataSetType.isDisallowDeletion())
            }
          }
        ]
      },
      materialTypes: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'validationPlugin',
            label: 'Validation Plugin'
          }
        ],
        rows: [
          {
            values: {
              code: testMaterialType.getCode(),
              description: testMaterialType.getDescription(),
              validationPlugin: testMaterialType.validationPlugin.name
            }
          }
        ]
      },
      vocabularyTypes: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'urlTemplate',
            label: 'URL Template'
          }
        ],
        rows: [
          {
            values: {
              code: testVocabularyType.getCode(),
              description: testVocabularyType.getDescription(),
              urlTemplate: testVocabularyType.getUrlTemplate()
            }
          }
        ]
      }
    })
  } else {
    form.expectJSON({
      messages: [
        {
          text: 'No results found',
          type: 'info'
        }
      ],
      objectTypes: null,
      collectionTypes: null,
      dataSetTypes: null,
      materialTypes: null,
      vocabularyTypes: null
    })
  }
}

async function testLoadWithObjectType(resultsFound) {
  const { testObjectType, anotherObjectType } = TypeSearchTestData

  openbis.mockSearchSampleTypes(
    resultsFound ? [testObjectType, anotherObjectType] : []
  )

  const form = await common.mount({
    objectType: objectTypes.OBJECT_TYPE
  })

  form.expectJSON({
    messages: [],
    objectTypes: {
      columns: [
        {
          name: 'code',
          label: 'Code'
        },
        {
          name: 'description',
          label: 'Description'
        },
        {
          name: 'validationPlugin',
          label: 'Validation Plugin'
        },
        {
          name: 'generatedCodePrefix',
          label: 'Generated code prefix'
        },
        {
          name: 'autoGeneratedCode',
          label: 'Generate Codes'
        },
        {
          name: 'subcodeUnique',
          label: 'Unique Subcodes'
        },
        {
          name: 'showParents',
          label: 'Show Parents'
        },
        {
          name: 'showContainer',
          label: 'Show Container'
        }
      ],
      rows: resultsFound
        ? [
            {
              values: {
                code: anotherObjectType.getCode(),
                description: anotherObjectType.getDescription(),
                validationPlugin: null,
                generatedCodePrefix: null,
                autoGeneratedCode: null,
                subcodeUnique: null,
                showParents: null,
                showContainer: null
              }
            },
            {
              values: {
                code: testObjectType.getCode(),
                description: testObjectType.getDescription(),
                validationPlugin: testObjectType.validationPlugin.name,
                generatedCodePrefix: testObjectType.getGeneratedCodePrefix(),
                autoGeneratedCode: String(testObjectType.isAutoGeneratedCode()),
                subcodeUnique: String(testObjectType.isSubcodeUnique()),
                showParents: String(testObjectType.isShowParents()),
                showContainer: String(testObjectType.isShowContainer())
              }
            }
          ]
        : []
    },
    collectionTypes: null,
    dataSetTypes: null,
    materialTypes: null,
    vocabularyTypes: null
  })
}
