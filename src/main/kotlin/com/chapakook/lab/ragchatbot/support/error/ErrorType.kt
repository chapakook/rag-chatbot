package com.chapakook.lab.ragchatbot.support.error

import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: String, val message: String) {
    /** 범용 에러 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.reasonPhrase, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.reasonPhrase, "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.reasonPhrase, "이미 존재하는 리소스입니다."),

    /** OpenAI API 범용 에러 */
    OPENAI_API_UNKNOWN_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
        "OpenAI API로부터 예상치 못한 응답을 받았습니다. 잠시후 다시 시도해주세요.",
    ),
    OPENAI_API_INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
        "OpenAI API의 서버 에러가 발생했습니다. 잠시후 다시 시도해주세요.",
    ),

    /** OpenAI API 키 에러 */
    OPENAI_API_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED.reasonPhrase, "OpenAI API 키가 설정되지 않았습니다."),
    OPENAI_API_KEY_INVALID(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED.reasonPhrase, "OpenAI API 키가 유효하지 않습니다."),
    OPENAI_API_KEY_FORBIDDEN(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN.reasonPhrase, "OpenAI API 키가 권한이 없습니다."),

    /** OpenAI API 요청 에러 */
    OPENAI_API_TOO_MANY_REQUESTS(
        HttpStatus.TOO_MANY_REQUESTS,
        HttpStatus.TOO_MANY_REQUESTS.reasonPhrase,
        "OpenAI API 요청이 너무 많습니다. 잠시후 다시 시도해주세요.",
    ),
    OPENAI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS.reasonPhrase, "OpenAI API 사용량을 초과했습니다."),
    OPENAI_API_REQUEST_TIMEOUT(
        HttpStatus.REQUEST_TIMEOUT,
        HttpStatus.REQUEST_TIMEOUT.reasonPhrase,
        "OpenAI API 요청이 시간 초과되었습니다. 잠시후 다시 시도해주세요.",
    ),
    OPENAI_CONTEXT_LENGTH_EXCEEDED(
        HttpStatus.BAD_REQUEST,
        HttpStatus.BAD_REQUEST.reasonPhrase,
        "OpenAI API 요청이 너무 깁니다. 요청 길이를 줄여주세요.",
    ),
    OPENAI_MODEL_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        HttpStatus.NOT_FOUND.reasonPhrase,
        "OpenAI API 해당모델을 찾을 수 없습니다. 요청항 모델명을 확인해주세요.",
    ),

    /** QDARNT 범용 에러 */
    QDRANT_INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
        "QDARNT 서버 에러가 발생했습니다. 잠시후 다시 시도해주세요.",
    ),
    QDRANT_BAD_REQUEST(
        HttpStatus.BAD_REQUEST,
        HttpStatus.BAD_REQUEST.reasonPhrase,
        "요청 파라메터의 오류가 있습니다. 확인 후 다시 시도해주세요.",
    ),
    QDRANT_TIMEOUT(
        HttpStatus.GATEWAY_TIMEOUT,
        HttpStatus.GATEWAY_TIMEOUT.reasonPhrase,
        "Qdrant 응답이 일정 시간 초과하였습니다. 잠시후 다시 시도해주세요.",
    ),
    QDRANT_UNKNOWN_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
        "QDARNT 서버로부터 예상치 못한 응답을 받았습니다. 잠시후 다시 시도해주세요.",
    ),
}
