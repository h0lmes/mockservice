<template>
    <div class="holder">
        <span class="source">{{ datafile.source }}</span>
        <span class="part name" v-if="!hasParts">{{ datafile.name }}</span>
        <span class="part group" v-if="hasParts">{{ group }}</span><span class="part name" v-if="hasParts">{{ fileSeparator }}</span><span class="part http-method" v-if="hasParts">{{ httpMethod }}</span><span class="part name" v-if="hasParts">{{ requestMapping }}</span><span class="part option" v-if="hasParts">{{ option }}</span><span class="part file-extension" v-if="hasParts">{{ fileExtension }}</span>
    </div>
</template>
<script>
    export default {
        name: "DataFileInfo",
        data() {
            return {};
        },
        props: {
            datafile: Object
        },
        computed: {
            parts() {
                if (this.datafile && this.datafile.name) {
                    const regex = /^(.+)(\\|\/)(get|post|put|delete|patch)?(.+)(\.json|\.xml)$/;
                    return this.datafile.name.match(regex);
                }
                return [];
            },
            hasParts() {
                return this.parts ? this.parts.length > 0 : false;
            },
            group() {
                return this.hasParts ? this.parts[1] : '';
            },
            fileSeparator() {
                return this.hasParts ? this.parts[2] : '';
            },
            httpMethod() {
                return this.hasParts ? this.parts[3] : '';
            },
            requestMapping() {
                if (this.hasParts) {
                    const regex = /^(.+)(--.+)$/;
                    let found = this.parts[4].match(regex);
                    return found ? found[1] : this.parts[4];
                }
                return '';
            },
            option() {
                if (this.hasParts) {
                    const regex = /^(.+)(--.+)$/;
                    let found = this.parts[4].match(regex);
                    return found ? found[2] : '';
                }
                return '';
            },
            fileExtension() {
                return this.hasParts ? this.parts[5] : '';
            }
        }
    }
</script>
<style scoped>
    .holder {
        padding: .1rem;
    }
    .source {
        font-size: .7rem;
        background-color: #317373;
        color: rgba(255,255,255,.5);
        border: 0 none;
        border-radius: .25rem;
        padding: .1rem .3rem;
        margin: 0 .3rem 0 0;
    }
    .part {
        display: inline;
        margin: 0;
        padding: 0;
    }
    .group {
        color: rgb(203 141 255);
    }
    .http-method {
        color: rgb(140 255 153);
    }
    .option {
        color: rgb(255 218 47);
    }
    .file-extension {
        color: rgb(255 91 143);
    }
</style>