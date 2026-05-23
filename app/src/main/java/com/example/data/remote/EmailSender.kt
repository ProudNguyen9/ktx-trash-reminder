package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {

    suspend fun sendEmailViaGmail(
        senderEmail: String,
        senderPassword: String,
        toEmail: String,
        recipientName: String,
        sequenceId: Int,
        roomName: String = "",
        webConfirmUrl: String = "",
        firebaseDbUrl: String = "",
        firebaseApiKey: String = "",
        confirmToken: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        if (senderEmail.isBlank() || senderPassword.isBlank() || toEmail.isBlank()) {
            Log.e("EmailSender", "Cannot send email: Missing sender or recipient details.")
            return@withContext false
        }

        fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
        fun String.htmlEscape(): String = this
            .replace("&", "&#38;")
            .replace("\"", "&#34;")
            .replace("<", "&#60;")
            .replace(">", "&#62;")

        val sentAtMillis = System.currentTimeMillis()
        val sentAtText = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date(sentAtMillis))
        val sentAtSubjectText = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(sentAtMillis))
        val mailRunId = sentAtMillis.toString().takeLast(6)

        val normalizedConfirmBaseUrl = webConfirmUrl.trim()
            .removePrefix("WEB_CONFIRM_URL=")
        val confirmUrl = if (normalizedConfirmBaseUrl.isNotBlank()) {
            val separator = if (normalizedConfirmBaseUrl.contains("?")) "&" else "?"
            normalizedConfirmBaseUrl + separator + listOf(
                "roomName=${roomName.urlEncode()}",
                "email=${toEmail.trim().lowercase().urlEncode()}",
                "name=${recipientName.urlEncode()}",
                "sequenceId=$sequenceId",
                "token=${confirmToken.urlEncode()}",
                "firebaseDbUrl=${firebaseDbUrl.urlEncode()}",
                "firebaseApiKey=${firebaseApiKey.urlEncode()}"
            ).joinToString("&")
        } else {
            ""
        }

        val confirmButtonHtml = if (confirmUrl.isNotBlank()) {
            val confirmUrlForHtml = confirmUrl.htmlEscape()
            """
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin: 28px auto;">
                    <tr>
                        <td align="center" bgcolor="#0B57D0" style="border-radius: 999px; background-color: #0B57D0;">
                            <a href="$confirmUrlForHtml" target="_blank" rel="noopener" style="display: block; background-color: #0B57D0; color: #FFFFFF; text-decoration: none; padding: 14px 22px; border-radius: 999px; font-size: 15px; font-weight: 700; font-family: Arial, sans-serif;">
                                ✅ Xác nhận đã đổ rác
                            </a>
                        </td>
                    </tr>
                </table>
                <div style="text-align: center; margin: -18px 0 24px;">
                    <p style="font-size: 12px; color: #49454F; margin-top: 10px; line-height: 1.4;">
                        Chỉ bấm nút này sau khi bạn đã mang rác đi đổ xong.
                    </p>
                    <p style="font-size: 11px; color: #79747E; line-height: 1.5; word-break: break-all;">
                        Nếu nút không bấm được, hãy mở link này:<br/>
                        <a href="$confirmUrlForHtml" target="_blank" rel="noopener" style="color: #0B57D0; text-decoration: underline;">$confirmUrlForHtml</a>
                    </p>
                </div>
            """.trimIndent()
        } else {
            """
                <div style="background-color: #FFF8E1; border-left: 4px solid #F9AB00; padding: 14px; margin: 22px 0; border-radius: 12px;">
                    <p style="margin: 0; font-size: 13px; color: #3C4043; line-height: 1.5;">
                        Chưa cấu hình link xác nhận nhanh, vui lòng mở ứng dụng Dorm Trash Guard và nhấn <strong>Đã Đổ Rác</strong> sau khi hoàn thành.
                    </p>
                </div>
            """.trimIndent()
        }

        val subject = "⚠️ [Dorm Trash Guard] Rác đầy lúc $sentAtSubjectText • Mã #$mailRunId"
        val htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 16px; background-color: #FEF7FF; color: #1D1B20;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <span style="font-size: 40px;">🗑️</span>
                    <h2 style="color: #B3261E; margin-top: 10px; font-weight: 800; letter-spacing: -0.5px;">🚨 CẢNH BÁO RÁC ĐẦY!</h2>
                    <p style="color: #49454F; font-size: 14px; margin-top: 0;">Dorm Trash Guard App • Gửi lúc $sentAtText • Mã #$mailRunId</p>
                </div>
                
                <p>Chào <strong>$recipientName</strong>,</p>
                <p>Một thành viên trong phòng vừa gửi báo cáo lúc <strong>$sentAtText</strong>: <strong>thùng rác sinh hoạt chung của phòng đã đầy rồi!</strong></p>
                
                <div style="background-color: #F9DEDC; border-left: 4px solid #B3261E; padding: 16px; margin: 20px 0; border-radius: 12px;">
                    <p style="margin: 0; font-size: 15px; color: #410E0B; font-weight: bold;">
                        👉 Hiện tại đang đến lượt của bạn (Thứ tự quy định: #$sequenceId trong vòng lặp thành viên).
                    </p>
                </div>
                
                <p>Bạn hãy giúp phòng xách túi rác bỏ đi nhé! Sau khi hoàn thành, hãy nhấn nút bên dưới để cập nhật lượt cho người kế tiếp.</p>

                $confirmButtonHtml
                
                <p style="font-size: 13px; color: #49454F; line-height: 1.5;">Email này chỉ dùng để nhắc nhở người đang tới lượt. Vui lòng chỉ xác nhận khi bạn là người nhận email và đã hoàn tất việc đổ rác.</p>
                
                <p style="margin-top: 40px; font-size: 11px; color: #79747E; text-align: center; border-top: 1px solid #CAC4D0; padding-top: 20px; line-height: 1.4;">
                    Hệ thống email tự động vận hành bởi ứng dụng <strong>Dorm Trash Guard</strong> • Mail ID #$mailRunId.<br/>
                    <em>Đảm bảo sạch sẽ, công bằng và gắn kết thành viên phòng KTX.</em>
                </p>
            </div>
        """.trimIndent()

        try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
                put("mail.smtp.connectiontimeout", "12000")
                put("mail.smtp.timeout", "12000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail, "Dorm_trash_guard"))
                addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                setSubject(subject)
                setContent(htmlContent, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.d("EmailSender", "Email sent successfully via Gmail SMTP to $toEmail")
            true
        } catch (e: Exception) {
            Log.e("EmailSender", "Failed to send email via Gmail SMTP: ${e.message}", e)
            false
        }
    }
}
