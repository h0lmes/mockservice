import {addSelectedProperty, handleError, selectAll} from "../js/common";

export const state = () => ({
    tests: []
});

export const mutations = {
    store(state, payload) {
        addSelectedProperty(payload);
        state.tests = payload;
    },
    add(state, payload) {
        state.tests.unshift(payload);
    },
    select(state, payload) {
        for (let i = 0; i < state.tests.length; i++) {
            if (state.tests[i].alias === payload.test.alias) {
                state.tests[i]._selected = payload.selected;
            }
        }
    },
    selectAll(state, payload) {
        selectAll(state.tests, payload);
    },
};

export const actions = {
    async fetch({commit, rootState}) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/tests';
            const res = await fetch(url);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async save({commit, rootState}, tests) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/tests';
            const params = {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(tests)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async saveAll({commit, rootState}, payload) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/tests';
            const method = payload.overwrite ? 'PUT' : 'POST';
            const params = {
                method,
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload.tests)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async delete({commit, rootState}, tests) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/tests';
            const params = {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(tests)
            };
            const res = await fetch(url, params);
            await handleError(res);
            const data = await res.json();
            commit('store', data);
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async execute({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/tests/execute';
            const params = {
                method: 'POST',
                headers: {'Content-Type': 'text/text'},
                body: alias
            };
            const res = await fetch(url, params);
            if (res.status === 404) {
                commit('setLastError', 'Test not found', {root: true});
                return ''
            }
            if (res.status === 202) {
                commit('setLastError', 'Test is already in progress', {root: true});
                return ''
            }
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async stop({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + '/__webapi__/tests/stop';
            const params = {
                method: 'POST',
                headers: {'Content-Type': 'text/text'},
                body: alias
            };
            const res = await fetch(url, params);
            if (res.status === 404) {
                commit('setLastError', 'Test not found or not run yet', {root: true});
                return ''
            }
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async result({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + `/__webapi__/tests/${alias}/result`;
            const res = await fetch(url, {method: 'GET'});
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    async clear({commit, rootState}, alias) {
        try {
            const url = rootState.BASE_URL + `/__webapi__/tests/${alias}/clear`;
            const res = await fetch(url, {method: 'POST'});
            if (res.status === 404) {
                commit('setLastError', 'Test not found or not run yet', {root: true});
                return ''
            }
            if (res.status === 202) {
                commit('setLastError', 'Test is in progress', {root: true});
                return ''
            }
            await handleError(res);
            return await res.text();
        } catch (err) {
            commit('setLastError', err, {root: true});
        }
    },
    add({commit}) {
        const entity = {
            group: '',
            alias: 'New Test',
            plan: '',
            _new: true,
            _selected: null,
        };
        commit('add', entity);
    },
    select({commit}, payload) {
        commit('select', payload);
    },
    selectAll({commit}, payload) {
        commit('selectAll', payload);
    },
};
