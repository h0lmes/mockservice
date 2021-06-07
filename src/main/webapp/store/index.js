export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    settings: {},
    routes: [],
    scenarios: [],
    activeScenarios: [],
    lastError: ''
});

export const mutations = {
    setSettings(state, payload) {
        state.settings = payload;
    },

    setRoutes(state, payload) {
        state.routes = payload;
    },
    addRoute(state, payload) {
        state.routes.unshift(payload);
    },

    setScenarios(state, payload) {
        state.scenarios = payload;
    },
    addScenario(state, payload) {
        state.scenarios.unshift(payload);
    },
    setActiveScenarios(state, payload) {
        state.activeScenarios = payload;
    },

    setLastError(state, payload) {
        state.lastError = payload;
        console.log('Error: ', payload);
    },
    resetLastError(state) {
        state.lastError = '';
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
    return response;
}

export const actions = {
    async fetchSettings({commit, state}) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/settings'
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setSettings', response)
        ).catch(error => commit('setLastError', error));
    },
    async saveSettings({commit, state}, settings) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/settings',
            {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({...state.settings, ...settings})
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setSettings', response)
        ).catch(error => commit('setLastError', error));
    },

    setLastError({commit}, text) {
        commit('setLastError', text);
    },
    resetLastError({commit}) {
        commit('resetLastError');
    },

    fetchConfig({commit, state}) {
        commit('resetLastError');
        return fetch(state.BASE_URL + '/web-api/config'
        ).then(handleError
        ).then(response => response.text()
        ).catch(error => commit('setLastError', error));
    },
    async saveConfig({commit, state}, config) {
        commit('resetLastError');
        return fetch(state.BASE_URL + '/web-api/config',
            {
                method: 'PUT',
                headers: {'Content-Type': 'text/plain'},
                body: config
            }
        ).then(handleError
        ).then(response => response.text()
        ).then(commit('resetLastError')
        ).catch(error => commit('setLastError', error));
    },

    async fetchRoutes({commit, state}) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/routes'
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setRoutes', response)
        ).catch(error => commit('setLastError', error));
    },
    async saveRoute({commit, state}, routes) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/routes',
            {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(routes)
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setRoutes', response)
        ).catch(error => commit('setLastError', error));
    },
    async deleteRoute({commit, state}, route) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/routes',
            {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(route)
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setRoutes', response)
        ).catch(error => commit('setLastError', error));
    },
    newRoute({commit}) {
        commit('addRoute', {group: '', type: 'REST', method: 'GET', path: '/', alt: '', disabled: false, _new: true});
    },

    async fetchScenarios({commit, state}) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/scenarios'
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setScenarios', response)
        ).catch(error => commit('setLastError', error));
    },
    async saveScenario({commit, state}, scenarios) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/scenarios',
            {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenarios)
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setScenarios', response)
        ).catch(error => commit('setLastError', error));
    },
    async deleteScenario({commit, state}, scenario) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/scenarios',
            {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(scenario)
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setScenarios', response)
        ).catch(error => commit('setLastError', error));
    },
    newScenario({commit}) {
        commit('addScenario', {group: 'Default', alias: 'New Alias', type: 'MAP', _new: true});
    },
    async fetchActiveScenarios({commit, state}) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/scenarios/active'
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setActiveScenarios', response)
        ).catch(error => commit('setLastError', error));
    },
    async activateScenario({commit, state}, alias) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/scenarios/active',
            {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: alias
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setActiveScenarios', response)
        ).catch(error => commit('setLastError', error));
    },
    async deactivateScenario({commit, state}, alias) {
        commit('resetLastError');
        return fetch(
            state.BASE_URL + '/web-api/scenarios/active',
            {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: alias
            }
        ).then(handleError
        ).then(response => response.json()
        ).then(response => commit('setActiveScenarios', response)
        ).catch(error => commit('setLastError', error));
    },
};