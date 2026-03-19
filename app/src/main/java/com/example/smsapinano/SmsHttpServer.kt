package com.example.smsapinano

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

class SmsHttpServer(port: Int = 8080, private val context: Context) : NanoHTTPD(port) {

    // ─── Helper: agrega headers CORS a cualquier Response ──────────────────────
    private fun addCorsHeaders(response: Response): Response {
        response.addHeader("Access-Control-Allow-Origin",  "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        response.addHeader("Access-Control-Max-Age",       "86400")
        return response
    }

    override fun serve(session: IHTTPSession): Response {
        val uri    = session.uri
        val method = session.method

        // ─── Preflight OPTIONS (el browser lo manda antes de cada POST) ─────────
        // Sin esto, el browser bloquea la petición antes de que llegue el POST real.
        if (method == Method.OPTIONS) {
            return addCorsHeaders(
                newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "")
            )
        }

        if (method == Method.POST && uri == "/send") {
            val clientIp = session.remoteIpAddress ?: "unknown"

            // ── Verificar IP permitida ─────────────────────────────────────────
            val prefs      = context.getSharedPreferences("IpConfig", Context.MODE_PRIVATE)
            val allowedIps = prefs.getStringSet("allowed_ips", setOf("127.0.0.1", "::1")) ?: setOf()

            if (clientIp !in allowedIps) {
                Log.w("SmsHttpServer", "Intento denegado desde IP: $clientIp")
                return addCorsHeaders(
                    newFixedLengthResponse(
                        Response.Status.FORBIDDEN,
                        MIME_PLAINTEXT,
                        "Acceso denegado: IP no autorizada ($clientIp)"
                    )
                )
            }

            var phone = "unknown"
            var text  = "unknown"

            try {
                val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0

                if (contentLength <= 0) {
                    return addCorsHeaders(
                        newFixedLengthResponse(
                            Response.Status.BAD_REQUEST,
                            MIME_PLAINTEXT,
                            "Content-Length requerido o body vacío"
                        )
                    )
                }

                // ── Leer body ─────────────────────────────────────────────────
                val buffer = ByteArray(contentLength)
                var totalRead = 0
                while (totalRead < contentLength) {
                    val read = session.inputStream.read(buffer, totalRead, contentLength - totalRead)
                    if (read == -1) break
                    totalRead += read
                }

                val body = String(buffer, 0, totalRead, Charsets.UTF_8)

                if (body.isBlank()) {
                    return addCorsHeaders(
                        newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Body vacío")
                    )
                }

                val json = JSONObject(body)
                phone = json.optString("to",   "").trim()
                text  = json.optString("text", "").trim()

                if (phone.isEmpty() || text.isEmpty()) {
                    return addCorsHeaders(
                        newFixedLengthResponse(
                            Response.Status.BAD_REQUEST,
                            MIME_PLAINTEXT,
                            "Faltan \"to\" o \"text\""
                        )
                    )
                }

                text = text.replace("\r\n", "\n").replace("\r", "\n")

                // ── Enviar SMS ────────────────────────────────────────────────
                val smsManager = SmsManager.getDefault()
                val parts      = smsManager.divideMessage(text)


                smsManager.sendMultipartTextMessage(phone, null, parts, null, null)

                RequestLog.add(SmsRequest(ip = clientIp, to = phone, text = text, success = true))

                return addCorsHeaders(
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        """{"status":"success","message":"SMS enviado a $phone"}"""
                    )
                )

            } catch (e: org.json.JSONException) {
                RequestLog.add(SmsRequest(ip = clientIp, to = phone, text = text,
                    success = false, errorMessage = "JSON inválido: ${e.message}"))
                return addCorsHeaders(
                    newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        MIME_PLAINTEXT,
                        "JSON inválido: ${e.message}"
                    )
                )

            } catch (e: Exception) {
                RequestLog.add(SmsRequest(ip = clientIp, to = phone, text = text,
                    success = false, errorMessage = e.message ?: e.javaClass.simpleName))
                Log.e("SmsHttpServer", "Error enviando SMS", e)
                return addCorsHeaders(
                    newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        "application/json",
                        """{"status":"error","message":"${e.message ?: "Error desconocido"}"}"""
                    )
                )
            }
        }

        // ─── Endpoint no encontrado ─────────────────────────────────────────────
        return addCorsHeaders(
            newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "application/json",
                """{"status":"error","message":"Endpoint no encontrado. Usa POST /send con JSON: {\"to\":\"numero\",\"text\":\"mensaje\"}"}"""
            )
        )
    }
}