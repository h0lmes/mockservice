export const state = () => ({
    value: ''
});

export const mutations = {
    store(state, payload) {
        state.value = payload;
    },
};

import {handleError} from "../js/common";

export const actions = {
    async json({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/generate/json';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.text();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};