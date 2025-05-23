<template>
    <div>
        <div class="tester-buttons">
            <button type="button" class="btn btn-sm btn-primary" @click="testExecute">run test</button>
            <button type="button" class="btn btn-sm btn-default" @click="testStop">stop</button>
            <button type="button" class="btn btn-sm btn-default" @click="testFetchResult">get log</button>
            <button type="button" class="btn btn-sm btn-danger" @click="testClear">clear log</button>
            <button type="button" class="btn btn-sm btn-default" @click="copyToClipboard()">
                copy log
                <span v-show="copied">&#9989;</span>
            </button>
            <ButtonEdit @click="$emit('edit')"></ButtonEdit>
            <button type="button" class="btn btn-sm btn-default" @click="$emit('close')">close</button>
        </div>
        <div class="tester-result">
            <pre class="form-control form-control-sm monospace"
                 v-html="resultProcessed"></pre>
        </div>

        <Loading v-if="loading"></Loading>
    </div>
</template>
<script>
import {mapActions} from "vuex";
import copy from "../../js/clipboard";
import ButtonEdit from "@/components/other/ButtonEdit";
import Loading from "@/components/other/Loading";

export default {
    name: "TestRun",
    components: {ButtonEdit, Loading},
    data() {
        return {
            loading: false,
            result: '...',
            copied: false,
            ws: null,
        }
    },
    props: {
        test: {type: Object, default: {}}
    },
    mounted() {
        this.testFetchResult();
        this.startWebSocket();
    },
    beforeDestroy() {
        if (this.ws == null) return;
        this.ws.close();
        this.ws = null;
    },
    computed: {
        wsUrl() {
            return 'ws://' + location.hostname + ':8081/__wsapi__'
        },
        resultProcessed() {
            return this.result
                .replaceAll('SUCCESS', '<span class="green">SUCCESS</span>')
                .replaceAll('WARNING', '<span class="orange-yellow">WARNING</span>')
                .replaceAll('FAILED', '<span class="red">FAILED</span>');
        },
    },
    methods: {
        ...mapActions({
            executeTest: 'tests/execute',
            stopTest: 'tests/stop',
            fetchResult: 'tests/result',
            clearTest: 'tests/clear',
        }),
        async testFetchResult() {
            this.loading = true;
            this.fetchResult(this.test.alias)
                .then(result => {
                    this.loading = false;
                    this.clearOutput();
                    this.println(result);
                })
                .catch(error => {
                    this.loading = false;
                    this.println('----------');
                    this.println(error);
                });
        },
        async testExecute() {
            if (this.ws == null) this.startWebSocket();
            try {
                await this.executeTest(this.test.alias);
            } catch (err) {
                this.println('----------');
                this.println(err);
            }
        },
        async testStop() {
            try {
                await this.stopTest(this.test.alias);
            } catch (err) {
                this.println('----------');
                this.println(err);
            }
        },
        async testClear() {
            try {
                await this.clearTest(this.test.alias)
                await this.testFetchResult();
            } catch (err) {
                this.println('----------');
                this.println(err);
            }
        },
        println(text) {
            this.result = this.result + text + '\n';
            this.copied = false;
        },
        clearOutput() {
            this.result = '';
            this.copied = false;
        },
        copyToClipboard() {
            copy(this.result).then(
                () => this.copied = true
            ).catch(
                console.error
            );
        },
        send(command, id, data) {
            if (this.ws == null) return;
            this.ws.send({command, id, data});
        },
        startWebSocket() {
            console.log("Starting WebSocket...")
            this.ws = new WebSocket(this.wsUrl)
            this.ws.onmessage = this.onWsMessage;
            this.ws.onopen = this.onWsOpen;
            this.ws.onclose = this.onWsClose;
            this.ws.onerror = this.onWsError;
        },
        onWsMessage(event) {
            console.log("WebSocket::test_run::message")
            const data = JSON.parse(event.data);
            if (data != null && data.event === 'TEST_RESULT' && data.id == this.test.alias) {
                this.clearOutput()
                this.println(data.data)
            }
        },
        onWsOpen() {
            console.log("WebSocket::test_run::open")
        },
        onWsClose() {
            console.log("WebSocket::test_run::closed")
            this.ws = null;
        },
        onWsError(event) {
            console.error("WebSocket::test_run::error: ", event)
        },
    }
}
</script>
<style lang="scss" scoped>
.tester-result {
    padding: 0;
    text-align: initial;
}
.tester-buttons {
    padding: 0.3rem 0 0.7rem;
    text-align: end;
}
</style>
