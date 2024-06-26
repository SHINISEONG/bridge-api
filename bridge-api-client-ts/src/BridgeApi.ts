import {ApiCommonRequest} from "./type/ApiCommonRequest";
import {MethodType} from "./enums/MethodType";

/**
 * Calls an Android function through the Bridge API.
 * @template ReqT The request type.
 * @template ResT The response type.
 * @param apiCommonRequest The API common request object.
 * @returns The response from the Android function.
 */
const callAndroidFunction = async <ReqT, ResT>(apiCommonRequest: ApiCommonRequest<ReqT>) => {
    const jsonString = JSON.stringify(apiCommonRequest);
    const result: string = await window.BridgeApi.bridgeRequest(jsonString);
    const response: ResT = JSON.parse(result);

    return response;
}

/**
 * BridgeApi class for making HTTP requests through the Bridge API.
 */
export class BridgeApi {
    /**
     * Makes a GET request.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.get<ResponseType>("/path?query=param");
     */
    static get<ResT>(pathAndQuery: string): Promise<ResT>;
    /**
     * Makes a GET request with a request body.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @param body The request body.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.get<ResponseType>("/path?query=param", requestBody);
     */
    static get<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static get<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.GET, body: body ?? ''});
    }

    /**
     * Makes a POST request.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.post<ResponseType>("/path");
     */
    static post<ResT>(pathAndQuery: string): Promise<ResT>;
    /**
     * Makes a POST request with a request body.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @param body The request body.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.post<ResponseType>("/path", requestBody);
     */
    static post<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static post<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.POST, body: body ?? ''});
    }

    /**
     * Makes a PUT request.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.put<ResponseType>("/path");
     */
    static put<ResT>(pathAndQuery: string): Promise<ResT>;
    /**
     * Makes a PUT request with a request body.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @param body The request body.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.put<ResponseType>("/path", requestBody);
     */
    static put<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static put<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.PUT, body: body ?? ''});
    }

    /**
     * Makes a DELETE request.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.delete<ResponseType>("/path");
     */
    static delete<ResT>(pathAndQuery: string): Promise<ResT>;
    /**
     * Makes a DELETE request with a request body.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @param body The request body.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.delete<ResponseType>("/path", requestBody);
     */
    static delete<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static delete<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.DELETE, body: body ?? ''});
    }

    /**
     * Makes a PATCH request.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.patch<ResponseType>("/path");
     */
    static patch<ResT>(pathAndQuery: string): Promise<ResT>;
    /**
     * Makes a PATCH request with a request body.
     * @template ResT The response type.
     * @param pathAndQuery The path and query string.
     * @param body The request body.
     * @returns A promise that resolves to the response.
     * @example
     * const response = await BridgeApi.patch<ResponseType>("/path", requestBody);
     */
    static patch<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static patch<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.PATCH, body: body ?? ''});
    }
}
