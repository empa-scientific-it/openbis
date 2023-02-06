import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Radio from '@material-ui/core/Radio'
import Typography from '@material-ui/core/Typography'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {},
  radioContainer: {
    display: 'flex',
    padding: `${theme.spacing(1) / 2}px 0px`,
    '&:first-child': {
      paddingTop: 0
    },
    '&:last-child': {
      paddingBottom: 0
    }
  },
  radio: {
    padding: '2px',
    marginRight: '4px'
  },
  label: {
    cursor: 'pointer'
  },
  labelDisabled: {
    cursor: 'inherit'
  }
})

class RadioGroupField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit'
  }

  constructor(props) {
    super(props)

    const references = {}
    if (props.options) {
      props.options.forEach(option => {
        if (option.value) {
          references[option.value] = React.createRef()
        }
      })
    }

    this.references = references
    this.actions = {}
    this.handleLabelClick = this.handleLabelClick.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
  }

  handleLabelClick(value) {
    const reference = this.getReference(value)
    if (reference && reference.current) {
      reference.current.click()
    }
  }

  handleChange(event) {
    this.handleEvent(event, this.props.onChange)
  }

  handleFocus(event) {
    this.handleEvent(event, this.props.onFocus)
    const action = this.getAction(event.target.value)
    if (action) {
      action.focusVisible()
    }
  }

  handleEvent(event, handler) {
    const { name } = this.props

    if (handler) {
      const newEvent = {
        ...event,
        target: {
          ...event.target,
          name: name,
          value: event.target.value
        }
      }
      delete newEvent.target.checked
      handler(newEvent)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'RadioGroupField.render')

    const {
      description,
      options,
      error,
      metadata,
      mode,
      styles,
      classes,
      onClick
    } = this.props

    if (mode !== 'view' && mode !== 'edit') {
      throw 'Unsupported mode: ' + mode
    }

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <div className={classes.container}>
          {options.map(option => this.renderOption(option))}
        </div>
      </FormFieldContainer>
    )
  }

  renderOption(option) {
    const { value, disabled, mode, styles, classes, onClick } = this.props

    const isDisabled = disabled || mode !== 'edit'

    return (
      <div key={option.value} className={classes.radioContainer}>
        <Radio
          inputRef={this.getReference(option.value)}
          action={action => (this.actions[option.value] = action)}
          value={option.value}
          checked={option.value === value}
          disabled={isDisabled}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onClick={onClick}
          classes={{ root: classes.radio }}
          size='small'
        />
        <Typography
          component='label'
          className={isDisabled ? classes.labelDisabled : classes.label}
          onClick={() => this.handleLabelClick(option.value)}
        >
          <FormFieldLabel
            label={option.label}
            styles={styles}
            onClick={onClick}
          />
        </Typography>
      </div>
    )
  }

  getReference(value) {
    return this.references[value]
  }
  getAction(value) {
    return this.actions[value]
  }
}

export default withStyles(styles)(RadioGroupField)
