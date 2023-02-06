import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('select term', testSelectTerm)
  test('follow selected term', testFollowSelectedTerm)
})

async function testSelectTerm() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  form.getGrid().getRows()[1].click()
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { selected: false },
        { selected: true },
        { selected: false },
        { selected: false },
        { selected: false },
        { selected: false }
      ]
    },
    parameters: {
      term: {
        title: 'Term',
        code: {
          label: 'Code',
          value: fixture.TEST_TERM_2_DTO.getCode(),
          enabled: false,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: fixture.TEST_TERM_2_DTO.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_TERM_2_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        official: {
          label: 'Official',
          value: fixture.TEST_TERM_2_DTO.isOfficial(),
          enabled: false,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addTerm: {
        enabled: true
      },
      removeTerm: {
        enabled: true
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

async function testFollowSelectedTerm() {
  const form = await common.mountNew()

  const labels = [
    'Term 10',
    'Term 20',
    'Term 30',
    'Term 40',
    'Term 50',
    'Term 60'
  ]

  for (let i = 0; i < labels.length; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(labels[i])
    await form.update()
  }

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: 'Term 10' }, selected: false },
        { values: { label: 'Term 20' }, selected: false },
        { values: { label: 'Term 30' }, selected: false },
        { values: { label: 'Term 40' }, selected: false },
        { values: { label: 'Term 50' }, selected: false }
      ],
      paging: {
        range: '1-5 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 60'
        },
        messages: [
          {
            type: 'warning',
            text: 'The selected object is currently not visible in the list due to the chosen filtering and paging.'
          }
        ]
      }
    }
  })

  form.getGrid().getRows()[0].click()
  await form.update()

  form.getParameters().getTerm().getLabel().change('Term 25')
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: 'Term 20' }, selected: false },
        { values: { label: 'Term 25' }, selected: true },
        { values: { label: 'Term 30' }, selected: false },
        { values: { label: 'Term 40' }, selected: false },
        { values: { label: 'Term 50' }, selected: false }
      ],
      paging: {
        range: '1-5 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 25'
        },
        messages: []
      }
    }
  })

  form.getParameters().getTerm().getLabel().change('Term 65')
  await form.update()

  form.expectJSON({
    grid: {
      rows: [{ values: { label: 'Term 65' }, selected: true }],
      paging: {
        range: '6-6 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 65'
        },
        messages: []
      }
    }
  })

  form.getGrid().getPaging().getFirstPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: 'Term 20' }, selected: false },
        { values: { label: 'Term 30' }, selected: false },
        { values: { label: 'Term 40' }, selected: false },
        { values: { label: 'Term 50' }, selected: false },
        { values: { label: 'Term 60' }, selected: false }
      ],
      paging: {
        range: '1-5 of 6'
      }
    },
    parameters: {
      term: {
        label: {
          value: 'Term 65'
        },
        messages: [
          {
            type: 'warning',
            text: 'The selected object is currently not visible in the list due to the chosen filtering and paging.'
          }
        ]
      }
    }
  })
}
