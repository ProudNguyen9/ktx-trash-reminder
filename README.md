# Dorm Trash Guard

Dorm Trash Guard là ứng dụng Android quản lý lượt đổ rác cho phòng ký túc xá. Ứng dụng giúp các thành viên theo dõi lượt trực, báo rác đầy, gửi email nhắc việc, xác nhận đã đổ rác qua nút trong email và đồng bộ trạng thái theo thời gian gần realtime bằng Firebase Realtime Database.

## Tính năng chính

- Quản lý nhiều phòng ký túc xá.
- Đăng nhập theo email và mật khẩu/secret của từng thành viên.
- Phân quyền admin và user:
  - Admin có thể cấu hình phòng, email, Firebase, tài khoản admin và chỉnh sửa danh sách thành viên.
  - User có thể xem trạng thái, báo rác đầy, xác nhận đã đổ rác và xem danh sách thành viên ở chế độ chỉ đọc.
- Luân phiên lượt đổ rác tự động theo danh sách thành viên.
- Hỗ trợ đánh dấu thành viên vắng mặt để bỏ qua lượt phù hợp.
- Báo rác đầy và gửi email nhắc đến đúng thành viên đang tới lượt.
- Nút xác nhận trong email thông qua Firebase Hosting.
- Xác thực xác nhận bằng token, email và thành viên đang tới lượt.
- Đồng bộ Firebase Realtime Database bằng REST API.
- App tự polling Firebase để cập nhật trạng thái gần realtime.
- Ghi lịch sử các thao tác quan trọng như báo rác đầy, xác nhận đã đổ rác, chỉnh thứ tự thành viên.
- Giao diện Jetpack Compose hiện đại, nền trắng, responsive cho nhiều kích thước màn hình.

## Công nghệ sử dụng

- Kotlin
- Jetpack Compose
- Material 3
- Room Database
- Kotlin Coroutines và Flow
- OkHttp
- Moshi
- JavaMail/Gmail SMTP
- Firebase Realtime Database
- Firebase Hosting
- Gradle Kotlin DSL

## Cấu trúc nổi bật

```text
.
├── app/
│   └── src/main/java/com/example/
│       ├── data/
│       │   ├── local/          # Room database, DAO
│       │   ├── model/          # Entity/model
│       │   ├── remote/         # Firebase REST client, email sender
│       │   └── repository/     # Business logic
│       └── ui/
│           ├── screens/        # Compose screens/tabs/dialogs
│           ├── theme/          # Theme, color, typography, shape
│           └── viewmodel/      # TrashViewModel
├── web-confirm/                # Firebase Hosting page xác nhận từ email
├── firebase.json               # Firebase Hosting config
├── .env.example                # Mẫu biến cấu hình local
└── README.md
```

## Yêu cầu môi trường

- Android Studio mới nhất.
- JDK phù hợp với Android Gradle Plugin trong dự án.
- Thiết bị Android hoặc emulator.
- Firebase project có Realtime Database và Hosting.
- Gmail App Password nếu dùng Gmail SMTP.
- Firebase CLI nếu muốn deploy trang xác nhận web.

## Cài đặt local

1. Clone repository:

```bash
git clone https://github.com/ProudNguyen9/ktx-trash-reminder.git
cd ktx-trash-reminder
```

2. Tạo file `.env` từ `.env.example`:

```bash
cp .env.example .env
```

Trên Windows có thể dùng:

```cmd
copy .env.example .env
```

3. Cập nhật các biến trong `.env`:

```env
FIREBASE_DB_URL=https://your-project-default-rtdb.asia-southeast1.firebasedatabase.app/
FIREBASE_API_KEY=your_firebase_web_api_key
FIREBASE_PROJECT_ID=your_firebase_project_id

SENDER_GMAIL_ADDRESS=your_sender@gmail.com
SENDER_GMAIL_PASSWORD=your_gmail_app_password

WEB_CONFIRM_URL=https://your-project.web.app/confirm-trash
```

> Không commit file `.env` vì file này chứa thông tin nhạy cảm.

4. Mở dự án bằng Android Studio và chạy app trên emulator hoặc điện thoại thật.

## Build bằng terminal

Trên Windows:

```cmd
gradlew.bat assembleDebug
```

Trên macOS/Linux:

```bash
./gradlew assembleDebug
```

APK debug sẽ nằm trong:

```text
app/build/outputs/apk/debug/
```

## Thiết lập Firebase

### 1. Realtime Database

- Tạo Firebase project.
- Bật Realtime Database.
- Lấy Database URL và Web API Key.
- Điền vào `.env` hoặc cấu hình trong tab Cấu hình của app.

### 2. Firebase Hosting cho nút xác nhận email

Trang xác nhận nằm trong thư mục `web-confirm/` và được cấu hình bởi `firebase.json`.

Đăng nhập Firebase CLI:

```cmd
firebase login
```

Chọn project hoặc deploy trực tiếp bằng project id:

```cmd
firebase use --add
```

Deploy Hosting:

```cmd
firebase deploy --only hosting --project your_firebase_project_id
```

Sau khi deploy, đặt biến:

```env
WEB_CONFIRM_URL=https://your-project.web.app/confirm-trash
```

## Luồng xác nhận qua email

1. Một thành viên báo rác đầy trong app.
2. App lưu trạng thái rác đầy và tạo `confirmToken` mới.
3. App gửi email đến thành viên đang tới lượt đổ rác.
4. Email chứa nút xác nhận trỏ đến Firebase Hosting.
5. Trang web xác nhận đọc trạng thái từ Firebase và kiểm tra:
   - token hợp lệ,
   - email đúng người,
   - thành viên đang tới lượt,
   - trạng thái rác vẫn đang đầy.
6. Nếu hợp lệ, trang web cập nhật Firebase: tắt cảnh báo rác đầy, xóa token và chuyển lượt tiếp theo.
7. App Android polling Firebase để cập nhật lại UI gần realtime.

## Ghi chú bảo mật

- Không commit `.env`, mật khẩu Gmail, token, keystore riêng tư hoặc thông tin nhạy cảm.
- Nên dùng Gmail App Password thay vì mật khẩu tài khoản Gmail chính.
- Firebase Database Rules cần được cấu hình phù hợp trước khi dùng thật.
- Link xác nhận email có chứa token dùng một lần cho từng lần báo rác đầy.

## Lệnh Git thường dùng

```bash
git status
git add .
git commit -m "Update Dorm Trash Guard"
git push origin main
```

## Trạng thái dự án

Dự án hiện là app Android native, không phải cross-platform. App tập trung vào quản lý lượt đổ rác phòng ký túc xá với email confirmation và Firebase sync.
