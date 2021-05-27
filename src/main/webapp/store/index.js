export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    datafiles: [],
    routes: [],
    lastError: ''
});

export const mutations = {
    setDataFiles(state, payload) {
        state.datafiles = payload
    },
    setRoutes(state, payload) {
        state.routes = payload
    },
    addRoute(state, payload) {
        state.routes.unshift(payload);
    },
    setLastError(state, payload) {
        state.lastError = payload
    }
};

function handleError(response) {
    if (!response.ok) {
        throw Error(response.statusText);
    }
    return response;
}

export const actions = {
    setLastError({commit}, text) {
        commit('setLastError', text);
    },

    resetLastError({commit}) {
        commit('setLastError', '');
    },

    async fetchDataFiles({commit, state, dispatch}) {
        fetch(
            state.BASE_URL + '/web-api/datafiles'
        ).then(
            handleError
        ).then(response => {
            return response.json();
        }).then(response => {
            commit('setDataFiles', response);
        }).catch(error => {
            console.log('Error: ', error);
            commit('setLastError', error);
        });
    },

    async fetchRoutes({commit, state}) {
        fetch(
            state.BASE_URL + '/web-api/routes'
        ).then(
            handleError
        ).then(response => {
            return response.json();
        }).then(response => {
            commit('setRoutes', response);
        }).catch(error => {
            console.log('Error: ', error);
            commit('setLastError', error);
        });
    },

    async saveRoute({commit, state}, routes) {
        fetch(
            state.BASE_URL + '/web-api/route',
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(routes)
            }
        ).then(
            handleError
        ).then(response => {
            return response.json();
        }).then(response => {
            commit('setRoutes', response);
        }).catch(error => {
            console.log('Error: ', error);
            commit('setLastError', error);
        });
    },

    async deleteRoute({commit, state}, route) {
        fetch(
            state.BASE_URL + '/web-api/route',
            {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(route)
            }
        ).then(
            handleError
        ).then(response => {
            return response.json();
        }).then(response => {
            commit('setRoutes', response);
        }).catch(error => {
            console.log('Error: ', error);
            commit('setLastError', error);
        });
    },

    newRoute({commit}) {
        commit('addRoute', {group: '', type: 'REST', method: 'GET', path: '/', suffix: '', new: true});
    },
};