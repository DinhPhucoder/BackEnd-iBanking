# PaymentService Flow Guide

## Vai trò
- Điều phối giao dịch thanh toán học phí.
- Không lưu trữ dữ liệu trong DB. Service chỉ giữ trạng thái tạm thời trong bộ nhớ cho các giao dịch đang chờ OTP.
- Tương tác với: AccountService, TuitionService, OTPNotificationService.

## API
- POST `/payments` (initiate)
  - Input: `{ userId, mssv, amount }`
  - Output: `{ paymentId, otpRequired: true }`
- POST `/payments/confirm` (confirm)
  - Input: `{ otp }`
  - Output: `{ status: "success" | "failed", transactionId }`

## Trình tự Initiate
1. Kiểm tra MSSV tồn tại với TuitionService (ví dụ endpoint: `GET /tuition/{mssv}/exists`).
2. Kiểm tra số dư với AccountService (ví dụ endpoint: `GET /accounts/{userId}/balance`).
3. Yêu cầu khóa theo thứ tự để tránh deadlock:
   - `POST /accounts/{userId}/lock`
   - `POST /tuition/{mssv}/lock`
   - Nếu khóa bất kỳ bên nào thất bại → trả 409 Conflict.
4. Tạo OTP gắn với `paymentId` bằng OTPNotificationService (ví dụ `POST /otp?paymentId=&userId=`).
5. Lưu trạng thái pending trong RAM: `paymentId → { userId, mssv, amount }`.
6. Trả `{ paymentId, otpRequired: true }` cho client.

## Trình tự Confirm
1. Xác minh OTP với OTPNotificationService (ví dụ `POST /otp/verify?otp=`) → trả về `paymentId` nếu hợp lệ.
2. Tra `paymentId` trong pending. Nếu không có → trả `{ status: "failed" }`.
3. Thực hiện cập nhật theo thứ tự an toàn:
   - `POST /accounts/{userId}/balance?delta=-amount`
   - `POST /tuition/{mssv}/paid?paymentId=...`
   - Nếu cập nhật học phí lỗi → rollback sơ bộ số dư (`delta=+amount`).
4. Lưu giao dịch: `POST /accounts/{userId}/transactions?paymentId=...&amount=...` → nhận `transactionId`.
5. Xoá `pending[paymentId]`.
6. Mở khóa cả hai phía:
   - `POST /accounts/{userId}/unlock`
   - `POST /tuition/{mssv}/unlock`
7. Trả `{ status: "success", transactionId }`.

## Quy ước lỗi/HTTP status
- Khóa thất bại: 409 Conflict.
- Dữ liệu không hợp lệ (MSSV không tồn tại, số dư không đủ): 400 Bad Request.
- Lỗi service phụ thuộc (Account/Tuition/OTP hỏng hoặc timeout): 502 Bad Gateway.
- OTP sai: 200 với `{ status: "failed" }`.

## Cấu trúc mã nguồn (đề xuất)
- `dto/*`: models cho payload vào/ra API.
- `config/HttpClientConfig.java`: cấu hình `WebClient` + base URLs của các service phụ thuộc.
- `client/*`: wrapper gọi REST tới AccountService, TuitionService, OTPNotificationService.
- `service/PaymentOrchestratorService.java`: luồng điều phối initiate/confirm, giữ lock/rollback/unlock, không dùng DB.
- `controller/PaymentController.java`: ánh xạ endpoints tới orchestrator.

## Ghi chú triển khai
- Vì confirm chỉ nhận `{ otp }`, OTP service cần ánh xạ `otp → paymentId` và trả `paymentId` khi verify thành công.
- Nên bổ sung cơ chế timeout/cleanup pending để tránh giữ lock quá lâu nếu người dùng không xác nhận OTP (có thể dùng Scheduler đơn giản xoá pending quá hạn và gọi unlock).
- Thứ tự lock nhất quán (user trước, rồi mssv) để tránh deadlock khi có nhiều giao dịch đồng thời.
- Khi xảy ra lỗi sau khi đã trừ tiền nhưng chưa cập nhật học phí, cần rollback sơ bộ số dư.


