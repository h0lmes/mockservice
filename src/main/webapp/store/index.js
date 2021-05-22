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
        let path = window.location + '';
        if (path.includes(':3000')) {
            path = path.replace(':3000', ':8081'); // dev mode URL fix
        }

        commit('setDataFiles', await fetch(
            path + '/web-api/datafiles'
        ).then(
            res => res.json()
        ))
    },
    async fetchRoutes({ commit }) {
        let path = window.location + '';
        if (path.includes(':3000')) {
            path = path.replace(':3000', ':8081'); // dev mode URL fix
        }

        commit('setRoutes', await fetch(
            path + '/web-api/routes'
        ).then(
            res => res.json()
        ))
    }
};