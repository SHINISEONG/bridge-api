import {ApiCommonRequest} from "./type/ApiCommonRequest";
import {MethodType} from "./enums/MethodType";

type PromiseMapType = Map<string, { resolve: (result: any) => void; reject: (reason?: any) => void }>;
const promiseMap: PromiseMapType = new Map();

/**
 * Generates a unique callback ID using the current timestamp and a random number.
 *
 * @returns {string} Unique callback ID.
 */
const generateUniqueCallbackId = (): string => {
    // Get the current time in milliseconds and convert to a hexadecimal string
    const timestamp = Date.now().toString(16);
    // Generate a random number and convert to a hexadecimal string
    const random = Math.floor(Math.random() * 0xffff).toString(16);
    // Combine the two values to create a unique ID
    return `promise_${timestamp}_${random}`;
}

/**
 * Calls the Bridge API function and returns a promise.
 *
 * @param {ApiCommonRequest<ReqT>} apiCommonRequest - The API request object.
 * @param {number} [timeout=5000] - The timeout period in milliseconds.
 * @returns {Promise<ResT>} - A promise that resolves with the API response.
 */
const callBridgeApiFunction = async <ReqT, ResT>(
    apiCommonRequest: ApiCommonRequest<ReqT>,
    timeout: number = 5000,
): Promise<ResT> => {
    const promiseId = generateUniqueCallbackId();

    return new Promise<ResT>((resolve, reject) => {
        const timeoutId = setTimeout(() => {
            promiseMap.delete(promiseId);
            reject(new Error(`Timeout after ${timeout}ms for request: ${apiCommonRequest.body}`));
        }, timeout);

        try {
            const jsonString = JSON.stringify(apiCommonRequest);
            window.BridgeApi.bridgeRequest(promiseId, jsonString);
            promiseMap.set(promiseId, {
                resolve: (result: ResT) => {
                    clearTimeout(timeoutId);
                    resolve(result);
                },
                reject: (e: any) => {
                    clearTimeout(timeoutId);
                    reject(e);
                }
            });
        } catch (error) {
            clearTimeout(timeoutId);
            promiseMap.delete(promiseId);
            console.error(`Error calling bridge api(request: ${apiCommonRequest}):`, error);
            reject(new Error(`Failed to call bridge api(request: ${apiCommonRequest.body})`));
        }
    });
}

/**
 * Resolves the asynchronous promise with the given ID and result.
 *
 * @param {string} id - The unique callback ID.
 * @param {any} result - The result to resolve the promise with.
 */
window.resolveAsyncPromise = (id: string, result: any) => {
    if (!promiseMap.has(id)) {
        console.warn(`Promise with ID ${id} not found in promiseMap on executing callAndroidAsyncBridgeFunction`);
        return;
    }
    const {resolve} = promiseMap.get(id)!;
    const resultJson = JSON.parse(result);
    resolve(resultJson);
    promiseMap.delete(id);
};

/**
 * Rejects the asynchronous promise with the given ID and error.
 *
 * @param {string} id - The unique callback ID.
 * @param {any} error - The error to reject the promise with.
 */
window.rejectAsyncPromise = (id: string, error: any) => {
    if (!promiseMap.has(id)) {
        console.warn(`Promise with ID ${id} not found in promiseMap on executing callAndroidAsyncBridgeFunction`);
        return;
    }
    const {reject} = promiseMap.get(id)!;
    reject(JSON.parse(error));
    promiseMap.delete(id);
};

interface BridgeApiConfig {
    headers: { [key: string]: string },
    timeout: number,
}

export class BridgeApi {
    config: BridgeApiConfig

    private constructor(config?: BridgeApiConfig) {
        this.config = config ?? {headers: {}, timeout: 5000};
    }

    /**
     * Creates a new instance of BridgeApi.
     *
     * @param {BridgeApiConfig} [config] - Optional configuration for the API.
     * @returns {BridgeApi} - A new instance of BridgeApi.
     */
    static create(config?: BridgeApiConfig): BridgeApi {
        return new BridgeApi(config);
    }

    /**
     * Sends a GET request to the specified path and query.
     *
     * @param {string} pathAndQuery - The path and query string for the GET request.
     * @param {any} [body=''] - The body of the request.
     * @param {number} [timeout=this.config.timeout] - The timeout period in milliseconds.
     * @returns {Promise<ResT>} - A promise that resolves with the response.
     */
    get<ResT>(
        pathAndQuery: string, body: any = '',
        timeout: number = this.config.timeout
    ): Promise<ResT> {
        return callBridgeApiFunction({
                pathAndQuery: pathAndQuery,
                method: MethodType.GET,
                headers: this.config.headers,
                body: body
            },
            timeout ?? this.config.timeout,
        )
    }

    /**
     * Sends a POST request to the specified path and query.
     *
     * @param {string} pathAndQuery - The path and query string for the POST request.
     * @param {any} [body=''] - The body of the request.
     * @param {number} [timeout=this.config.timeout] - The timeout period in milliseconds.
     * @returns {Promise<ResT>} - A promise that resolves with the response.
     */
    post<ResT>(
        pathAndQuery: string,
        body: any = '',
        timeout: number = this.config.timeout
    ): Promise<ResT> {
        return callBridgeApiFunction({
                pathAndQuery: pathAndQuery,
                method: MethodType.POST,
                headers: this.config.headers,
                body: body,
            },
            timeout,
        )
    }

    /**
     * Sends a PUT request to the specified path and query.
     *
     * @param {string} pathAndQuery - The path and query string for the PUT request.
     * @param {any} [body=''] - The body of the request.
     * @param {number} [timeout=this.config.timeout] - The timeout period in milliseconds.
     * @returns {Promise<{ id: string, result: ResT }>} - A promise that resolves with the response.
     */
    put<ResT>(
        pathAndQuery: string,
        body: any = '',
        timeout: number = this.config.timeout
    ): Promise<{ id: string, result: ResT }> {
        return callBridgeApiFunction({
                pathAndQuery: pathAndQuery,
                method: MethodType.PUT,
                headers: this.config.headers,
                body: body
            },
            timeout
        )
    }

    /**
     * Sends a DELETE request to the specified path and query.
     *
     * @param {string} pathAndQuery - The path and query string for the DELETE request.
     * @param {any} [body=''] - The body of the request.
     * @param {number} [timeout=this.config.timeout] - The timeout period in milliseconds.
     * @returns {Promise<{ id: string, result: ResT }>} - A promise that resolves with the response.
     */
    delete<ResT>(
        pathAndQuery: string,
        body: any = '',
        timeout: number = this.config.timeout
    ): Promise<{ id: string, result: ResT }> {
        return callBridgeApiFunction({
                pathAndQuery: pathAndQuery,
                method: MethodType.DELETE,
                headers: this.config.headers,
                body: body
            },
            timeout
        )
    }

    /**
     * Sends a PATCH request to the specified path and query.
     *
     * @param {string} pathAndQuery - The path and query string for the PATCH request.
     * @param {any} [body=''] - The body of the request.
     * @param {number} [timeout=this.config.timeout] - The timeout period in milliseconds.
     * @returns {Promise<{ id: string, result: ResT }>} - A promise that resolves with the response.
     */
    patch<ResT>(
        pathAndQuery: string,
        body: any = '',
        timeout: number = this.config.timeout
    ): Promise<{ id: string, result: ResT }> {
        return callBridgeApiFunction({
                pathAndQuery: pathAndQuery,
                method: MethodType.PATCH,
                headers: this.config.headers,
                body: body
            },
            timeout
        )
    }
}
