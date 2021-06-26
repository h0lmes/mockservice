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

export const actions = {
    async fetch({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, dispatch, rootState}, scenarios) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenarios)
            };
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, dispatch, rootState}, scenario) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenario)
            };
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    add({commit}) {
        commit('add', {group: 'Default', alias: 'New Alias', type: 'MAP', _new: true});
    },
    async fetchActive({commit, dispatch, rootState}) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const res = await fetch(url);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('active', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async activate({commit, dispatch, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const params = {method: 'PUT', body: alias};
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('active', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async deactivate({commit, dispatch, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const params = {method: 'DELETE', body: alias};
            const res = await fetch(url, params);
            dispatch('handleError', res, {root: true});
            const data = await res.json();
            commit('active', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
};