export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    routes: [],
    lastError: ''
});

export const mutations = {
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
    }
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
    setLastError({commit}, text) {
        commit('setLastError', text);
    },
    resetLastError({commit}) {
        commit('resetLastError');
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
            state.BASE_URL + '/web-api/route',
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
            state.BASE_URL + '/web-api/route',
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
        commit('addRoute', {group: '', type: 'REST', method: 'GET', path: '/', suffix: '', _new: true});
    },
};