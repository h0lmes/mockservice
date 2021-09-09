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
    async saveAll({commit, rootState}, payload) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes';
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
        const route = {
            id: '',
            group: '',
            type: 'REST',
            method: 'GET',
            path: '/',
            alt: '',
            response: '',
            disabled: false,
            variables: null,
            _new: true,
        };
        commit('add', route);
    },
    async setVariable({commit, rootState}, variable) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes/variables';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(variable)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('set', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async clearVariable({commit, rootState}, variable) {
        try {
            const url = rootState.BASE_URL + '/web-api/routes/variables';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(variable)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('clear', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};