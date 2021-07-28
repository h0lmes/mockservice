export default function(text) {

    return new Promise((resolve, reject) => {

        if (!navigator.clipboard) {
            fallbackCopyToClipboard(text, resolve, reject);
            return;
        }

        navigator.clipboard.writeText(text).then(
            () => resolve()
        ).catch(
            () => reject('Async copy to clipboard failed')
        );

        function fallbackCopyToClipboard(text, resolve, reject) {
            const textArea = document.createElement("textarea");
            textArea.value = text;
            textArea.style.top = "0";
            textArea.style.left = "0";
            textArea.style.position = "fixed";
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();

            try {
                const successful = document.execCommand('copy');
                if (successful) {
                    resolve();
                } else {
                    reject('Fallback copy to clipboard failed');
                }
            } catch (err) {
                reject('Fallback copy to clipboard failed' + err);
            }

            document.body.removeChild(textArea);
        }
    });
}