import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('remove term', testRemoveTerm)
})

async function testRemoveTerm() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  form.getButtons().getEdit().click()
  await form.update()

  form.getGrid().getPaging().getPageSize().change(5)
  form.getGrid().getPaging().getNextPage().click()
  await form.update()

  form.getGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [fixture.TEST_TERM_6_DTO].map(term => ({
        values: {
          code: term.getCode(),
          label: term.getLabel(),
          description: term.getDescription(),
          official: String(term.isOfficial())
        },
        selected: true
      })),
      paging: {
        pageSize: {
          value: 5
        },
        range: '6-6 of 6'
      }
    }
  })

  form.getButtons().getRemoveTerm().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        fixture.TEST_TERM_1_DTO,
        fixture.TEST_TERM_2_DTO,
        fixture.TEST_TERM_3_DTO,
        fixture.TEST_TERM_4_DTO,
        fixture.TEST_TERM_5_DTO
      ].map(term => ({
        values: {
          code: term.getCode(),
          label: term.getLabel(),
          description: term.getDescription(),
          official: String(term.isOfficial())
        },
        selected: false
      })),
      paging: {
        pageSize: {
          value: 5
        },
        range: '1-5 of 5'
      }
    },
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
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
