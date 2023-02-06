import VocabularyTypeFormComponentTest from '@srcTest/js/components/types/form/vocabularytype/VocabularyTypeFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new VocabularyTypeFormComponentTest()
  common.beforeEach()
})

describe(VocabularyTypeFormComponentTest.SUITE, () => {
  test('page', testPage)
})

async function testPage() {
  const form = await common.mountNew()

  for (let i = 1; i <= 23; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(String(i))
    await form.update()
  }

  form.getGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.getGrid().getPaging().getPageSize().change(5)
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: '1' } },
        { values: { label: '2' } },
        { values: { label: '3' } },
        { values: { label: '4' } },
        { values: { label: '5' } }
      ],
      paging: {
        range: '1-5 of 23'
      }
    }
  })

  form.getGrid().getPaging().getNextPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: '6' } },
        { values: { label: '7' } },
        { values: { label: '8' } },
        { values: { label: '9' } },
        { values: { label: '10' } }
      ],
      paging: {
        range: '6-10 of 23'
      }
    }
  })

  form.getGrid().getPaging().getLastPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: '21' } },
        { values: { label: '22' } },
        { values: { label: '23' } }
      ],
      paging: {
        range: '21-23 of 23'
      }
    }
  })

  form.getGrid().getPaging().getPrevPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: '16' } },
        { values: { label: '17' } },
        { values: { label: '18' } },
        { values: { label: '19' } },
        { values: { label: '20' } }
      ],
      paging: {
        range: '16-20 of 23'
      }
    }
  })

  form.getGrid().getPaging().getFirstPage().click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { label: '1' } },
        { values: { label: '2' } },
        { values: { label: '3' } },
        { values: { label: '4' } },
        { values: { label: '5' } }
      ],
      paging: {
        range: '1-5 of 23'
      }
    }
  })
}
