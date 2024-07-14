/// <reference types="vite/client" />
interface Window {
    BridgeApi: {
        // eslint-disable-next-line @typescript-eslint/ban-types
        bridgeRequest: Function
    }
    resolveAsyncPromise: Function,
    rejectAsyncPromise: Function
}
