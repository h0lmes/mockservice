export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    lastError: '',
    apiSearchExpression: '',
    kafkaSearchExpression: '',
});

export const mutations = {
    setLastError(state, payload) {
        state.lastError = payload;
        console.log('Error: ', payload);
    },
    resetLastError(state) {
        state.lastError = '';
    },
    setApiSearchExpression(state, payload) {
        state.apiSearchExpression = payload == null ? '' : ('' + payload).trim();
    },
    setKafkaSearchExpression(state, payload) {
        state.kafkaSearchExpression = payload == null ? '' : ('' + payload).trim();
    },
};

export const actions = {
    setLastError({commit}, text) {
        commit('setLastError', text);
    },
    resetLastError({commit}) {
        commit('resetLastError');
    },
    setApiSearchExpression({commit}, text) {
        commit('setApiSearchExpression', text);
    },
    setKafkaSearchExpression({commit}, text) {
        commit('setKafkaSearchExpression', text);
    },
};
