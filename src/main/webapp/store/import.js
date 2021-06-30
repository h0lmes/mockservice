export const state = () => ({
    routes: []
});

export const mutations = {
    store(state, payload) {
        state.routes = payload;
    },
};

import {handleError} from "../js/common";

export const actions = {
    async openapi3({commit, dispatch, rootState}, param) {
        try {
            const url = rootState.BASE_URL + '/web-api/import/openapi3';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'text/plain'},
                body: param
            };
            const res = await fetch(url, params);
            await handleError(res);
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
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};