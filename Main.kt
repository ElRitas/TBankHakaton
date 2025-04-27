import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerializationException
import java.util.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            anyHost()
            allowCredentials = true
            allowNonSimpleContentTypes = true
            methods.addAll(HttpMethod.DefaultMethods)
        }
        install(StatusPages) {
            exception<SerializationException> { call, _ ->
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "Invalid JSON format or wrong UUID", code = 400)
                )
            }
            exception<IllegalArgumentException> { call, cause ->
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = cause.message ?: "Invalid request", code = 400)
                )
            }
            exception<Throwable> { call, _ ->
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Something went wrong", code = 500)
                )
            }
        }
        install(HttpsRedirect) {
            sslPort = 8443
            permanentRedirect = true
        }

        routing {
            post("/payment") {
                val request = call.receive<PaymentRequest>()

                try {
                    UUID.fromString(request.paymentId)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid UUID format: ${request.paymentId}")
                }

                if (request.deviceOS !in listOf("Android", "iOS", "Desktop")) {
                    throw IllegalArgumentException("Unsupported deviceOS: ${request.deviceOS}")
                }

                val paymentLink = generatePaymentLink(request)


                call.respond(
                    HttpStatusCode.OK,
                    PaymentResponse(
                        link = paymentLink,
                        status = HttpStatusCode.OK.value
                    )
                )
            }
        }
    }.start(wait = true)
}

private fun generatePaymentLink(request: PaymentRequest): String {
    return when (request.deviceOS) {
        "Android" -> "tinkoffbank://Main/tpay/${request.paymentId}"
        "iOS" -> {
            if (request.webview) {
                "https://www.tinkoff.ru/tpay/${request.paymentId}"
            } else {
                "bank100000000004://Main/tpay/${request.paymentId}"
            }
        }
        "Desktop" -> "https://www.tinkoff.ru/tpay/${request.paymentId}"
        else -> throw IllegalArgumentException("Unsupported deviceOS: ${request.deviceOS}")
    }
}


@Serializable
data class PaymentRequest(
    @Contextual val paymentId: String,
    val deviceOS: String,
    val webview: Boolean
)

@Serializable
data class PaymentResponse(
    val link: String,
    val status: Int
)

@Serializable
data class ErrorResponse(
    val error: String,
    val code: Int
)
