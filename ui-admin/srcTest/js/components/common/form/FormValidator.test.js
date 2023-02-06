import FormValidator from '@src/js/components/common/form/FormValidator.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

describe('FormValidator', () => {
  test('validateInBasicMode', testValidateInBasicMode)
  test('validateInFullMode', testValidateInFullMode)
  test('returnOriginalObjectWhenNoErrors', testReturnOriginalObjectWhenNoErrors)
  test('returnNewObjectWhenErrors', testReturnNewObjectWhenErrors)
  test('returnOriginalArrayWhenNoErrors', testReturnOriginalArrayWhenNoErrors)
  test('returnNewArrayWhenErrors', testReturnNewArrayWhenErrors)
})

function testValidateInBasicMode() {
  const validator = new FormValidator(FormValidator.MODE_BASIC)

  const object = {
    emptyCode: FormUtil.createField({ value: null }),
    incorrectCode: FormUtil.createField({ value: 'I am incorrect' }),
    correctCode: FormUtil.createField({ value: 'Correct_Code-123' }),
    emptyTermCode: FormUtil.createField({ value: null }),
    incorrectTermCode: FormUtil.createField({ value: 'I am incorrect' }),
    correctTermCode: FormUtil.createField({ value: 'Correct_Code-123:1' })
  }

  validator.validateNotEmpty(object, 'emptyCode', 'Empty Code')
  validator.validateCode(object, 'emptyCode', 'Empty Code')

  validator.validateNotEmpty(object, 'incorrectCode', 'Incorrect Code')
  validator.validateCode(object, 'incorrectCode', 'Incorrect Code')

  validator.validateNotEmpty(object, 'correctCode', 'Correct Code')
  validator.validateCode(object, 'correctCode', 'Correct Code')

  validator.validateNotEmpty(object, 'emptyTermCode', 'Empty Term Code')
  validator.validateTermCode(object, 'emptyTermCode', 'Empty Term Code')

  validator.validateNotEmpty(object, 'incorrectTermCode', 'Incorrect Code')
  validator.validateTermCode(object, 'incorrectTermCode', 'Incorrect Term Code')

  validator.validateNotEmpty(object, 'correctTermCode', 'Correct Term Code')
  validator.validateTermCode(object, 'correctTermCode', 'Correct Term Code')

  expect(validator.getErrors()).toEqual([
    {
      name: 'incorrectCode',
      error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
      object: object
    },
    {
      name: 'incorrectTermCode',
      error:
        'Incorrect Term Code can only contain A-Z, a-z, 0-9 and _, -, ., :',
      object: object
    }
  ])
}

function testValidateInFullMode() {
  const validator = new FormValidator(FormValidator.MODE_FULL)

  const object = {
    emptyCode: FormUtil.createField({ value: null }),
    incorrectCode: FormUtil.createField({ value: 'I am incorrect' }),
    correctCode: FormUtil.createField({ value: 'Correct_Code-123' }),
    emptyTermCode: FormUtil.createField({ value: null }),
    incorrectTermCode: FormUtil.createField({ value: 'I am incorrect' }),
    correctTermCode: FormUtil.createField({ value: 'Correct_Code-123:1' })
  }

  validator.validateNotEmpty(object, 'emptyCode', 'Empty Code')
  validator.validateCode(object, 'emptyCode', 'Empty Code')

  validator.validateNotEmpty(object, 'incorrectCode', 'Incorrect Code')
  validator.validateCode(object, 'incorrectCode', 'Incorrect Code')

  validator.validateNotEmpty(object, 'correctCode', 'Correct Code')
  validator.validateCode(object, 'correctCode', 'Correct Code')

  validator.validateNotEmpty(object, 'emptyTermCode', 'Empty Term Code')
  validator.validateTermCode(object, 'emptyTermCode', 'Empty Term Code')

  validator.validateNotEmpty(object, 'incorrectTermCode', 'Incorrect Code')
  validator.validateTermCode(object, 'incorrectTermCode', 'Incorrect Term Code')

  validator.validateNotEmpty(object, 'correctTermCode', 'Correct Term Code')
  validator.validateTermCode(object, 'correctTermCode', 'Correct Term Code')

  expect(validator.getErrors()).toEqual([
    {
      name: 'emptyCode',
      error: 'Empty Code cannot be empty',
      object: object
    },
    {
      name: 'incorrectCode',
      error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
      object: object
    },
    {
      name: 'emptyTermCode',
      error: 'Empty Term Code cannot be empty',
      object: object
    },
    {
      name: 'incorrectTermCode',
      error:
        'Incorrect Term Code can only contain A-Z, a-z, 0-9 and _, -, ., :',
      object: object
    }
  ])
}

function testReturnOriginalObjectWhenNoErrors() {
  const validator = new FormValidator(FormValidator.MODE_FULL)

  const object = {
    errors: 0,
    correctCode: FormUtil.createField({ value: 'Correct_Code-123' }),
    nonEmptyField: FormUtil.createField({ value: 'I am not empty' })
  }

  validator.validateNotEmpty(object, 'correctCode', 'Correct Code')
  validator.validateCode(object, 'correctCode', 'Correct Code')
  validator.validateNotEmpty(object, 'nonEmptyField', 'Non Empty Field')

  expect(validator.getErrors()).toEqual([])
  expect(validator.withErrors(object)).toEqual({
    errors: 0,
    correctCode: {
      enabled: true,
      value: 'Correct_Code-123',
      visible: true
    },
    nonEmptyField: {
      enabled: true,
      value: 'I am not empty',
      visible: true
    }
  })
  expect(validator.withErrors(object)).toBe(object)
}

