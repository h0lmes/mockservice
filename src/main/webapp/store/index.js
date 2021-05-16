export const state = () => ({
    datafiles: []
});

export const mutations = {
    setDataFiles(state, datafiles) {
        state.datafiles = datafiles
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
    }
};