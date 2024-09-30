export const state = () => ({
    items: []
});

export const mutations = {
    store(state, payload) {
        state.items = payload;
    },
};

import {handleError} from "~/js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/products';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};
