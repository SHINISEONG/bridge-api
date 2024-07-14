package io.github.shiniseong.bridgeApi.util

fun generateResolveScript(promiseId: String, responseJsonString: String): String =
    "resolveAsyncPromise('$promiseId', '$responseJsonString')"

fun generateRejectScript(promiseId: String, errorJsonString: String): String =
    "rejectAsyncPromise('$promiseId', '$errorJsonString')"