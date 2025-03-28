export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    lastError: '',
    search: ''
});

export const mutations = {
    setLastError(state, payload) {
        state.lastError = payload;
        console.log('Error: ', payload);
    },
    resetLastError(state) {
        state.lastError = '';
    },
    setSearch(state, payload) {
        state.search = payload;
    },
    resetSearch(state) {
        state.search = '';
    },
};

export const actions = {
    setLastError({commit}, text) {
        commit('setLastError', text);
    },
    resetLastError({commit}) {
        commit('resetLastError');
    },
    setSearch({commit}, text) {
        commit('setSearch', text);
    },
    resetSearch({commit}) {
        commit('resetSearch');
    },
};
