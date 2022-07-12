import ActiveUserReportFormComponentTest from '@srcTest/js/components/tools/form/activeUserReport/ActiveUserReportFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new ActiveUserReportFormComponentTest()
  common.beforeEach()
})

describe(ActiveUserReportFormComponentTest.SUITE, () => {
  test('sendReport', testSendReport)
})

async function testSendReport() {
  const report = await common.mount({ activeUsersCount: "2"})

  report.expectJSON({
    title: "Active Users Report",
    button: {
      enabled: true,
      "label": "Send Report"
    },
    message: null
  })

  expect(common.context.getState()).toMatchObject({})

  const good = {"status": "OK"}
  common.facade.sendReport.mockReturnValue(Promise.resolve(good))
  await common.controller.sendReport()

  expect(common.context.getState()).toMatchObject({result: {success: true, output: good}})

  const bad = {"status": "error", "message": "bad output"}
  common.facade.sendReport.mockReturnValue(Promise.resolve(bad))
  await common.controller.sendReport()

  expect(common.context.getState()).toMatchObject({result: {success: false, output: bad}})
}
