export const state = () => ({
    scenarios: [],
    activeScenarios: [],
});

export const mutations = {
    setScenarios(state, payload) {
        state.scenarios = payload;
    },
    addScenario(state, payload) {
        state.scenarios.unshift(payload);
    },
    setActiveScenarios(state, payload) {
        state.activeScenarios = payload;
    },
};

async function handleError(response) {
    if (response.status === 400) {
        const errorInfo = await response.json();
        throw Error(errorInfo.message || errorInfo);
    }
    if (!response.ok) {
        throw Error(response.statusText || response);
    }
}

export const actions = {
    async fetchScenarios({commit, dispatch, rootState}) {
        dispatch('resetLastError', {}, {root: true});
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const res = await fetch(url);
            handleError(res);
            const data = await res.json();
            commit('setScenarios', data);
        } catch (err) {
            dispatch('setLastError', err, {root: true});
        }
    },
    async saveScenario({commit, dispatch, rootState}, scenarios) {
        dispatch('resetLastError', {}, {root: true});
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenarios)
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setScenarios', data);
        } catch (err) {
            dispatch('setLastError', err, {root: true});
        }
    },
    async deleteScenario({commit, dispatch, rootState}, scenario) {
        dispatch('resetLastError', {}, {root: true});
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenario)
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setScenarios', data);
        } catch (err) {
            dispatch('setLastError', err, {root: true});
        }
    },
    newScenario({commit}) {
        commit('addScenario', {group: 'Default', alias: 'New Alias', type: 'MAP', _new: true});
    },
    async fetchActiveScenarios({commit, dispatch, rootState}) {
        dispatch('resetLastError', {}, {root: true});
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const res = await fetch(url);
            handleError(res);
            const data = await res.json();
            commit('setActiveScenarios', data);
        } catch (err) {
            dispatch('setLastError', err, {root: true});
        }
    },
    async activateScenario({commit, dispatch, rootState}, alias) {
        dispatch('resetLastError', {}, {root: true});
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: alias
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setActiveScenarios', data);
        } catch (err) {
            dispatch('setLastError', err, {root: true});
        }
    },
    async deactivateScenario({commit, dispatch, rootState}, alias) {
        dispatch('resetLastError', {}, {root: true});
        try {
            const url = rootState.BASE_URL + '/web-api/scenarios/active';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: alias
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setActiveScenarios', data);
        } catch (err) {
            dispatch('setLastError', err, {root: true});
        }
    },
};