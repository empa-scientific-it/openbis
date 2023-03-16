import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('sort', testSort)
})

async function testSort() {
  const form = await common.mountNew()

  const labels = [
    'Term 1',
    'term 11',
    'Term 2',
    'TERM A',
    'term B',
    'Term A1',
    'tErM A11',
    'term A2'
  ]

  for (let i = 0; i < labels.length; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(labels[i])
    await form.update()
  }

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { name: 'code', sort: null },
        { name: 'label', sort: 'asc' },
        { name: 'description', sort: null },
        { name: 'official', sort: null },
        { name: 'registrator', sort: null },
        { name: 'registrationDate', sort: null }
      ],
      rows: [
        { values: { label: 'Term 1' } },
        { values: { label: 'Term 2' } },
        { values: { label: 'term 11' } },
        { values: { label: 'TERM A' } },
        { values: { label: 'Term A1' } },
        { values: { label: 'term A2' } },
        { values: { label: 'tErM A11' } },
        { values: { label: 'term B' } }
      ]
    }
  })

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { name: 'code', sort: null },
        { name: 'label', sort: 'desc' },
        { name: 'description', sort: null },
        { name: 'official', sort: null },
        { name: 'registrator', sort: null },
        { name: 'registrationDate', sort: null }
      ],
      rows: [
        { values: { label: 'term B' } },
        { values: { label: 'tErM A11' } },
        { values: { label: 'term A2' } },
        { values: { label: 'Term A1' } },
        { values: { label: 'TERM A' } },
        { values: { label: 'term 11' } },
        { values: { label: 'Term 2' } },
        { values: { label: 'Term 1' } }
      ]
    }
  })
}
