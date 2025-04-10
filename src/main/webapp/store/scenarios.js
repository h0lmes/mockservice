export const state = () => ({
    scenarios: [],
});

export const mutations = {
    store(state, payload) {
        state.scenarios = payload;
    },
    add(state, payload) {
        state.scenarios.unshift(payload);
    },
    activate(state, alias) {
        for (let i = 0; i < state.scenarios.length; i++) {
            if (state.scenarios[i].alias === alias) {
                state.scenarios[i].active = true;
            }
        }
    },
    deactivate(state, alias) {
        for (let i = 0; i < state.scenarios.length; i++) {
            if (state.scenarios[i].alias === alias) {
                state.scenarios[i].active = false;
            }
        }
    },
};

import {handleError} from "../js/common";

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/scenarios';
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
            const url = rootState.BASE_URL + '/__webapi__/scenarios';
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
            const url = rootState.BASE_URL + '/__webapi__/scenarios';
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
        const scenario = {
            group: '',
            alias: 'New Scenario',
            type: 'MAP',
            data: '',
            _new: true
        };
        commit('add', scenario);
    },
    async activate({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/scenarios/active';
            const params = {method: 'PUT', body: alias};
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            if (data.includes(alias)) {
                commit('activate', alias);
            }
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async deactivate({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/scenarios/active';
            const params = {method: 'DELETE', body: alias};
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            if (!data.includes(alias)) {
                commit('deactivate', alias);
            }
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};
