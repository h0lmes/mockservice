<template>
    <div id="log-page-el" class="monospace">

        <textarea id="log-text-area-el" class="form-control form-control-sm v-resize" :rows="rows" v-model="value"></textarea>
        <div id="log-buttons-el" class="mt-4 pl-1">
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
                value: '',
                oneRowHeight: 16,
                resizeTimeoutId: null,
                rows: 16
            }
        },
        mounted() {
            this.oneRowHeight = document.getElementById('hta2').clientHeight - document.getElementById('hta1').clientHeight;
            window.addEventListener("resize", this.onResize);
            this.$nextTick(function () {
                this.onResize();
            });
        },
        beforeDestroy() {
            window.removeEventListener("resize", this.onResize);
        },
        async fetch() {
            return this.fetchLog()
                .then(response => {
                    this.value = response;
                    this.$nextTick(function () {
                        const el = document.getElementById('log-text-area-el');
                        el.scrollTop = el.scrollHeight;
                    });
                });
        },
        fetchDelay: 0,
        methods: {
            ...mapActions(['fetchLog']),
            download() {
                this.saveTextAsFile(this.value, 'mockservice.log')
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
                        - document.getElementById('log-text-area-el').offsetTop
                        - 90
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