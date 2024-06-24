import {ApiCommonRequest} from "./type/ApiCommonRequest";
import {MethodType} from "./enums/MethodType";

const callAndroidFunction = async <ReqT, ResT>(apiCommonRequest: ApiCommonRequest<ReqT>) => {
    const jsonString = JSON.stringify(apiCommonRequest);
    const result: string = await window.BridgeApi.bridgeRequest(jsonString);
    const response: ResT = JSON.parse(result);

    return response;
}

export class BridgeApi {
    static get<ResT>(pathAndQuery: string): Promise<ResT>;
    static get<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static get<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.GET, body: body ?? ''})
    }

    static post<ResT>(pathAndQuery: string): Promise<ResT>;
    static post<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static post<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.POST, body: body ?? ''})
    }

    static put<ResT>(pathAndQuery: string): Promise<ResT>;
    static put<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static put<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.PUT, body: body ?? ''})
    }

    static delete<ResT>(pathAndQuery: string): Promise<ResT>;
    static delete<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static delete<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.DELETE, body: body ?? ''})
    }

    static patch<ResT>(pathAndQuery: string): Promise<ResT>;
    static patch<ResT>(pathAndQuery: string, body: any): Promise<ResT>;

    static patch<ResT>(pathAndQuery: string, body?: any): Promise<ResT> {
        return callAndroidFunction({pathAndQuery: pathAndQuery, method: MethodType.PATCH, body: body ?? ''})
    }
}