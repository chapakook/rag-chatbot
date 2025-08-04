package com.chapakook.lab.ragchatbot.support.error

import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: String, val message: String) {
    /** 범용 에러 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.reasonPhrase, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.reasonPhrase, "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.reasonPhrase, "이미 존재하는 리소스입니다."),

    /** OpenAI API 범용 에러 */
    OPENAI_API_UNEXPECTED_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, "OpenAI API로부터 예상치 못한 응답을 받았습니다."),

    /** OpenAI API 키 에러 */
    OPENAI_API_KEY_NOT_FOUND(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED.reasonPhrase, "OpenAI API 키가 설정되지 않았습니다."),
    OPENAI_API_KEY_INVALID(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED.reasonPhrase, "OpenAI API 키가 유효하지 않습니다."),
    OPENAI_API_KEY_FORBIDDEN(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN.reasonPhrase, "OpenAI API 키가 권한이 없습니다."),

    /** OpenAI API 요청 에러 */
    OPENAI_API_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS.reasonPhrase, "OpenAI API 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
    OPENAI_API_REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, HttpStatus.REQUEST_TIMEOUT.reasonPhrase, "OpenAI API 요청이 시간 초과되었습니다. 잠시 후 다시 시도해주세요."),
    OPENAI_API_REQUEST_TOO_LONG(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.reasonPhrase, "OpenAI API 요청이 너무 깁니다. 요청 길이를 줄여주세요."),
}
