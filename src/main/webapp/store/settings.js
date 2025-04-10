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
            const url = rootState.BASE_URL + '/__webapi__/settings';
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
            const url = rootState.BASE_URL + '/__webapi__/settings';
            const params = {
                method: 'POST',
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
    async certificatePassword({commit, rootState}, password) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/settings/certificatePassword';
            const params = {method: 'POST', body: password};
            const res = await fetch(url, params);
            await handleError(res);
            await res.text();
            return true;
        } catch (err) {
            commit('setLastError', err, {root: true});
            return false;
        }
    },
};
