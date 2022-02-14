export default class error {
  constructor(error) {
    this.error = error
  }

  getMessage() {
    if (this.error) {
      if (this.error.message) {
        return this.error.message
      } else {
        return this.error
      }
    } else {
      return null
    }
  }

  getStackTrace() {
    if (this.error && this.error.data && this.error.data.stackTrace) {
      return this.error.data.stackTrace
    } else {
      return null
    }
  }

  toString() {
    let string = ''
    if (this.getMessage() !== null) {
      string += this.getMessage() + '\n'
    }
    if (this.getStackTrace() !== null) {
      string += this.getStackTrace() + '\n'
    }
    return string
  }
}
