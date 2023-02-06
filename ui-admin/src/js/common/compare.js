const collator = new Intl.Collator(undefined, {
  numeric: true,
  sensitivity: 'base'
})

export default function compare(v1, v2) {
  return collator.compare(v1 || '', v2 || '')
}
