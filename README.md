# CAMERA BẢO MẬT - ỨNG DỤNG ANDROID

## Giới thiệu

Camera Bảo Mật là một ứng dụng chụp ảnh Android được phát triển nhằm cung cấp giải pháp bảo mật cho việc lưu trữ hình ảnh. Ứng dụng này cho phép người dùng chụp ảnh, lưu trữ an toàn và tùy chọn mã hóa các hình ảnh. Việc sử dụng API CameraX hiện đại giúp ứng dụng tương thích với nhiều thiết bị Android và cung cấp trải nghiệm chụp ảnh chất lượng cao.

## Tính năng chính

- Chụp ảnh với giao diện người dùng thân thiện và trực quan
- Mã hóa ảnh sử dụng thuật toán AES-256-GCM để bảo vệ quyền riêng tư
- Điều chỉnh chất lượng ảnh (1080p, 2K, 4K, hoặc chất lượng tối đa)
- Hỗ trợ camera trước và sau với chuyển đổi nhanh chóng
- Điều khiển đèn flash với ba chế độ (tắt, bật, tự động)
- Thư viện ảnh tích hợp hiển thị thông tin chi tiết về ảnh (ngày, kích thước, trạng thái mã hóa)
- Tự động xóa ảnh sau 30 ngày (tùy chọn bật/tắt)
- Lưu và khôi phục cài đặt người dùng dễ dàng
- Giao diện Material Design hiện đại, hỗ trợ cả chế độ sáng và tối

## Cài đặt và Khởi chạy

### Yêu cầu hệ thống

- Android SDK 33 trở lên (Android 13+)
- Gradle 7.5+
- Java 11
- Thiết bị với ít nhất 1GB RAM và 50MB bộ nhớ trống
- Camera trước và sau (nếu có)

### Cài đặt từ mã nguồn

1. Clone repository:

   ```
   git clone <repository-url>
   ```

2. Mở project trong Android Studio:

   - Khởi động Android Studio
   - Chọn "Open an existing project"
   - Điều hướng đến thư mục `secure-camera-app`

3. Cấu hình SDK:

   - Mở file `local.properties` và cập nhật đường dẫn SDK:
     ```
     sdk.dir=C:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
     ```

4. Build project:

   - Sử dụng nút "Build" trong Android Studio
   - Hoặc sử dụng dòng lệnh:
     ```
     ./gradlew assembleDebug
     ```

5. Cài đặt trên thiết bị:
   - Kết nối thiết bị Android thông qua USB
   - Bật chế độ "USB Debugging" trên thiết bị
   - Chạy lệnh:
     ```
     ./gradlew installDebug
     ```

### Khởi chạy trên thiết bị giả lập

Nếu bạn muốn chạy trên thiết bị giả lập:

1. Tạo thiết bị ảo trong AVD Manager của Android Studio
2. Khởi động thiết bị ảo
3. Để sử dụng webcam của máy tính:
   ```
   emulator -avd <tên_thiết_bị_ảo> -webcam front -camera-front webcam0 -camera-back webcam0
   ```

## Thư viện và Công nghệ sử dụng

### Thư viện chính

- **CameraX** (androidx.camera): API hiện đại để truy cập và điều khiển camera

  - camera-core: Thành phần cốt lõi của CameraX
  - camera-camera2: Triển khai Camera2 API
  - camera-lifecycle: Tích hợp camera với lifecycle của Activity
  - camera-view: Cung cấp PreviewView để hiển thị luồng camera

- **Security Crypto** (androidx.security:security-crypto): Cung cấp API mã hóa file an toàn

  - Sử dụng mã hóa AES-256-GCM
  - Lưu trữ khóa trong Android Keystore

- **WorkManager** (androidx.work:work-runtime): Quản lý tác vụ nền như xóa ảnh tự động

  - Hỗ trợ tác vụ theo lịch và tác vụ định kỳ
  - Hoạt động ngay cả khi ứng dụng không chạy

- **Preferences** (androidx.preference): Quản lý lưu trữ cài đặt người dùng

  - Hỗ trợ EncryptedSharedPreferences cho dữ liệu nhạy cảm

- **Glide**: Thư viện tải và hiển thị hình ảnh hiệu quả

  - Bộ nhớ đệm thông minh
  - Hỗ trợ làm mờ và chuyển đổi ảnh

- **Material Components**: Cung cấp giao diện người dùng theo chuẩn Material Design
  - Thành phần UI hiện đại và nhất quán
  - Hỗ trợ chủ đề sáng/tối

### Các thành phần Android

