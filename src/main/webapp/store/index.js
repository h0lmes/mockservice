export const state = () => ({
    datafiles: [],
    routes: []
});

export const mutations = {
    setDataFiles(state, datafiles) {
        state.datafiles = datafiles
    },
    setRoutes(state, routes) {
        state.routes = routes
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
    }
};