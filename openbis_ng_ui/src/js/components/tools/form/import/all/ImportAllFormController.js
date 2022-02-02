import autoBind from 'auto-bind'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class ImportAllFormController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.context = context
    this.object = context.getProps().object
  }

  load() {
    this.context.setState({
      fields: {
        updateMode: FormUtil.createField({})
      },
      loaded: true
    })
  }

  async handleChange(params) {
    await this.context.setState(oldState => {
      const { newObject } = FormUtil.changeObjectField(
        oldState.fields,
        params.field,
        params.value
      )
      return {
        ...oldState,
        fields: newObject
      }
    })
  }

  import() {}

  getFacade() {
    return this.facade
  }

  getContext() {
    return this.context
  }

  getObject() {
    return this.object
  }
}
