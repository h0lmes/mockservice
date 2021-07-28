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