- ConstraintLayout: Tạo giao diện linh hoạt
- RecyclerView: Hiển thị danh sách hình ảnh
- CardView: Tạo giao diện cards cho các mục trong thư viện ảnh
- AppCompat: Đảm bảo tính tương thích với các phiên bản Android cũ hơn

## Cấu trúc dự án

### Các thành phần chính

#### Activities

- **SplashActivity**: Màn hình khởi động, hiển thị logo và chuyển sang MainActivity sau 1.5 giây
- **MainActivity**: Màn hình chính với chức năng chụp ảnh
- **GalleryActivity**: Hiển thị ảnh đã chụp trong RecyclerView
- **SettingsActivity**: Quản lý cài đặt ứng dụng

#### Các lớp hỗ trợ

- **PhotoAdapter**: Adapter để hiển thị ảnh trong RecyclerView
- **CleanupWorker**: Worker định kỳ kiểm tra và xóa ảnh cũ

## Phân tích chi tiết từng thành phần

### SplashActivity

**Mục đích**: Hiển thị màn hình chào mừng và khởi tạo ứng dụng

**Các hàm chính**:

- `onCreate()`: Thiết lập giao diện và hẹn giờ chuyển sang MainActivity
- Sử dụng Handler để trì hoãn chuyển màn hình

### MainActivity

**Mục đích**: Màn hình chính để chụp và lưu ảnh có mã hóa

**Các hàm chính**:

- `onCreate()`: Khởi tạo giao diện, camera và cài đặt
- `requestPermissions()`: Yêu cầu quyền truy cập camera và bộ nhớ
- `startCamera()`: Cấu hình và khởi động camera sử dụng CameraX
- `takePhoto()`: Chụp ảnh và lưu vào bộ nhớ
- `encryptFile()`: Mã hóa file ảnh nếu được bật
- `toggleFlash()`: Chuyển đổi chế độ đèn flash
- `updateFlashUI()`: Cập nhật giao diện nút flash
- `updateEncryptionStatus()`: Cập nhật trạng thái mã hóa
- `showMessage()`: Hiển thị thông báo cho người dùng
- `scheduleCleanupWork()`: Lên lịch xóa ảnh tự động

**Cấu hình Camera**:

- Thiết lập chất lượng ảnh theo cài đặt người dùng
- Cấu hình flash dựa trên cài đặt
- Hỗ trợ chuyển đổi giữa camera trước và sau

### GalleryActivity

**Mục đích**: Hiển thị danh sách ảnh đã chụp

**Các hàm chính**:

- `onCreate()`: Khởi tạo RecyclerView và adapter
- `loadPhotos()`: Tải ảnh từ thư mục lưu trữ, sắp xếp theo thời gian mới nhất

### SettingsActivity

**Mục đích**: Quản lý cài đặt người dùng

**Các hàm chính**:

- `onCreate()`: Khởi tạo các điều khiển cài đặt
- `loadPreferences()`: Tải cài đặt đã lưu
- `savePreference()`: Lưu cài đặt mới
- `resetSettings()`: Khôi phục cài đặt mặc định
- `getFlashModeFromPref()`: Chuyển đổi vị trí spinner flash thành chế độ flash
- `getCameraLensFacingFromPref()`: Chuyển đổi vị trí spinner camera thành chế độ camera
- `getPhotoQualityFromPref()`: Chuyển đổi vị trí spinner chất lượng thành độ phân giải

### CleanupWorker

**Mục đích**: Xóa tự động ảnh cũ sau 30 ngày

**Các hàm chính**:

- `doWork()`: Chạy trong nền, quét thư mục ảnh và xóa các file cũ

## Cài đặt và tùy chọn người dùng

### Cài đặt bảo mật

- **Mã hóa mặc định**: Tự động bật mã hóa khi khởi động ứng dụng
- **Tự động xóa**: Xóa ảnh cũ sau 30 ngày

### Cài đặt camera

- **Chế độ flash mặc định**: Tắt, Bật, Tự động
- **Camera mặc định**: Camera sau hoặc camera trước
- **Chất lượng ảnh**:
  - Thấp (1080p)
  - Trung bình (2K)
  - Cao (4K)
  - Tối đa (độ phân giải tối đa của thiết bị)

## Bảo mật và quyền riêng tư

Ứng dụng Camera Bảo Mật sử dụng nhiều kỹ thuật bảo mật để đảm bảo quyền riêng tư của dữ liệu người dùng:

### Mã hóa ảnh

- Sử dụng thư viện `security-crypto` của Android để mã hóa file dựa trên mật khẩu thiết bị
- Lưu trữ khóa mã hóa an toàn trong EncryptedSharedPreferences
- Sử dụng thuật toán AES-256-GCM để mã hóa

### Quyền truy cập

