<template>
    <div class="var-row">
        <FontAwesomeIcon icon="hashtag" class="ml-2"/>

        <div class="var-col">
            <div class="mock-col-value">{{ variable.name }}</div>
        </div>
        <div class="var-col">
            <input type="text"
                   class="form-control form-control-sm"
                   v-model="value"
                   :placeholder="defaultValue"/>
        </div>
        <div class="var-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-default" @click="setVar">set</button>
            <button type="button" class="btn btn-sm btn-danger" @click="clearVar">clear</button>
        </div>

        <div class="var-col w3"></div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';

    export default {
        name: "RouteVariable",
        components: {},
        data() {
            return {
                value: '',
            }
        },
        props: {
            route: {type: Object},
            variable: {type: Object},
        },
        computed: {
            defaultValue() {
                return this.variable.value === null ? this.variable.defaultValue : '';
            },
        },
        mounted() {
            this.value = this.variable.value;
        },
        watch: {
            variable: {
                handler(newValue) {
                    this.value = newValue.value;
                    console.log('newValue = ', newValue);
                },
                deep: true
            }
        },
        methods: {
            ...mapActions({
                setVariable: 'routes/setVariable',
                clearVariable: 'routes/clearVariable',
            }),
            getVar() {
                return {
                    method: this.route.method,
                    path: this.route.path,
                    alt: this.route.alt,
                    name: this.variable.name,
                    value: this.value ? this.value : '',
                };
            },
            setVar() {
                this.$nuxt.$loading.start();
                this.setVariable(this.getVar()).then(() => {
                    this.$nuxt.$loading.finish();
                });
            },
            clearVar() {
                this.$nuxt.$loading.start();
                this.clearVariable(this.getVar()).then(() => {
                    this.$nuxt.$loading.finish();
                });
            },
        },
    }
</script>
<style lang="scss" scoped>
    .var-row {
        margin: -0.2rem 0;
        padding: 0.2rem 0;
        display: flex;
        gap: 0.7rem 1rem;
        flex-wrap: wrap;
        justify-content: left;
        align-content: space-around;
        align-items: center;
    }

    .var-col {
        flex: 1 1 0;
        position: relative;
        box-sizing: border-box;
        min-width: 7rem;

        &.w-fixed-auto {
            flex: 0 1 auto;
            min-width: initial;
        }
    }
</style>