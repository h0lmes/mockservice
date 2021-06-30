export const state = () => ({
    settings: {}
});

export const mutations = {
    store(state, payload) {
        state.settings = payload;
    },
};

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/settings';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, state, rootState}, settings) {
        try {
            const url = rootState.BASE_URL + '/web-api/settings';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({...state.settings, ...settings})
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