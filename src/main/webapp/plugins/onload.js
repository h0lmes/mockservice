export default function ({ app, store }) {
    app.router.onReady(() => store.dispatch("routes/fetch"));
}