export const state = () => ({
    value: ''
});

export const mutations = {
    store(state, payload) {
        state.value = payload;
    },
};

export const actions = {
    async json({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/generate/json';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            const data = await res.text();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};