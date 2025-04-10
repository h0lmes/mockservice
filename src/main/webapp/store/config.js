export const state = () => ({
});

export const mutations = {
};

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/config';
            const res = await fetch(url);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, config) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/config';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'text/plain'},
                body: config
            };
            const res = await fetch(url, params);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async backup({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/config/backup';
            const res = await fetch(url);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async restore({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/config/restore';
            const res = await fetch(url);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};
