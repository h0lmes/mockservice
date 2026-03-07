const saveBlobAsFile = (blob: Blob, fileName: string) => {
  const link = document.createElement('a')
  link.download = fileName
  link.href = URL.createObjectURL(blob)
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  link.remove()
}

export const saveTextAsFile = (text: string, fileName: string, type = 'text/plain') => {
  saveBlobAsFile(new Blob([text], { type }), fileName)
}
