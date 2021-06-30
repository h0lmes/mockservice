export const state = () => ({
    scenarios: [],
    activeScenarios: [],
});

export const mutations = {
    store(state, payload) {
        state.scenarios = payload;
    },
    add(state, payload) {
        state.scenarios.unshift(payload);
    },
    active(state, payload) {
        state.activeScenarios = payload;
    },
};

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, scenarios) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenarios)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, rootState}, scenario) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenario)
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
        commit('add', {group: 'Default', alias: 'New Alias', type: 'MAP', _new: true});
    },
    async fetchActive({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('active', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async activate({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const params = {method: 'PUT', body: alias};
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('active', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async deactivate({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const params = {method: 'DELETE', body: alias};
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('active', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};