export const state = () => ({
    topics: []
});

export const mutations = {
    store(state, payload) {
        state.topics = payload;
    },
    add(state, payload) {
        state.topics.unshift(payload);
    },
};

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__kafka__';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, topics) {
        try {
            const url = rootState.BASE_URL + '/__kafka__';
            const params = {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(topics)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, rootState}, topics) {
        try {
            const url = rootState.BASE_URL + '/__kafka__';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(topics)
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
        const topic = {
            group: '',
            topic: 'new topic',
            partition: 0,
            _new: true,
        };
        commit('add', topic);
    },
    async records({commit, rootState}, search) {
        try {
            const url = rootState.BASE_URL + '/__kafka__/records?' + new URLSearchParams(search).toString();
            const res = await fetch(url);
            await handleError(res);
            return await res.json();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};
