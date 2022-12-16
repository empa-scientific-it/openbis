export default class BrowserControllerReload {
  constructor(controller) {
    this.controller = controller
  }

  async doGetObservedModifications() {
    throw 'Method not implemented'
  }

  reload(fullObjectModifications) {
    const observedModifications = this.doGetObservedModifications()

    const getTimestamp = (modifications, type, operation) => {
      return (
        (modifications &&
          modifications[type] &&
          modifications[type][operation]) ||
        0
      )
    }

    const newObjectModifications = {}
    let reload = false

    Object.keys(observedModifications).forEach(observedType => {
      const observedOperations = observedModifications[observedType]

      newObjectModifications[observedType] = {}

      observedOperations.forEach(observedOperation => {
        const timestamp = getTimestamp(
          this.lastObjectModifications,
          observedType,
          observedOperation
        )
        const newTimestamp = getTimestamp(
          fullObjectModifications,
          observedType,
          observedOperation
        )

        newObjectModifications[observedType][observedOperation] = Math.max(
          timestamp,
          newTimestamp
        )

        if (newTimestamp > timestamp) {
          reload = true
        }
      })
    })

    this.lastObjectModifications = newObjectModifications

    if (reload) {
      this.controller.load()
    }
  }
}
