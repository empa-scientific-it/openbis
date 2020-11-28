import ToolBrowserComponentTest from '@srcTest/js/components/tools/browser/ToolBrowserComponentTest.js'
import ToolBrowserTestData from '@srcTest/js/components/tools/browser/ToolBrowserTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new ToolBrowserComponentTest()
  common.beforeEach()
})

describe(ToolBrowserComponentTest.SUITE, () => {
  test('open/close', testOpenClose)
})

async function testOpenClose() {
  const {
    testDynamicPropertyJythonPlugin,
    testDynamicPropertyPredeployedPlugin,
    testManagedPropertyJythonPlugin,
    testEntityValidationJythonPlugin,
    testQuery
  } = ToolBrowserTestData

  openbis.mockSearchPlugins([
    testDynamicPropertyJythonPlugin,
    testDynamicPropertyPredeployedPlugin,
    testManagedPropertyJythonPlugin,
    testEntityValidationJythonPlugin
  ])
  openbis.mockSearchQueries([testQuery])

  const browser = await common.mount()

  browser.getNodes()[0].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 1, text: testDynamicPropertyJythonPlugin.name },
      { level: 1, text: testDynamicPropertyPredeployedPlugin.name },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 0, text: 'Queries' }
    ]
  })

  browser.getNodes()[3].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 1, text: testDynamicPropertyJythonPlugin.name },
      { level: 1, text: testDynamicPropertyPredeployedPlugin.name },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 1, text: testEntityValidationJythonPlugin.name },
      { level: 0, text: 'Queries' }
    ]
  })

  browser.getNodes()[5].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 1, text: testDynamicPropertyJythonPlugin.name },
      { level: 1, text: testDynamicPropertyPredeployedPlugin.name },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 1, text: testEntityValidationJythonPlugin.name },
      { level: 0, text: 'Queries' },
      { level: 1, text: testQuery.name }
    ]
  })

  browser.getNodes()[5].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 1, text: testDynamicPropertyJythonPlugin.name },
      { level: 1, text: testDynamicPropertyPredeployedPlugin.name },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 1, text: testEntityValidationJythonPlugin.name },
      { level: 0, text: 'Queries' }
    ]
  })

  browser.getNodes()[0].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 1, text: testEntityValidationJythonPlugin.name },
      { level: 0, text: 'Queries' }
    ]
  })

  browser.getNodes()[1].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 0, text: 'Queries' }
    ]
  })
}
