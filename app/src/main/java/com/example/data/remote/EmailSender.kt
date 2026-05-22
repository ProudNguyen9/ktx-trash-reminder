package com.example.data.remote

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailSender {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    data class ResendPayload(
        val from: String,
        val to: List<String>,
        val subject: String,
        val html: String
    )

    suspend fun sendEmailViaResend(
        apiKey: String,
        toEmail: String,
        recipientName: String,
        sequenceId: Int,
        webConfirmUrl: String = "",
        firebaseDbUrl: String = "",
        firebaseApiKey: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || toEmail.isBlank()) return@withContext false

        var confirmButtonHtml = ""
        if (webConfirmUrl.isNotBlank() && firebaseDbUrl.isNotBlank()) {
            try {
                val dbUrlEncoded = java.net.URLEncoder.encode(firebaseDbUrl, "UTF-8")
                val apiKeyEncoded = java.net.URLEncoder.encode(firebaseApiKey, "UTF-8")
                val confirmUrl = "$webConfirmUrl?dbUrl=$dbUrlEncoded&apiKey=$apiKeyEncoded"
                
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
                    <p style="color: #49454F; font-size: 14px; margin-top: 0;">Dorm Trash Guard App • KTX 7 Thành Viên</p>
                </div>
                
                <p>Chào <strong>$recipientName</strong>,</p>
                <p>Một thành viên trong phòng vừa gửi báo cáo: <strong>thùng rác sinh hoạt chung của phòng đã đầy rồi!</strong></p>
                
                <div style="background-color: #F9DEDC; border-left: 4px solid #B3261E; padding: 16px; margin: 20px 0; border-radius: 12px;">
                    <p style="margin: 0; font-size: 15px; color: #410E0B; font-weight: bold;">
                        👉 Hiện tại đang đến lượt của bạn (Thứ tự quy định: #${sequenceId} trong vòng lặp 1-7).
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

        val payload = ResendPayload(
            from = "Dorm Trash Guard <onboarding@resend.dev>",
            to = listOf(toEmail),
            subject = subject,
            html = htmlContent
        )

        return@withContext try {
            val adapter = moshi.adapter(ResendPayload::class.java)
            val json = adapter.toJson(payload)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            
            val request = Request.Builder()
                .url("https://api.resend.com/emails")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("EmailSender", "Email sent successfully via Resend API")
                    true
                } else {
                    val errorBody = response.body?.string() ?: ""
                    Log.e("EmailSender", "Resend API error: ${response.code} $errorBody")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("EmailSender", "Error sending email via Resend API", e)
            false
        }
    }
}
