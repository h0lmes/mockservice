export const state = () => ({
});

export const mutations = {
};

export const actions = {
    async fetch({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/config';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, dispatch, rootState}, config) {
        try {
            const url = rootState.BASE_URL + '/web-api/config';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'text/plain'},
                body: config
            };
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async backup({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/config/backup';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async restore({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/config/restore';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};