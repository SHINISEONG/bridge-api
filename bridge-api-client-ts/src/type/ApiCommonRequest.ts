import {MethodType} from "../enums/MethodType";

export interface ApiCommonRequest<T> {
    pathAndQuery: string;
    method: MethodType;
    body: T;
}