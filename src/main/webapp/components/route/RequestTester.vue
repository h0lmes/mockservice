<template>
    <div>
        <div class="tester-buttons">
            <button type="button" class="btn btn-sm btn-primary" @click="test">retry</button>
            <button type="button" class="btn btn-sm btn-default" @click="$emit('close')">close</button>
        </div>
        <div class="tester-result">
            <pre class="form-control form-control-sm monospace">{{ testResult }}</pre>
        </div>
    </div>
</template>
<script>
    import {mapActions} from "vuex";

    export default {
        name: "RequestTester",
        data() {
            return {
                testResult: '',
            }
        },
        props: {
            request: {type: Object, default: {}}
        },
        mounted() {
            this.test();
        },
        computed: {
        },
        methods: {
            ...mapActions({
                executeRequest: 'requests/execute',
            }),
            async test() {
                this.testResult = '';
                this.println('working ...');
                this.$nextTick();

                try {
                    const startTime = new Date();
                    const body = await this.executeRequest(this.request.id);
                    const elapsed = new Date() - startTime;
                    this.println('----- response in ' + elapsed + ' ms -----');
                    this.println(body);
                } catch (err) {
                    this.println('----------');
                    this.println(err);
                }
            },
            println(text) {
                this.testResult = this.testResult + text + '\n';
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
        text-align: center;
    }
</style>
