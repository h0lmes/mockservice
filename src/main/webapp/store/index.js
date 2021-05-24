export const state = () => ({
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
    setConfig(state, config) {
        state.config = config
    }
};

export const actions = {
    async fetchDataFiles({ commit }) {
        let host = location.protocol + '//' + location.hostname + ':8081';
        commit('setDataFiles', await fetch(host + '/web-api/datafiles').then(
            res => res.json()
        ))
    },

    async fetchRoutes({ commit }) {
        let host = location.protocol + '//' + location.hostname + ':8081';
        commit('setRoutes', await fetch(host + '/web-api/routes').then(
            res => res.json()
        ))
    },

    async saveRoute({ commit }, routes) {
        let host = location.protocol + '//' + location.hostname + ':8081';
        commit('setRoutes', await fetch(
            host + '/web-api/route',
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

    async deleteRoute({ commit }, route) {
        let host = location.protocol + '//' + location.hostname + ':8081';
        commit('setRoutes', await fetch(
            host + '/web-api/route',
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

    async fetchConfig({ commit }) {
        let host = location.protocol + '//' + location.hostname + ':8081';
        commit('setConfig', await fetch(host + '/web-api/config').then(
            res => res.json()
        ))
    },
};