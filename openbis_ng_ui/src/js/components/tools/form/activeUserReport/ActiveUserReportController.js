import autoBind from 'auto-bind'
import ErrorObject from '@src/js/components/common/error/ErrorObject.js'

export default class ActiveUserReportController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.context = context
  }

  async sendReport() {
    try {
      await this.context.setState({
        loading: true
      })

      const output = await this.facade.sendReport()

      this.context.setState({
        result: {
          success: output.status == "OK",
          output: output
        }
      })
    } catch (error) {
      this.context.setState({
        result: {
          success: false,
          output: new ErrorObject(error).getMessage()
        }
      })
    } finally {
      await this.context.setState({
        loading: false
      })
    }
  }
}