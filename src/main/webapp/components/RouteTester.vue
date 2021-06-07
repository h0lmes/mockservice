<template>
    <div>
        <div class="wrapper">
            <pre class="form-control form-control-sm monospace">{{ testResult }}</pre>
        </div>
        <div class="buttons">
            <div class="btn btn-sm btn-primary" @click="test" v-cloak="inProgress">RETRY</div>
            <div class="btn btn-sm btn-default" @click="$emit('close')">CLOSE</div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "RouteTester",
        data() {
            return {
                testResult: '',
                inProgress: false
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
                        'Mock-Alt': this.encodedPath + '/' + this.route.alt
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
                this.inProgress = true;
                this.testResult = '';
                this.println(this.route.method.toUpperCase() + ' ' + this.host + this.route.path);
                this.println(JSON.stringify(this.testHeaders));
                this.println('query running ...');
                this.$nextTick();

                try {
                    const startTime = new Date();
                    const response = await fetch(this.host + this.route.path, {
                        method: this.route.method,
                        headers: this.testHeaders
                    });
                    const body = await response.text();
                    const elapsed = new Date() - startTime;
                    this.println('');
                    this.println('--- response in ' + elapsed + ' ms with status ' + response.status + ' ---');
                    this.println(body);
                    this.println('');
                    this.println('--- headers ---');
                    for (let header of response.headers) {
                        this.println(this.headerToString(header));
                    }
                } catch (err) {
                    this.println('------');
                    this.println(err);
                }
            },
            println(text) {
                this.testResult = this.testResult + text + '\n';
            },
            headerToString(header) {
              if (Array.isArray(header)) {
                  return header[0] + ': ' + header[1];
              } else {
                  return header;
              }
            },
        }
    }
</script>
<style lang="scss" scoped>
    .wrapper {
        padding: 0 0 0.7rem;
        text-align: initial;
    }
    .buttons {
        padding: 0;
        text-align: center;
    }
</style>