<template>
    <textarea class="form-control form-control-sm v-resize monospace"
              :rows="rows"
              :value="value"
              @input="e => input(e)"
    ></textarea>
</template>
<script>
    export default {
        name: "AutoSizeTextArea",
        data() {
            return {}
        },
        props: {
            value: {type: String},
            maxRows: {type: Number, default: 20},
        },
        computed: {
            rows() {
                let size = 2;
                if (!!this.value) {
                    const lines = this.value.split('\n');
                    if (lines.length > size) {
                        size = Math.min(lines.length, this.maxRows);
                    }
                }
                return size;
            },
        },
        methods: {
            input(e) {
                this.$emit('input', e.target.value);
            },
        },
    }
</script>
<style scoped>
</style>