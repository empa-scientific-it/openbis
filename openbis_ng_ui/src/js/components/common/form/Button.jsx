import React from 'react'
import Button from '@material-ui/core/Button'
import { withStyles } from '@material-ui/core/styles'

const styles = theme => ({
  risky: {
    backgroundColor: theme.palette.error.main,
    color: theme.palette.error.contrastText,
    '&:hover': {
      backgroundColor: theme.palette.error.dark
    },
    '&:disabled': {
      backgroundColor: theme.palette.error.light
    }
  }
})

class FormButton extends React.Component {
  static defaultProps = {
    variant: 'contained'
  }

  render() {
    const {
      reference,
      id,
      name,
      label,
      color,
      type,
      variant,
      disabled,
      href,
      styles,
      classes,
      onClick
    } = this.props

    let theColor = null
    let theClasses = { ...styles }

    if (color) {
      theColor = color
    } else {
      if (type === 'final') {
        theColor = 'primary'
      } else if (type === 'risky') {
        theColor = 'secondary'
        theClasses = {
          ...styles,
          root: `${styles.root} ${classes.risky}`
        }
      } else {
        theColor = 'secondary'
      }
    }

    return (
      <Button
        ref={reference}
        id={id}
        name={name}
        classes={theClasses}
        variant={variant}
        color={theColor}
        href={href}
        onClick={onClick}
        disabled={disabled}
        size='small'
      >
        {label}
      </Button>
    )
  }
}

export default withStyles(styles)(FormButton)
