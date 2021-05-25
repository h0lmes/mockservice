<template>
    <div class="wrapper">
        <textarea class="form-control form-control-sm v-resize" rows="10" v-model="testResult"></textarea>
        <div class="buttons-wrapper">
            <div class="btn btn-sm btn-primary mr-3" @click="test">RETRY</div>
            <div class="btn btn-sm btn-default" @click="$emit('close')">CLOSE</div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "RouteTester",
        data() {
            return {
                testResult: ''
            }
        },
        props: {
            route: {
                type: Object,
                default: {}
            }
        },
        mounted: function() {
            this.test();
        },
        computed: {
            host() {
                return this.$store.state.host;
            },
            encodedPath() {
                let path = this.route.path;
                if (path.startsWith('/')) path = path.substring(1);
                return path.replaceAll('/', '-');
            },
            contentType() {
                if (this.route.type === 'SOAP') return 'text/xml';
                return 'application/json';
            },
            testHeaders() {
                if (this.route.suffix) {
                    return {
                        'Content-Type': this.contentType,
                        'Mock-Suffix': this.encodedPath + '/' + this.route.suffix
                    }
                } else {
                    return {
                        'Content-Type': this.contentType
                    }
                }
            },
        },
        methods: {
            async test() {
                this.testResult = '';
                this.testPrintln(this.route.method.toUpperCase() + ' ' + this.host + this.route.path);
                this.testPrintln(JSON.stringify(this.testHeaders));
                this.testPrintln('--- response ---');
                this.$nextTick();

                try {
                    const response = await fetch(this.host + this.route.path, {
                        method: this.route.method,
                        headers: this.testHeaders
                    });
                    for (let header of response.headers) {
                        this.testPrintln(header);
                    }
                    this.testPrintln('--- body ---');
                    this.testPrintln(await response.text());
                    this.testPrintln('--- status code ---');
                    this.testPrintln(response.status);
                } catch (err) {
                    this.testPrintln(err);
                }
            },
            testPrintln(text) {
                this.testResult = this.testResult + text + '\n';
            },
        }
    }
</script>
<style scoped>
    .wrapper {
        display: block;
        box-sizing: border-box;
        margin: 0;
        padding: 0 .5rem;
        width: 100%;
    }
    .buttons-wrapper {
        cursor: default;
        display: block;
        box-sizing: border-box;
        margin: 0;
        padding: 1.3rem 0 1.2rem;
        width: 100%;
        text-align: center;
    }
</style>