export const handleError = async (response) => {
    if (response.status === 400) {
        const err = await response.json();
        throw new Error(err.message || JSON.stringify(err));
    }
    else if (!response.ok) {
        const err = response.statusText || response.error || response.message || JSON.stringify(response);
        throw new Error(err);
    }
    return response;
}

export const addSelectedProperty = (data) => {
    if (Array.isArray(data)) {
        for (let i = 0; i < data.length; i++) {
            data[i]._selected = null;
        }
    }
}

export const selectAll = (data, selected) => {
    if (Array.isArray(data)) {
        for (let i = 0; i < data.length; i++) {
            data[i]._selected = selected;
        }
    }
}

export const _isRoute = (entity) => entity.hasOwnProperty('alt');
export const _isRequest = (entity) => entity.hasOwnProperty('id');
export const _isScenario = (entity) => entity.hasOwnProperty('data');
export const _isTest = (entity) => entity.hasOwnProperty('plan');
