export const state = () => ({
    settings: {}
});

export const mutations = {
    store(state, payload) {
        state.settings = payload;
    },
};

export const actions = {
    async fetch({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/settings';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, dispatch, state, rootState}, settings) {
        try {
            const url = rootState.BASE_URL + '/web-api/settings';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({...state.settings, ...settings})
            };
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};