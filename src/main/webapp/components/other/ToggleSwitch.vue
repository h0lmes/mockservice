<template>
    <div class="toggle-switch" tabindex="0" :class="{small : !!small}" @keydown.space.exact="inputRef?.click()">
        <input type="checkbox" tabindex="-1" ref="inputRef" :id="id" :checked="checkedValue" @input="input">
        <label :for="id" :class="{gap : hasTitle}">
            <div class="area" aria-hidden="true">
                <div class="background">
                    <div class="handle"></div>
                </div>
            </div>
            <slot></slot>
            <div v-if="sub"></div>
            <div v-if="sub" class="toggle-switch__sub color-secondary">{{ sub }}</div>
        </label>
    </div>
</template>

<script setup lang="ts">
import {computed, ref, useSlots} from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: boolean
  value?: boolean
  small?: boolean
  sub?: string
}>(), {
  modelValue: undefined,
  value: undefined,
  small: false,
  sub: undefined,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  input: [value: boolean]
  toggle: [value: boolean]
}>()

const slots = useSlots()
const inputRef = ref<HTMLInputElement | null>(null)
const id = `toggle-${Math.random().toString(36).slice(2)}-${Date.now().toString(36)}`
const hasTitle = computed(() => !!slots.default)
const checkedValue = computed(() => props.modelValue !== undefined ? props.modelValue : props.value)

const input = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked
  emit('update:modelValue', checked)
  emit('input', checked)
  emit('toggle', checked)
}

const selectionStart = () => inputRef.value?.selectionStart
const selectionEnd = () => inputRef.value?.selectionEnd
const focus = () => inputRef.value?.focus()

defineExpose({
  selectionStart,
  selectionEnd,
  focus,
})
</script>

<style lang="scss" scoped>
    .toggle-switch {
        --width: 2.5rem;
        --height: 1.75rem;
        &.small {
            --width: 2rem;
            --height: 1.25rem;
        }

        --padding: 2px;
        --handle-size: calc(var(--height) - var(--padding) * 2);

        --toggle-switch-bg-unchecked: darkgray;
        --toggle-switch-bg-unchecked-focus: gray;
        --toggle-switch-bg-checked: var(--color-accent-one);
        --toggle-switch-bg-handle: var(--bg-component);

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
        overflow-wrap: anywhere;
        cursor: pointer;

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

    .toggle-switch .sub {
        display: inline-block !important;
        overflow-wrap: anywhere;
        margin-top: 0.5rem;
        font-size: smaller;
    }
</style>
