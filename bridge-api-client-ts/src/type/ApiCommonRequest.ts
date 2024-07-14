import {MethodType} from "../enums/MethodType";

export interface ApiCommonRequest<T> {
    pathAndQuery: string;
    method: MethodType;
    headers: { [key: string]: string };
    body: T;
}