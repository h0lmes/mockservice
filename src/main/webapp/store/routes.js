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

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, route) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
            const params = {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(route)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async saveAll({commit, rootState}, routes, overwrite) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
            const method = overwrite ? 'PUT' : 'POST';
            const params = {
                method,
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
    async delete({commit, rootState}, routes) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
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
        commit('add', {group: 'Default', type: 'REST', method: 'GET', path: '/', alt: '', disabled: false, _new: true});
    },
};