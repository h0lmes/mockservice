<template>
    <div class="toggle-switch" tabindex="0" :class="{small : !!small}">
        <input type="checkbox" :id="id" :checked="value" @input="e => input(e)">
        <label :for="id" :class="{gap : hasTitle}">
            <div class="area" aria-hidden="true">
                <div class="background">
                    <div class="handle"></div>
                </div>
            </div>
            <slot></slot>
        </label>
    </div>
</template>

<script>
    export default {
        name: "ToggleSwitch",
        data() {
            return {}
        },
        props: {
            id: {type: String},
            value: {type: Boolean},
            small: {type: Boolean},
        },
        computed: {
            hasTitle() {
                return !!this.$slots.default;
            }
        },
        methods: {
            input(e) {
                this.$emit('input', e.target.checked);
                this.$emit('toggle', e.target.checked);
            }
        },
    }
</script>
<style lang="scss" scoped>
    .toggle-switch {
        --width: 2.5rem;
        --height: 1.75rem;
        --padding: 2px;
        --handle-size: calc(var(--height) - var(--padding) * 2);

        &.small {
            --width: 2rem;
            --height: 1.25rem;
        }

        display: inline-block;
        outline-width: 0;
    }

    .toggle-switch > input {
        position: absolute;
        clip: rect(1px, 1px, 1px, 1px);
        clip-path: inset(50%);
        height: 1px;
        width: 1px;
        margin: -1px;
        overflow: hidden;
    }

    label {
        display: inline-grid;
        grid-template-columns: auto auto;
        column-gap: 0;
        line-height: var(--height);

        &.gap {
            column-gap: 1rem;
        }
    }

    .area {
        padding: 4px;
        margin: -4px;
    }

    .toggle-switch :active {
        outline-width: 0;
    }

    .background,
    .handle {
        transition: all 0.1s ease;
    }

    .background {
        display: inline-flex;
        flex-direction: row;
        align-items: center;
        width: var(--width);
        height: var(--height);
        border-radius: var(--height);
        padding: 0 var(--padding);
        vertical-align: text-bottom;
        user-select: none;
        background-color: darkgray;
        box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.25);
        overflow: hidden;
        transition: background-color .15s ease;
    }

    .toggle-switch:focus .area {
        outline: 1px dotted gray;
    }

    .toggle-switch:active .area {
        outline-width: 0;
    }

    .toggle-switch:focus .background,
    .area:hover .background {
        background-color: gray;
    }

    .handle {
        width: var(--handle-size);
        height: var(--handle-size);
        background-color: white;
        border-radius: 50%;
        box-shadow:
                0 2px 4px rgba(0, 0, 0, 0.5),
                inset 0 2px 4px rgba(0, 0, 0, 0.15);
    }

    .handle:hover {
        background-color: white;
    }

    input:checked + label .area .background {
        background-color: var(--color-accent-one);
    }

    input:checked + label .area .handle {
        background-color: white;
        transform: translateX(calc(var(--width) - var(--handle-size)));
    }
</style>