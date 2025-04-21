export const state = () => ({
    topics: [],
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
            initialData: '',
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
    async produce({commit, rootState}, data) {
        try {
            const url = rootState.BASE_URL + '/__kafka__/producer';
            const params = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            };
            const res = await fetch(url, params);
            await handleError(res);
            await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async appendItem({commit, rootState}, {text, topic, partition}) {
        try {
            const queryParams = {topic: topic, partition: partition};
            const url = rootState.BASE_URL + '/__kafka__/append-item?' + new URLSearchParams(queryParams).toString();
            const params = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: text
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            return JSON.stringify(data, null, 4);
        } catch (err) {
            commit('setLastError', err, {root: true});
            return text;
        }
    },
};
