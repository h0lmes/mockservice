export const state = () => ({
    BASE_URL: location.protocol + '//' + location.hostname + ':8081',
    datafiles: [],
    routes: [],
    config: {}
});

export const mutations = {
    setDataFiles(state, datafiles) {
        state.datafiles = datafiles
    },
    setRoutes(state, routes) {
        state.routes = routes
    },
    addRoute(state, route) {
        state.routes.unshift(route);
    },
    setConfig(state, config) {
        state.config = config
    }
};

export const actions = {
    async fetchDataFiles({commit, state}) {
        commit('setDataFiles',
            await fetch(state.BASE_URL + '/web-api/datafiles').then(
                res => res.json()
            )
        )
    },

    async fetchRoutes({commit, state}) {
        commit('setRoutes',
            await fetch(state.BASE_URL + '/web-api/routes').then(
                res => res.json()
            )
        )
    },

    async saveRoute({commit, state}, routes) {
        commit('setRoutes', await fetch(
            state.BASE_URL + '/web-api/route',
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(routes)
            }
        ).then(
            res => res.json()
        ))
    },

    async deleteRoute({commit, state}, route) {
        commit('setRoutes', await fetch(
            state.BASE_URL + '/web-api/route',
            {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(route)
            }
        ).then(
            res => res.json()
        ))
    },

    newRoute({commit}) {
        commit('addRoute', {group: '', type: 'REST', method: 'GET', path: '/', suffix: '', new: true});
    },

    async fetchConfig({commit, state}) {
        commit('setConfig',
            await fetch(state.BASE_URL + '/web-api/config').then(
                res => res.json()
            )
        )
    },

    async saveConfig({commit, state}, config) {
        commit('setConfig', config);
        await fetch(
            state.BASE_URL + '/web-api/config',
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(config)
            }
        )
    },
};