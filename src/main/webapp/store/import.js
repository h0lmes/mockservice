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
    async import({commit, dispatch, rootState}, param) {
        try {
            const url = rootState.BASE_URL + '/web-api/import';
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
};