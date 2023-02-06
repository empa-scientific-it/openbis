export default class AppController {
  constructor() {
    this.isSystemUser = jest.fn()
    this.objectNew = jest.fn()
    this.objectOpen = jest.fn()
    this.objectChange = jest.fn()
    this.objectUpdate = jest.fn()
    this.objectDelete = jest.fn()
  }
}
