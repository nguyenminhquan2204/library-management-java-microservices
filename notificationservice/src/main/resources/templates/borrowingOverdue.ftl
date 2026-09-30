<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thông báo sách quá hạn</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px;">
        <h2 style="color: #b91c1c;">Sách bạn mượn đã quá hạn trả</h2>
        <p style="color: #555555; font-size: 16px;">Chào ${employeeName!"bạn"},</p>
        <p style="color: #555555; font-size: 16px; line-height: 1.6;">
            Cuốn sách dưới đây đã quá hạn trả <strong>${overdueDays} ngày</strong>.
        </p>
        <table style="width: 100%; border-collapse: collapse; font-size: 15px; color: #333333;">
            <tr><td style="padding: 8px 0; color: #888888;">Tên sách</td><td style="padding: 8px 0;"><strong>${bookName!""}</strong></td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Ngày mượn</td><td style="padding: 8px 0;">${borrowingDate!""}</td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Hạn trả</td><td style="padding: 8px 0;">${dueDate!""}</td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Số ngày quá hạn</td><td style="padding: 8px 0;">${overdueDays}</td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Phí mỗi ngày</td><td style="padding: 8px 0;">${(finePerDay!0)?string(",##0")} ${currency!""}</td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Tiền phạt tạm tính</td><td style="padding: 8px 0;"><strong style="color: #b91c1c; font-size: 18px;">${(fineAmount!0)?string(",##0")} ${currency!""}</strong></td></tr>
        </table>
        <p style="color: #555555; font-size: 16px; line-height: 1.6;">
            Tiền phạt tăng thêm mỗi ngày cho tới khi sách được trả. Vui lòng trả sách sớm nhất có thể;
            số tiền cuối cùng được chốt vào ngày bạn trả sách.
        </p>
        <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
        <p style="color: #888888; font-size: 14px;">Trân trọng,<br>Library Management Team</p>
    </div>
</body>
</html>