- Ứng dụng yêu cầu các quyền tối thiểu cần thiết cho hoạt động:
  - `CAMERA`: Để truy cập camera thiết bị
  - `READ_EXTERNAL_STORAGE` và `WRITE_EXTERNAL_STORAGE`: Để lưu trữ ảnh
- Yêu cầu quyền rõ ràng từ người dùng tại thời điểm chạy

### Dữ liệu người dùng

- Tất cả ảnh được lưu trữ cục bộ trên thiết bị, không gửi lên máy chủ
- Không thu thập thông tin cá nhân của người dùng
- Tùy chọn tự động xóa dữ liệu để giảm thiểu rủi ro

## Quá trình phát triển

### Giai đoạn 1: Thiết lập dự án

- Tạo dự án Android mới
- Cấu hình các dependencies trong build.gradle
- Thiết lập cấu trúc thư mục và tài nguyên

### Giai đoạn 2: Phát triển giao diện người dùng

- Thiết kế và triển khai giao diện cho tất cả các activity
- Tạo màn hình chào mừng (SplashActivity)
- Thiết kế màn hình camera chính với các điều khiển
- Thiết kế giao diện cài đặt và thư viện ảnh

### Giai đoạn 3: Tích hợp Camera

- Tích hợp CameraX API
- Triển khai chức năng chụp ảnh
- Thêm tính năng chuyển đổi camera và điều khiển flash

### Giai đoạn 4: Triển khai bảo mật

- Tích hợp thư viện Security Crypto
- Triển khai mã hóa file
- Thêm cài đặt mã hóa và lưu trữ preferences

### Giai đoạn 5: Thư viện ảnh

- Phát triển GalleryActivity với RecyclerView
- Tạo PhotoAdapter cho hiển thị ảnh
- Thêm thông tin chi tiết về ảnh và trạng thái mã hóa

### Giai đoạn 6: Tính năng nâng cao

- Triển khai WorkManager cho tự động xóa ảnh
- Thêm cài đặt chất lượng ảnh
- Tối ưu hóa hiệu suất và sử dụng bộ nhớ

## Sử dụng ứng dụng

### Màn hình chính

- **Nút chụp (ở giữa dưới)**: Chụp ảnh
- **Nút flash (trên trái)**: Chuyển đổi chế độ flash
- **Nút chuyển camera (trên phải)**: Chuyển đổi giữa camera trước và sau
- **Công tắc mã hóa (dưới phải)**: Bật/tắt mã hóa cho ảnh tiếp theo
- **Nút thư viện (dưới trái)**: Mở thư viện ảnh

### Thư viện ảnh

- Hiển thị ảnh dưới dạng lưới
- Mỗi ảnh hiển thị:
  - Ngày và giờ chụp
  - Kích thước file
  - Biểu tượng khóa nếu ảnh được mã hóa

### Cài đặt

- Truy cập từ menu trong MainActivity
- Điều chỉnh tất cả cài đặt ứng dụng
- Nút khôi phục cài đặt gốc ở dưới cùng

## Lưu ý và hạn chế

- Ứng dụng yêu cầu Android 13 (API level 33) trở lên
- Một số tính năng flash có thể không hoạt động trên tất cả thiết bị
- Khi sử dụng webcam của laptop làm camera, cần đảm bảo không có ứng dụng nào khác đang sử dụng webcam
- Hiệu suất mã hóa có thể thay đổi tùy theo cấu hình thiết bị
- Ứng dụng hiện không hỗ trợ camera ngoài (USB hoặc Bluetooth)
- Quá trình mã hóa có thể mất nhiều thời gian hơn đối với ảnh chất lượng cao

## Phát triển trong tương lai

- Thêm tính năng chỉnh sửa ảnh (cắt, xoay, điều chỉnh màu sắc, bộ lọc)
- Hỗ trợ quay video với cùng khả năng mã hóa
- Thêm xác thực sinh trắc học (vân tay, nhận diện khuôn mặt) để mở ảnh được mã hóa
- Tạo phiên bản web để đồng bộ hóa ảnh an toàn
- Bổ sung tính năng chia sẻ ảnh an toàn với liên kết tự hủy
- Phát triển tùy chọn sao lưu đám mây được mã hóa

## Báo cáo lỗi và đóng góp

Nếu bạn phát hiện lỗi hoặc muốn đóng góp cho dự án, vui lòng:

1. Báo cáo lỗi thông qua mục Issues trên GitHub
2. Gửi Pull Request với các cải tiến
3. Liên hệ với nhóm phát triển qua email: securecameraapp@example.com

## Giấy phép

Ứng dụng này được phát hành dưới giấy phép MIT.

---

_Tài liệu này được cập nhật lần cuối vào ngày 18/05/2025_
