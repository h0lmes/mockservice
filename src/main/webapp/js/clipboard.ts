const fallbackCopyToClipboard = (text: string): Promise<void> => new Promise((resolve, reject) => {
  const textArea = document.createElement('textarea')
  textArea.value = text
  textArea.style.top = '0'
  textArea.style.left = '0'
  textArea.style.position = 'fixed'
  document.body.appendChild(textArea)
  textArea.focus()
  textArea.select()

  try {
    const successful = document.execCommand('copy')
    if (successful) {
      resolve()
    } else {
      reject(new Error('Fallback copy to clipboard failed'))
    }
  } catch (error) {
    reject(new Error('Fallback copy to clipboard failed: ' + String(error)))
  } finally {
    document.body.removeChild(textArea)
  }
})

const copyToClipboard = (text: string): Promise<void> => {
  if (!navigator.clipboard) {
    return fallbackCopyToClipboard(text)
  }

  return navigator.clipboard.writeText(text).catch(() => {
    throw new Error('Async copy to clipboard failed')
  })
}

export default copyToClipboard
