<template>
    <textarea class="form-control form-control-sm no-resize monospace"
              ref="textArea"
              :value="value"
              :placeholder="placeholder"
              :rows="rows"
              :spellcheck="false"
              @input="e => input(e)"
              @keydown="e => $emit('keydown', e)"
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
            placeholder: {type: String},
            minRows: {type: Number, default: 1},
            maxRows: {type: Number, default: 20},
        },
        computed: {
            rows() {
                let size = this.minRows;
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
            selectionStart() {
                return this.$refs.textArea.selectionStart;
            },
            selectionEnd() {
                return this.$refs.textArea.selectionEnd;
            },
            focus() {
                this.$refs.textArea.focus();
            },
        },
    }
</script>
<style scoped>
</style>
