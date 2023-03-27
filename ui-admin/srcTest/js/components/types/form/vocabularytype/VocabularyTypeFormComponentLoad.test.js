import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await common.mountNew()

  form.expectJSON({
    grid: {
      rows: []
    },
    parameters: {
      vocabulary: {
        title: 'New Vocabulary Type',
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        urlTemplate: {
          label: 'URL Template',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addTerm: {
        enabled: true
      },
      removeTerm: {
        enabled: false
      },
      save: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  const gridJSON = {
    columns: [
      {
        name: 'code',
        label: 'Code',
        filter: null,
        sort: 'asc'
      },
      {
        name: 'label',
        label: 'Label',
        filter: null,
        sort: null
      },
      {
        name: 'description',
        label: 'Description',
        filter: null,
        sort: null
      },
      {
        name: 'official',
        label: 'Official',
        filter: null,
        sort: null
      },
      {
        name: 'registrator',
        label: 'Registrator',
        filter: null,
        sort: null
      },
      {
        name: 'registrationDate',
        label: 'Registration Date',
        filter: null,
        sort: null
      }
    ],
    rows: fixture.TEST_VOCABULARY_DTO.terms.map(term => ({
      values: {
        code: term.getCode(),
        label: term.getLabel(),
        description: term.getDescription(),
        official: String(term.isOfficial())
      },
      selected: false
    }))
  }

  form.expectJSON({
    grid: gridJSON,
    parameters: {
      vocabulary: {
        title: 'Vocabulary Type',
        code: {
          label: 'Code',
          value: fixture.TEST_VOCABULARY_DTO.getCode(),
          mode: 'view'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_VOCABULARY_DTO.getDescription(),
          mode: 'view'
        },
        urlTemplate: {
          label: 'URL Template',
          value: fixture.TEST_VOCABULARY_DTO.getUrlTemplate(),
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addTerm: null,
      removeTerm: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    grid: gridJSON,
    parameters: {
      vocabulary: {
        title: 'Vocabulary Type',
        code: {
          label: 'Code',
          value: fixture.TEST_VOCABULARY_DTO.getCode(),
          enabled: false,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_VOCABULARY_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        urlTemplate: {
          label: 'URL Template',
          value: fixture.TEST_VOCABULARY_DTO.getUrlTemplate(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addTerm: {
        enabled: true
      },
      removeTerm: {
        enabled: false
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })
}
