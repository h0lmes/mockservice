export const state = () => ({
    routes: []
});

export const mutations = {
    store(state, payload) {
        state.routes = payload;
    },
    add(state, payload) {
        state.routes.unshift(payload);
    },
};

export const actions = {
    async fetch({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, dispatch, rootState}, routes) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(routes)
            };
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, dispatch, rootState}, route) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(route)
            };
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    add({commit}) {
        commit('add', {group: '', type: 'REST', method: 'GET', path: '/', alt: '', disabled: false, _new: true});
    },
};