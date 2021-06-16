<template>
    <div id="config-page-el" class="monospace">

        <p class="red">Use with caution!</p>
        <p class="red mb-4">It is easy to ruin config by editing it as plain text.</p>
        <textarea id="ta-config-el" class="form-control form-control-sm v-resize" :rows="rows" v-model="config"></textarea>
        <div id="buttons-el" class="mt-4 pl-1">
            <button type="button" class="btn btn-sm btn-primary mr-3" @click="save">SAVE AND APPLY</button>
            <button type="button" class="btn btn-sm btn-default mr-3" @click="download">DOWNLOAD</button>
        </div>

        <textarea id="hta1" class="form-control form-control-sm invisible" :rows="1"></textarea>
        <textarea id="hta2" class="form-control form-control-sm invisible" :rows="2"></textarea>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";

    export default {
        name: "config",
        components: {Loading},
        data() {
            return {
                config: '',
                oneRowHeight: 16,
                textareaOffsetTop: 0,
                resizeTimeoutId: null,
                rows: 16
            }
        },
        mounted() {
            this.oneRowHeight = document.getElementById('hta2').clientHeight - document.getElementById('hta1').clientHeight;
            this.textareaOffsetTop = document.getElementById('ta-config-el').offsetTop;
            window.addEventListener("resize", this.onResize);
            this.$nextTick(function () {
                this.onResize();
            });
        },
        beforeDestroy() {
            window.removeEventListener("resize", this.onResize);
        },
        async fetch() {
            return this.fetchConfig()
                .then(response => this.config = response);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions(['fetchConfig', 'saveConfig']),
            async save() {
                if (confirm('Ye be warned =)')) {
                    this.$nuxt.$loading.start();
                    this.saveConfig(this.config)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            download() {
                this.saveTextAsFile(this.config, 'config.yml')
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
            onResize() {
                clearTimeout(this.resizeTimeoutId);
                this.resizeTimeoutId = setTimeout(() => {
                    this.rows = (
                        window.innerHeight
                        - this.textareaOffsetTop
                        - 90 /*(document.getElementById('config-page-el').clientHeight
                            - this.textareaOffsetTop
                            - document.getElementById('ta-config-el').offsetHeight)*/
                    ) / this.oneRowHeight;
                }, 500);
            },
        }
    }
</script>
<style scoped>
    .invisible {
        position: fixed;
        top: -10rem;
        width: 10rem;
    }
</style>