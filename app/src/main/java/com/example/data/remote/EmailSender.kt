package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        webConfirmUrl: String = "",
        firebaseDbUrl: String = "",
        firebaseApiKey: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        if (senderEmail.isBlank() || senderPassword.isBlank() || toEmail.isBlank()) {
            Log.e("EmailSender", "Cannot send email: Missing sender or recipient details.")
            return@withContext false
        }

        var confirmButtonHtml = ""
        if (webConfirmUrl.isNotBlank() && firebaseDbUrl.isNotBlank()) {
            try {
                val dbUrlEncoded = java.net.URLEncoder.encode(firebaseDbUrl, "UTF-8")
                val apiKeyEncoded = java.net.URLEncoder.encode(firebaseApiKey, "UTF-8")
                
                // Normalize webConfirmUrl to include web-confirm/index.html if not specified
                val finalWebConfirmUrl = when {
                    webConfirmUrl.contains("/web-confirm/") -> webConfirmUrl
                    webConfirmUrl.endsWith("/") -> "${webConfirmUrl}web-confirm/index.html"
                    else -> "$webConfirmUrl/web-confirm/index.html"
                }
                val confirmUrl = "$finalWebConfirmUrl?dbUrl=$dbUrlEncoded&apiKey=$apiKeyEncoded"
                
                confirmButtonHtml = """
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="$confirmUrl" style="background-color: #6750A4; color: #FFFFFF; padding: 16px 32px; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 16px; display: inline-block; box-shadow: 0 4px 12px rgba(103, 80, 164, 0.3);">
                            ĐÃ ĐỒ RÁC (XÁC NHẬN NHANH) ✅
                        </a>
                        <p style="margin-top: 10px; font-size: 11px; color: #6b7280;">
                            *Dành cho thành viên không có cài đặt ứng dụng. Hãy nhấn sau khi đã đổ rác xong.
                        </p>
                    </div>
                """.trimIndent()
            } catch (e: Exception) {
                Log.e("EmailSender", "Failed to encode DB parameters for confirm link", e)
            }
        }

        val subject = "⚠️ [Dorm Trash Guard] THÙNG RÁC ĐẦY RỒI! Đến lượt bạn đổ rác!"
        val htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 16px; background-color: #FEF7FF; color: #1D1B20;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <span style="font-size: 40px;">🗑️</span>
                    <h2 style="color: #B3261E; margin-top: 10px; font-weight: 800; letter-spacing: -0.5px;">🚨 CẢNH BÁO RÁC ĐẦY!</h2>
                    <p style="color: #49454F; font-size: 14px; margin-top: 0;">Dorm Trash Guard App • Đội đổ rác KTX</p>
                </div>
                
                <p>Chào <strong>$recipientName</strong>,</p>
                <p>Một thành viên trong phòng vừa gửi báo cáo: <strong>thùng rác sinh hoạt chung của phòng đã đầy rồi!</strong></p>
                
                <div style="background-color: #F9DEDC; border-left: 4px solid #B3261E; padding: 16px; margin: 20px 0; border-radius: 12px;">
                    <p style="margin: 0; font-size: 15px; color: #410E0B; font-weight: bold;">
                        👉 Hiện tại đang đến lượt của bạn (Thứ tự quy định: #${sequenceId} trong vòng lặp thành viên).
                    </p>
                </div>
                
                <p>Bạn hãy giúp phòng xách túi rác bỏ đi nhé! Sau khi hoàn thành, bạn có thể nhấn nút <strong>Xóa rác nhanh</strong> bên dưới ngay trong email này để tự động cập nhật và chuyển lượt đổ cho người kế tiếp mà không cần cài app.</p>
                
                $confirmButtonHtml
                
                <p style="font-size: 13px; color: #49454F; line-height: 1.5;">Nếu có ứng dụng Android cài trên điện thoại, bạn cũng có thể mở app lên để theo dõi lịch sử sinh hoạt của phòng hoặc cập nhật lại danh sách email thành viên.</p>
                
                <p style="margin-top: 40px; font-size: 11px; color: #79747E; text-align: center; border-top: 1px solid #CAC4D0; padding-top: 20px; line-height: 1.4;">
                    Hệ thống email tự động vận hành bởi ứng dụng <strong>Dorm Trash Guard</strong>.<br/>
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
                setFrom(InternetAddress(senderEmail, "Dorm Trash Guard"))
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
