import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('add term', testAddTerm)
})

async function testAddTerm() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  form.expectJSON({
    grid: {
      columns: [
        { name: 'code', sort: 'asc' },
        { name: 'label', sort: null },
        { name: 'description', sort: null },
        { name: 'official', sort: null },
        { name: 'registrator', sort: null },
        { name: 'registrationDate', sort: null }
      ],
      rows: [
        fixture.TEST_TERM_1_DTO,
        fixture.TEST_TERM_2_DTO,
        fixture.TEST_TERM_3_DTO,
        fixture.TEST_TERM_4_DTO,
        fixture.TEST_TERM_5_DTO,
        fixture.TEST_TERM_6_DTO
      ].map(term => ({
        values: {
          code: term.getCode()
        },
        selected: false
      })),
      paging: {
        pageSize: {
          value: 10
        },
        range: '1-6 of 6'
      }
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getGrid().getColumns()[0].getLabel().click()
  await form.update()

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.getButtons().getAddTerm().click()
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { name: 'code', sort: 'desc' },
        { name: 'label', sort: null },
        { name: 'description', sort: null },
        { name: 'official', sort: null },
        { name: 'registrator', sort: null },
        { name: 'registrationDate', sort: null }
      ],
      rows: [
        {
          values: { code: fixture.TEST_TERM_1_DTO.getCode() },
          selected: false
        },
        {
          values: { code: null },
          selected: true
        }
      ],
      paging: {
        pageSize: {
          value: 5
        },
        range: '6-7 of 7'
      }
    },
    parameters: {
      term: {
        title: 'Term',
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        label: {
          label: 'Label',
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
        official: {
          label: 'Official',
          value: true,
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
        enabled: true
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
