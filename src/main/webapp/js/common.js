export const handleError = async (response) => {
    if (response.status === 400) {
        const errorInfo = await response.json();
        throw new Error(errorInfo.message);
    }
    else if (!response.ok) {
        const err = response.statusText || response;
        throw new Error(err);
    }
    return response;
}