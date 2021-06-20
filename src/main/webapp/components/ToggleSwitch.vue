<template>
    <div class="toggle-switch" tabindex="0" :class="{small : !!small}" @keydown.space.exact="$refs.input.click()">
        <input type="checkbox" tabindex="-1" ref="input" :id="id" :checked="value" @input="e => input(e)">
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
            return {
                id: null
            }
        },
        props: {
            value: {type: Boolean},
            small: {type: Boolean},
        },
        computed: {
            hasTitle() {
                return !!this.$slots.default;
            }
        },
        created() {
            this.id = this.uuid();
        },
        methods: {
            input(e) {
                this.$emit('input', e.target.checked);
                this.$emit('toggle', e.target.checked);
            },
            uuid() {
                return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                    let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
                    return v.toString(16);
                });
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

        --toggle-switch-bg-unchecked: darkgray;
        --toggle-switch-bg-unchecked-focus: gray;
        --toggle-switch-bg-checked: var(--color-accent-one);
        --toggle-switch-bg-handle: white;

        &.small {
            --width: 2rem;
            --height: 1.25rem;
        }

        display: inline-block;
        outline-width: 0;
        box-sizing: content-box;
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

    .toggle-switch > label {
        display: inline-grid;
        grid-template-columns: auto auto;
        column-gap: 0;
        line-height: var(--height);

        &.gap {
            column-gap: 1rem;
        }
    }

    .toggle-switch .area {
        padding: 4px;
        margin: -4px;
    }

    .toggle-switch :active {
        outline-width: 0;
    }

    .toggle-switch .background,
    .toggle-switch .handle {
        transition: all 0.1s ease;
    }

    .toggle-switch .background {
        display: inline-flex;
        flex-direction: row;
        align-items: center;
        width: var(--width);
        height: var(--height);
        border-radius: var(--height);
        padding: 0 var(--padding);
        vertical-align: text-bottom;
        user-select: none;
        background-color: var(--toggle-switch-bg-unchecked);
        box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.25);
        overflow: hidden;
        transition: background-color .15s ease;
    }

    .toggle-switch:focus .area .background {
        box-shadow: 0 0 5px var(--toggle-switch-bg-checked);
    }

    .toggle-switch:active .area {
        outline-width: 0;
    }

    .toggle-switch:focus .background,
    .toggle-switch .area:hover .background {
        background-color: var(--toggle-switch-bg-unchecked-focus);
    }

    .toggle-switch .handle {
        width: var(--handle-size);
        height: var(--handle-size);
        background-color: var(--toggle-switch-bg-handle);
        border-radius: 50%;
        box-shadow:
                0 2px 4px rgba(0, 0, 0, 0.5),
                inset 0 2px 4px rgba(0, 0, 0, 0.15);
    }

    .toggle-switch .handle:hover {
        background-color: var(--toggle-switch-bg-handle);
    }

    .toggle-switch > input:checked + label .area .background {
        background-color: var(--toggle-switch-bg-checked);
    }

    .toggle-switch > input:checked + label .area .handle {
        background-color: var(--toggle-switch-bg-handle);
        transform: translateX(calc(var(--width) - var(--handle-size)));
    }
</style>