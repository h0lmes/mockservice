export const state = () => ({
});

export const mutations = {
};

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/variables';
            const res = await fetch(url);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, data) {
        try {
            const url = rootState.BASE_URL + '/web-api/variables';
            const params = {
                method: 'POST',
                headers: {'Content-Type': 'text/plain'},
                body: data || ' '
            };
            const res = await fetch(url, params);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};
