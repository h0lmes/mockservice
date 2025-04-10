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
    async json({commit, rootState}, schema) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/generate/json';
            let res;
            if (schema) {
                const params = {
                    method: 'POST',
                    body: schema
                };
                res = await fetch(url, params);
            } else {
                res = await fetch(url);
            }
            await handleError(res);
            const data = await res.text();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};
