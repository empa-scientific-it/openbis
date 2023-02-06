import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    display: 'flex'
  },
  input: {
    display: 'none'
  },
  fileName: {
    flex: '1 1 auto',
    paddingRight: theme.spacing(1),
    cursor: 'pointer'
  },
  button: {
    marginTop: theme.spacing(1),
    whiteSpace: 'nowrap'
  }
})

class FileChooserField extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.inputReference = React.createRef()
  }

  componentDidUpdate(prevProps) {
    if (this.props.value !== prevProps.value) {
      if (this.inputReference.current) {
        this.inputReference.current.value = null
      }
    }
  }

  handleClick() {
    this.inputReference.current.click()
  }

  async handleChange(event) {
    const { name, onChange, onBlur } = this.props

    var file = event.target.files.length > 0 ? event.target.files[0] : null

    const newEvent = {
      ...event,
      target: {
        ...event.target,
        name: name,
        value: file
      }
    }

    if (onChange) {
      await onChange(newEvent)
    }

    if (onBlur) {
      await onBlur(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'FileChooserField.render')

    const {
      reference,
      id,
      label,
      value,
      description,
      mandatory,
      error,
      styles,
      classes
    } = this.props

    return (
      <FormFieldContainer
        description={description}
        error={error}
        styles={styles}
      >
        <div className={classes.container}>
          <input
            ref={this.inputReference}
            type='file'
            onChange={this.handleChange}
            className={classes.input}
          />
          <div className={classes.fileName}>
            <TextField
              id={id}
              label={label}
              value={
                value !== null && value !== undefined
                  ? value.name
                  : messages.get(messages.NO_FILE_CHOSEN)
              }
              mandatory={mandatory}
              onClick={this.handleClick}
              disabled={true}
              styles={styles}
            />
          </div>
          <div className={classes.button}>
            <Button
              reference={reference}
              label={messages.get(messages.CHOOSE_FILE)}
              onClick={this.handleClick}
            />
          </div>
        </div>
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(FileChooserField)
