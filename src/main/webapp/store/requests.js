import {addSelectedProperty, handleError, selectAll} from "../js/common";

export const state = () => ({
    requests: []
});

export const mutations = {
    store(state, payload) {
        addSelectedProperty(payload);
        state.requests = payload;
    },
    add(state, payload) {
        state.requests.unshift(payload);
    },
    select(state, payload) {
        for (let i = 0; i < state.requests.length; i++) {
            if (state.requests[i].id === payload.request.id) {
                state.requests[i]._selected = payload.selected;
            }
        }
    },
    selectAll(state, payload) {
        selectAll(state.requests, payload);
    },
};

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/requests';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, requests) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/requests';
            const params = {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(requests)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async saveAll({commit, rootState}, payload) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/requests';
            const method = payload.overwrite ? 'PUT' : 'POST';
            const params = {
                method,
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload.requests)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, rootState}, requests) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/requests';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(requests)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async execute({commit, rootState}, requestId) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/requests/execute';
            const params = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({requestId})
            };
            const res = await fetch(url, params);
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    add({commit}) {
        const request = {
            id: '',
            group: '',
            type: 'REST',
            method: 'GET',
            path: '/',
            headers: '',
            body: '',
            responseToVars: false,
            disabled: false,
            triggerRequest: false,
            triggerRequestIds: '',
            triggerRequestDelay: '',
            _new: true,
            _selected: null,
        };
        commit('add', request);
    },
    select({commit}, payload) {
        commit('select', payload);
    },
    selectAll({commit}, payload) {
        commit('selectAll', payload);
    },
};
