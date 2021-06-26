export const state = () => ({
});

export const mutations = {
};

export const actions = {
    async fetch({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/log';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};