function testReturnNewObjectWhenErrors() {
  const validator = new FormValidator(FormValidator.MODE_FULL)

  const object = {
    errors: 0,
    incorrectCode: FormUtil.createField({ value: 'I am incorrect' }),
    nonEmptyField: FormUtil.createField({ value: 'I am not empty' }),
    other1: 'ignore me',
    other2: {
      value: 'ignore me'
    }
  }

  validator.validateNotEmpty(object, 'incorrectCode', 'Incorrect Code')
  validator.validateCode(object, 'incorrectCode', 'Incorrect Code')
  validator.validateNotEmpty(object, 'nonEmptyField', 'Non Empty Field')

  expect(validator.getErrors()).toEqual([
    {
      name: 'incorrectCode',
      error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
      object: object
    }
  ])

  expect(validator.withErrors(object)).toEqual({
    errors: 1,
    incorrectCode: {
      enabled: true,
      error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
      value: 'I am incorrect',
      visible: true
    },
    nonEmptyField: {
      enabled: true,
      value: 'I am not empty',
      visible: true
    },
    other1: 'ignore me',
    other2: {
      value: 'ignore me'
    }
  })

  expect(validator.withErrors(object)).not.toBe(object)
  expect(validator.withErrors(object).incorrectCode).not.toBe(
    object.incorrectCode
  )

  expect(validator.withErrors(object).nonEmptyField).toBe(object.nonEmptyField)
  expect(validator.withErrors(object).other1).toBe(object.other1)
  expect(validator.withErrors(object).other2).toBe(object.other2)
}

function testReturnOriginalArrayWhenNoErrors() {
  const validator = new FormValidator(FormValidator.MODE_FULL)

  const object1 = {
    errors: 0,
    correctCode: FormUtil.createField({ value: 'Correct_Code-123' })
  }
  const object2 = {
    errors: 0,
    nonEmptyField: FormUtil.createField({ value: 'I am not empty' })
  }

  const array = [object1, object2]

  validator.validateCode(object1, 'correctCode', 'Correct Code')
  validator.validateNotEmpty(object2, 'nonEmptyField', 'Non Empty Code')

  expect(validator.getErrors()).toEqual([])
  expect(validator.getErrors(object1)).toEqual([])
  expect(validator.getErrors(object2)).toEqual([])

  expect(validator.withErrors(array)).toBe(array)
  expect(validator.withErrors(array)).toEqual([
    {
      errors: 0,
      correctCode: {
        enabled: true,
        value: 'Correct_Code-123',
        visible: true
      }
    },
    {
      errors: 0,
      nonEmptyField: {
        enabled: true,
        value: 'I am not empty',
        visible: true
      }
    }
  ])
}

function testReturnNewArrayWhenErrors() {
  const validator = new FormValidator(FormValidator.MODE_FULL)

  const object1 = {
    errors: 0,
    incorrectCode: FormUtil.createField({ value: 'I am incorrect' })
  }
  const object2 = {
    errors: 0,
    emptyField: FormUtil.createField({})
  }
  const object3 = {
    errors: 0,
    nonEmptyField: FormUtil.createField({ value: 'I am not empty' })
  }

  const array = [object1, object2, object3]

  validator.validateCode(object1, 'incorrectCode', 'Incorrect Code')
  validator.validateNotEmpty(object2, 'emptyField', 'Empty Field')
  validator.validateNotEmpty(object3, 'nonEmptyField', 'Non Empty Field')

  expect(validator.getErrors()).toEqual([
    {
      name: 'incorrectCode',
      error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
      object: object1
    },
    {
      name: 'emptyField',
      error: 'Empty Field cannot be empty',
      object: object2
    }
  ])

  expect(validator.getErrors(object1)).toEqual([
    {
      name: 'incorrectCode',
      error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
      object: object1
    }
  ])
  expect(validator.getErrors(object2)).toEqual([
    {
      name: 'emptyField',
      error: 'Empty Field cannot be empty',
      object: object2
    }
  ])
  expect(validator.getErrors(object3)).toEqual([])

  expect(validator.withErrors(array)).not.toBe(array)
  expect(validator.withErrors(array)[0]).not.toBe(array[0])
  expect(validator.withErrors(array)[1]).not.toBe(array[1])
  expect(validator.withErrors(array)[2]).toBe(array[2])

  expect(validator.withErrors(array)).toEqual([
    {
      errors: 1,
      incorrectCode: {
        enabled: true,
        error: 'Incorrect Code can only contain A-Z, a-z, 0-9 and _, -, .',
        value: 'I am incorrect',
        visible: true
      }
    },
    {
      errors: 1,
      emptyField: {
        enabled: true,
        error: 'Empty Field cannot be empty',
        value: null,
        visible: true
      }
    },
    {
      errors: 0,
      nonEmptyField: {
        enabled: true,
        value: 'I am not empty',
        visible: true
      }
    }
  ])
}
