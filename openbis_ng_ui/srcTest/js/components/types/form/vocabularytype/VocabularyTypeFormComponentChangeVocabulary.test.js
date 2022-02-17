import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('change vocabulary', testChangeVocabulary)
})

async function testChangeVocabulary() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary Type',
        description: {
          label: 'Description',
          value: fixture.TEST_VOCABULARY_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form
    .getParameters()
    .getVocabulary()
    .getDescription()
    .change('New Description')
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary Type',
        description: {
          label: 'Description',
          value: 'New Description',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
