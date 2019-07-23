package ai.alice.api.http

enum class Status(code: Int, description: String) {
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),

    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    I_M_A_TEAPOT(418, "I'm a teapot"), //RFC2324
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    TOO_MANY_REQUESTS(429, "Too many requests"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    CONNECTION_REFUSED(503, "Connection Refused"),
    UNKNOWN_HOST(503, "Unknown Host"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    CLIENT_TIMEOUT(504, "Client Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

    val code: Int = code
    var description: String = description
        private set

    companion object {
        private val INFORMATIONAL = 100..199
        private val SUCCESSFUL = 200..299
        private val REDIRECTION = 300..399
        private val CLIENT_ERROR = 400..499
        private val SERVER_ERROR = 500..599

        val UNSATISFIABLE_PARAMETERS = BAD_REQUEST.description("Unsatisfiable Parameters")

        @JvmStatic
        fun of(code: Int) = values().first { it.code == code }

        @JvmStatic
        fun of(code: Int, description: String) = of(code).description(description)
    }


    val successful by lazy { SUCCESSFUL.contains(code) }
    val informational by lazy { INFORMATIONAL.contains(code) }
    val redirection by lazy { REDIRECTION.contains(code) }
    val clientError by lazy { CLIENT_ERROR.contains(code) }
    val serverError by lazy { SERVER_ERROR.contains(code) }

    fun description(newDescription: String) = apply {
        description = newDescription
    }

    override fun toString(): String = "$code $description"
}
