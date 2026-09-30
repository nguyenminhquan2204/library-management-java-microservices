<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nhắc hạn trả sách</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px;">
        <h2 style="color: #333333;">Sách bạn mượn sắp đến hạn trả</h2>
        <p style="color: #555555; font-size: 16px;">Chào ${employeeName!"bạn"},</p>
        <p style="color: #555555; font-size: 16px; line-height: 1.6;">
            Thư viện xin nhắc bạn: cuốn sách dưới đây sắp đến hạn trả.
        </p>
        <table style="width: 100%; border-collapse: collapse; font-size: 15px; color: #333333;">
            <tr><td style="padding: 8px 0; color: #888888;">Tên sách</td><td style="padding: 8px 0;"><strong>${bookName!""}</strong></td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Ngày mượn</td><td style="padding: 8px 0;">${borrowingDate!""}</td></tr>
            <tr><td style="padding: 8px 0; color: #888888;">Hạn trả</td><td style="padding: 8px 0;"><strong style="color: #d97706;">${dueDate!""}</strong></td></tr>
        </table>
        <p style="color: #555555; font-size: 16px; line-height: 1.6;">
            Vui lòng trả sách đúng hạn. Nếu trả muộn, bạn sẽ bị tính phí
            <strong>${(finePerDay!0)?string(",##0")} ${currency!""}</strong> cho mỗi ngày quá hạn.
        </p>
        <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">
        <p style="color: #888888; font-size: 14px;">Trân trọng,<br>Library Management Team</p>
    </div>
</body>
</html>
