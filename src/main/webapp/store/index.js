export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    settings: {},
    routes: [],
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
}

export const actions = {
    setLastError({commit}, text) {
        commit('setLastError', text);
    },
    resetLastError({commit}) {
        commit('resetLastError');
    },

    async fetchSettings({commit, state}) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/settings';
            const res = await fetch(url);
            handleError(res);
            const data = await res.json();
            commit('setSettings', data);
        } catch (err) {
            commit('setLastError', err);
        }
    },
    async saveSettings({commit, state}, settings) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/settings';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({...state.settings, ...settings})
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setSettings', data);
        } catch (err) {
            commit('setLastError', err);
        }
    },

    async fetchConfig({commit, state}) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/config';
            const res = await fetch(url);
            handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err);
        }
    },
    async saveConfig({commit, state}, config) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/config';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'text/plain'},
                body: config
            };
            const res = await fetch(url, params);
            handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err);
        }
    },
    async backupConfig({commit, state}) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/config/backup';
            const res = await fetch(url);
            handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err);
        }
    },
    async restoreConfig({commit, state}) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/config/restore';
            const res = await fetch(url);
            handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err);
        }
    },

    async fetchLog({commit, state}) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/log';
            const res = await fetch(url);
            handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err);
        }
    },

    async fetchRoutes({commit, state}) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/routes';
            const res = await fetch(url);
            handleError(res);
            const data = await res.json();
            commit('setRoutes', data);
        } catch (err) {
            commit('setLastError', err);
        }
    },
    async saveRoute({commit, state}, routes) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/routes';
            const params = {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(routes)
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setRoutes', data);
        } catch (err) {
            commit('setLastError', err);
        }
    },
    async deleteRoute({commit, state}, route) {
        commit('resetLastError');
        try {
            const url = state.BASE_URL + '/web-api/routes';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(route)
            };
            const res = await fetch(url, params);
            handleError(res);
            const data = await res.json();
            commit('setRoutes', data);
        } catch (err) {
            commit('setLastError', err);
        }
    },
    newRoute({commit}) {
        commit('addRoute', {group: '', type: 'REST', method: 'GET', path: '/', alt: '', disabled: false, _new: true});
    },
};