import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
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
    paddingRight: theme.spacing(1)
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
    this.state = {
      fileName: null
    }
  }

  handleClick() {
    this.inputReference.current.click()
  }

  async handleChange(event) {
    const { reference, name, onChange, onBlur } = this.props

    var file = event.target.files.length > 0 ? event.target.files[0] : null

    this.setState({
      fileName: file ? file.name : null
    })

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
      description,
      mandatory,
      error,
      styles,
      classes
    } = this.props
    const { fileName } = this.state

    return (
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
            description={description}
            value={
              fileName !== null && fileName !== undefined
                ? fileName
                : messages.get(messages.NO_FILE_CHOSEN)
            }
            mandatory={mandatory}
            disabled={true}
            error={error}
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
    )
  }
}

export default withStyles(styles)(FileChooserField)
