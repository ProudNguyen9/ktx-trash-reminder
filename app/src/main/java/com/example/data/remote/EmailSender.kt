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
                
                <p>Bạn hãy giúp phòng xách túi rác bỏ đi nhé! Sau khi hoàn thành, hãy mở ứng dụng Dorm Trash Guard và nhấn <strong>Đã Đổ Rác</strong> để cập nhật lượt cho người kế tiếp.</p>
                
                <p style="font-size: 13px; color: #49454F; line-height: 1.5;">Email này chỉ dùng để nhắc nhở người đang tới lượt. Việc xác nhận đã đổ rác được thực hiện trong ứng dụng Android để tránh bấm nhầm hoặc xác nhận sai người.</p>
                
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
