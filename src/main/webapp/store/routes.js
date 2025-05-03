import {addSelectedProperty, handleError, selectAll} from "../js/common";

export const state = () => ({
    routes: []
});

export const mutations = {
    store(state, payload) {
        addSelectedProperty(payload);
        state.routes = payload;
    },
    add(state, payload) {
        state.routes.unshift(payload);
    },
    select(state, payload) {
        for (let i = 0; i < state.routes.length; i++) {
            if (state.routes[i].method === payload.route.method
                && state.routes[i].path === payload.route.path
                && state.routes[i].alt === payload.route.alt) {
                state.routes[i]._selected = payload.selected;
            }
        }
    },
    selectAll(state, payload) {
        selectAll(state.routes, payload);
    },
};

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/routes';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, routes) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/routes';
            const params = {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(routes)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async saveAll({commit, rootState}, payload) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/routes';
            const method = payload.overwrite ? 'PUT' : 'POST';
            const params = {
                method,
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload.routes)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, rootState}, routes) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/routes';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(routes)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    add({commit}) {
        const route = {
            group: '',
            type: 'REST',
            method: 'GET',
            path: '/',
            alt: '',
            response: '',
            disabled: false,
            triggerRequest: false,
            triggerRequestIds: '',
            triggerRequestDelay: '',
            _new: true,
            _selected: null,
        };
        commit('add', route);
    },
    select({commit}, payload) {
        commit('select', payload);
    },
    selectAll({commit}, payload) {
        commit('selectAll', payload);
    },
};
