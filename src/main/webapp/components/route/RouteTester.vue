<template>
    <div>
        <div class="route-tester-buttons">
            <button type="button" class="btn btn-sm btn-primary" @click="test">retry</button>
            <ButtonEdit @click="$emit('edit')"></ButtonEdit>
            <button type="button" class="btn btn-sm btn-default" @click="$emit('close')">close</button>
        </div>
        <div class="route-tester-result">
            <pre class="form-control form-control-sm monospace">{{ testResult }}</pre>
        </div>
    </div>
</template>
<script>
import ButtonEdit from "@/components/other/ButtonEdit";

export default {
    name: "RouteTester",
    components: {ButtonEdit},
    data() {
        return {
            testResult: '',
        }
    },
    props: {
        route: {type: Object, default: {}}
    },
    mounted() {
        this.test();
    },
    computed: {
        host() {
            return this.$store.state.BASE_URL;
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
            if (this.route.alt) {
                return {
                    'Content-Type': this.contentType,
                    'Cache-Control': 'no-cache',
                    'Mock-Alt': this.encodedPath + '/' + this.route.alt
                }
            } else {
                return {
                    'Content-Type': this.contentType,
                    'Cache-Control': 'no-cache'
                }
            }
        },
    },
    methods: {
        async test() {
            this.testResult = '';
            this.println(this.route.method.toUpperCase() + ' ' + this.host + this.route.path);
            this.println(JSON.stringify(this.testHeaders));
            this.println('fetching ...');
            this.$nextTick();

            try {
                const startTime = new Date();
                const response = await fetch(
                    this.host + this.route.path,
                    {
                        method: this.route.method,
                        headers: this.testHeaders
                    }
                );
                const body = await response.text();
                const elapsed = new Date() - startTime;
                this.println('----- response in ' + elapsed + ' ms with status ' + response.status + ' -----');
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
.route-tester-result {
    padding: 0;
    text-align: initial;
}
.route-tester-buttons {
    padding: 0.3rem 0 0.7rem;
    text-align: end;
}
</style>
