<template>
    <div class="monospace">

        <div class="toolbar mb-3">
            <button type="button" class="btn btn-sm btn-primary" @click="$fetch()">FETCH (CTRL + ENTER)</button>
            <button type="button" class="btn btn-sm btn-default" @click="storeRequest">REQUEST TO STORAGE (F5)</button>
            <button type="button" class="btn btn-sm btn-default" @click="restoreRequest">REQUEST FROM STORAGE (F9)</button>
            <button type="button" class="btn btn-sm btn-default" @click="forgetRequest">FORGET REQUEST</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveRequest">REQUEST TO FILE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveResponse">RESPONSE TO FILE</button>
        </div>
        <textarea id="request-text-el"
                  class="form-control form-control-sm v-resize mb-4"
                  :placeholder="requestPlaceholder"
                  :rows="10"
                  @keydown.ctrl.enter.exact="$fetch()"
                  @keydown.116.exact.prevent="storeRequest"
                  @keydown.120.exact.prevent="restoreRequest"
                  v-model="requestValue"
        ></textarea>
        <pre class="form-control form-control-sm monospace min-height">{{ responseValue }}</pre>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import Loading from "../components/Loading";

    const storageKey = 'MockServiceRequest';

    async function handleError(response) {
        if (!response.ok) {
            const err = await response.json();
            throw Error(err.statusText || err.error || err.message || err);
        }
        return response;
    }

    export default {
        name: "request",
        components: {Loading},
        data() {
            return {
                requestPlaceholder: 'POST http://localhost:8081/api/v2/entity\n\n{"name": "Johnny 5"}',
                requestValue: '',
                responseValue: '',
            }
        },
        mounted() {
            this.restoreRequest();
        },
        async fetch() {
            if (!this.requestValue) return;

            const requestEl = document.getElementById('request-text-el');
            let lines;
            if (requestEl.selectionStart === requestEl.selectionEnd) {
                lines = this.requestValue.split('\n');
            } else {
                lines = this.requestValue.substring(requestEl.selectionStart, requestEl.selectionEnd).split('\n');
            }
            const len = lines.length;
            const spaceIndex = lines[0].indexOf(' ');
            const method = spaceIndex > -1 ? lines[0].substring(0, spaceIndex).toUpperCase() : 'GET';
            const url = spaceIndex > -1 ? lines[0].substring(spaceIndex + 1) : lines[0];

            const headers = {};
            let i = 1;
            while (i < len && lines[i]) {
                // add header
                i++;
            }

            let body = '';
            while (++i < len) {
                body += lines[i];
            }

            let params = {method, headers};
            if (method !== 'GET') {
                params = {...params, body};
            }

            return fetch(url, params
            ).then(handleError
            ).then(response => response.text()
            ).then(response => this.responseValue = response
            ).catch(error => this.responseValue = error);
        },
        fetchDelay: 0,
        methods: {
            storeRequest() {
                if (window.localStorage) {
                    window.localStorage.setItem(storageKey, this.requestValue);
                    console.log('Request saved in browser storage');
                }
            },
            restoreRequest() {
                if (window.localStorage) {
                    this.requestValue = window.localStorage.getItem(storageKey) || '';
                    console.log('Request loaded from browser storage');
                }
            },
            forgetRequest() {
                this.requestValue = '';
                if (window.localStorage) {
                    window.localStorage.removeItem(storageKey);
                }
            },
            saveRequest() {
                this.saveTextAsFile(this.requestValue, 'request.http')
            },
            saveResponse() {
                this.saveTextAsFile(this.responseValue, 'response.http')
            },
            saveTextAsFile(text, fileName) {
                let blob = new Blob([text], {type: 'text/plain'});
                let link = document.createElement("a");
                link.download = fileName;
                link.innerHTML = "Download File";
                link.href = URL.createObjectURL(blob);
                link.style.display = "none";
                document.body.appendChild(link);
                link.click();
                link.remove();
            },
        }
    }
</script>
<style scoped>
    .min-height {
        min-height: 2rem;
    }
</style>