export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    lastError: ''
});

export const mutations = {
    setLastError(state, payload) {
        state.lastError = payload;
        console.log('Error: ', payload);
    },
    resetLastError(state) {
        state.lastError = '';
    },
};

export const actions = {
    setLastError({commit}, text) {
        commit('setLastError', text);
    },
    resetLastError({commit}) {
        commit('resetLastError');
    },
};
