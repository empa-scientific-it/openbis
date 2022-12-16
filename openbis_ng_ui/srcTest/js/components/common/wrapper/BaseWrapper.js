export default class BaseWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getStringValue(value) {
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      return value
    }
  }

  getNumberValue(value) {
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      let number = Number(value)
      if (Number.isNaN(number)) {
        return null
      } else {
        return number
      }
    }
  }

  getBooleanValue(value) {
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      return value === true
    }
  }

  findComponent(component, wrapper = this.wrapper) {
    return wrapper.find(this.unwrapComponent(component))
  }

  unwrapComponent(component) {
    // unwrap a component definition from any potential wrappers;
    // using a wrapped component definition e.g. in "find()" method
    // makes the tests operate on a wrapper "instance" (i.e. functional component)
    // which cannot be used to access React methods like "handleClick" or default props.
    const unwrapped = component.Naked
    return unwrapped ? unwrapped : component
  }

  async update() {
    // update the wrapper after all queued async callbacks are finished
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        try {
          this.wrapper.update()
          resolve()
        } catch (e) {
          reject(e)
        }
      }, 0)
    })
  }

  exists() {
    return this.wrapper.exists()
  }

  async expectJSONWaiting(json, timeout) {
    const end = Date.now() + timeout
    for (;;) {
      try {
        await this.update()
        expect(this.toJSON()).toMatchObject(json)
      } catch (error) {
        if (Date.now() < end) {
          await new Promise(resolve => {
            setTimeout(() => {
              resolve()
            }, 10)
          })
        } else {
          throw error
        }
      }
    }
  }

  expectJSON(json) {
    expect(this.toJSON()).toMatchObject(json)
  }
}
