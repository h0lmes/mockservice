<template>
    <div class="monospace">

        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-primary" @click="$fetch()">Fetch (Ctrl + Enter)</button>
            <button type="button" class="btn btn-default" @click="storeRequest">Save request (F5)</button>
            <button type="button" class="btn btn-default" @click="forgetRequest">Forget request</button>
        </div>
        <AutoSizeTextArea class="mb-4"
                          ref="requestText"
                          :min-rows="3"
                          :placeholder="requestPlaceholder"
                          @keydown.ctrl.enter.exact="$fetch()"
                          @keydown.116.exact.prevent="storeRequest"
                          v-model="requestValue"
        ></AutoSizeTextArea>

        <div v-show="responseValue" class="component-toolbar mb-4">
            <button type="button" class="btn btn-default" @click="copyToClipboard(responseValue)">
                COPY TO CLIPBOARD
                <span v-show="copied">&#9989;</span>
            </button>
            <button type="button" class="btn btn-default" @click="saveResponse">SAVE RESPONSE TO FILE</button>
        </div>
        <pre v-show="responseValue" class="form-control form-control-sm monospace min-height">{{ responseValue }}</pre>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import Loading from "../components/other/Loading";
    import AutoSizeTextArea from "../components/other/AutoSizeTextArea";
    import copy from "../js/clipboard";
    import {handleError} from "@/js/common";

    const storageKey = 'MockServiceRequest';

    export default {
        name: "request",
        components: {AutoSizeTextArea, Loading},
        data() {
            return {
                requestPlaceholder: 'POST http://localhost:8081/api/v2/entity\r\n\r\n{"name": "Johnny 5"}',
                requestValue: '',
                responseValue: '',
                copied: false,
            }
        },
        mounted() {
            this.restoreRequest();
        },
        beforeRouteLeave(to, from , next) {
            if (window.localStorage) {
                const storedValue = window.localStorage.getItem(storageKey) || '';
                if (this.requestValue !== storedValue) {
                    if (!window.confirm('You have unsaved request.\nLeave page anyway?')) {
                        next(false);
                        return;
                    }
                }
            }
            next();
        },
        async fetch() {
            this.copied = false;

            if (!this.requestValue) return;

            const selStart = this.$refs.requestText.selectionStart();
            const selEnd = this.$refs.requestText.selectionEnd();
            let lines;
            if (selStart === selEnd) {
                lines = this.requestValue.split('\n');
            } else {
                lines = this.requestValue.substring(selStart, selEnd).split('\n');
            }
            const spaceIndex = lines[0].indexOf(' ');
            const method = spaceIndex > -1 ? lines[0].substring(0, spaceIndex).toUpperCase() : 'GET';
            const url = spaceIndex > -1 ? lines[0].substring(spaceIndex + 1) : lines[0];

            const headers = {};
            let i = 1;
            while (i < lines.length && !!lines[i]) {
                const index = lines[i].indexOf(':');
                const headerName = lines[i].substring(0, index);
                headers[headerName] = lines[i].substring(index + 1);
                i++;
            }

            let body = '';
            while (++i < lines.length) {
                body += lines[i] + '\n';
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
            copyToClipboard(text) {
                copy(text).then(
                    () => this.copied = true
                ).catch(
                    console.error
                );
            },
        }
    }
</script>
<style scoped>
    .min-height {
        min-height: 2rem;
    }
</style>
