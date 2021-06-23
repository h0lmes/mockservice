<template>
    <div class="grow-wrap" ref="textWrap">
        <textarea class="form-control form-control-sm v-resize monospace"
                  :class="{'no-border' : !border}"
                  ref="textArea"
                  :value="value"
                  :placeholder="placeholder"
                  @input="e => input(e)"
                  @keydown="e => $emit('keydown', e)"
        ></textarea>
    </div>
</template>
<script>
    export default {
        name: "AutoSizeTextArea",
        data() {
            return {
                height: 2,
            }
        },
        props: {
            value: {type: String},
            placeholder: {type: String},
            border: {type: Boolean, default: true},
        },
        mounted() {
            this.resize();
        },
        updated() {
            this.resize();
        },
        methods: {
            input(e) {
                this.$emit('input', e.target.value);
            },
            resize() {
                this.$refs.textWrap.dataset.replicatedValue = this.value;
            },
            selectionStart() {
                return this.$refs.textArea.selectionStart;
            },
            selectionEnd() {
                return this.$refs.textArea.selectionEnd;
            },
        },
    }
</script>
<style scoped>
    .grow-wrap {
        display: grid;
    }
    .grow-wrap::after {
        content: attr(data-replicated-value) " ";
        white-space: pre-wrap;
        word-wrap: break-word;
        visibility: hidden;
    }
    .grow-wrap > textarea {
        resize: none;
        overflow-y: hidden;
        word-wrap: break-word;
    }
    .grow-wrap > textarea,
    .grow-wrap::after {
        grid-area: 1 / 1 / 2 / 2;
        box-sizing: border-box;
        padding: 0.25rem 0.5rem;
        font-size: 0.875rem;
        line-height: 1.5;
        border-radius: 0.2rem;
    }
    .grow-wrap::after {
        border: 1px solid transparent;
    }
</style>