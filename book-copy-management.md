# Book Copy Management --- Nghiệp vụ quản lý nhiều bản sao sách

## 1. Mục tiêu

Thay mô hình:

``` text
Book
 └── isReady: Boolean
```

bằng mô hình thực tế hơn:

``` text
Book
 └── nhiều BookCopy
```

Một cuốn sách có thể có nhiều bản vật lý. Mỗi bản có trạng thái riêng và
chỉ một người có thể giữ/mượn một bản tại một thời điểm.

Ví dụ:

``` text
Book: Clean Code

COPY-001 → AVAILABLE
COPY-002 → BORROWED
COPY-003 → AVAILABLE
COPY-004 → LOST
```

Khi đó:

``` text
totalCopies     = 4
availableCopies = 2
```

> `availableCopies` có thể được lưu trực tiếp hoặc tính từ trạng thái
> của các `BookCopy`. Nếu lưu cả hai, phải có quy tắc để chúng không bị
> lệch.

------------------------------------------------------------------------

# 2. Các khái niệm nghiệp vụ

## 2.1 Book

`Book` là đầu sách/logical book.

Ví dụ:

``` text
BOOK-001
Clean Code
Robert C. Martin
```

Book không đại diện cho một quyển vật lý cụ thể.

------------------------------------------------------------------------

## 2.2 BookCopy

`BookCopy` là một bản vật lý cụ thể của Book.

Ví dụ:

``` text
BOOK-001
 ├── COPY-001
 ├── COPY-002
 ├── COPY-003
 └── COPY-004
```

Mỗi `BookCopy` có thể có:

-   id
-   bookId
-   barcode
-   status
-   location
-   condition
-   createdAt
-   updatedAt

Ví dụ:

``` text
COPY-001
bookId = BOOK-001
barcode = BC001
status = AVAILABLE
```

------------------------------------------------------------------------

# 3. Trạng thái của BookCopy

Đề xuất sử dụng:

``` text
AVAILABLE
RESERVED
BORROWED
LOST
DAMAGED
```

## AVAILABLE

Bản sách đang sẵn sàng để được mượn.

``` text
COPY-001 → AVAILABLE
```

------------------------------------------------------------------------

## RESERVED

Bản sách đã được giữ cho một borrowing nhưng quy trình mượn chưa hoàn
tất.

``` text
COPY-001 → RESERVED
```

Mục đích của `RESERVED` là tránh trường hợp một bản sách đã được Saga
chọn nhưng một request khác lại tiếp tục lấy bản đó.

------------------------------------------------------------------------

## BORROWED

Bản sách đã được xác nhận cho một người mượn.

``` text
COPY-001 → BORROWED
```

------------------------------------------------------------------------

## LOST

Bản sách bị mất.

``` text
COPY-001 → LOST
```

Không được chọn bản này để mượn.

------------------------------------------------------------------------

## DAMAGED

Bản sách bị hỏng và không thể cho mượn.

``` text
COPY-001 → DAMAGED
```

Không được chọn bản này để mượn.

------------------------------------------------------------------------

# 4. State transition

Luồng trạng thái cơ bản:

``` text
                  Reserve
AVAILABLE --------------------> RESERVED
    ^                              |
    |                              |
    |                              | Confirm Borrow
    |                              v
    |                           BORROWED
    |                              |
    |                              | Return
    +------------------------------+
```

Các trạng thái đặc biệt:

``` text
AVAILABLE → LOST
AVAILABLE → DAMAGED
```

Ví dụ:

``` text
AVAILABLE
    ↓
RESERVED
    ↓
BORROWED
    ↓
AVAILABLE
```

------------------------------------------------------------------------

# 5. Quy tắc nghiệp vụ quan trọng

## Rule 1 --- Chỉ AVAILABLE mới được giữ

Không được reserve:

``` text
RESERVED
BORROWED
LOST
DAMAGED
```

Chỉ được:

``` text
AVAILABLE → RESERVED
```

------------------------------------------------------------------------

## Rule 2 --- Một BookCopy chỉ được một Borrowing sử dụng tại một thời điểm

Không được:

``` text
COPY-001
   ↓
BORROW-001

và đồng thời

COPY-001
   ↓
BORROW-002
```

Một copy chỉ có thể thuộc về một borrowing đang active.

------------------------------------------------------------------------

## Rule 3 --- RESERVED không được người khác lấy

Nếu:

``` text
COPY-001 → RESERVED
```

thì request khác không được chọn COPY-001.

------------------------------------------------------------------------

## Rule 4 --- Borrowing chỉ được CONFIRMED khi giữ được BookCopy

Nếu không còn bản:

``` text
Borrowing
    ↓
FAILED
```

hoặc trạng thái tương đương tùy thiết kế.

Không được tạo một borrowing thành công mà không có BookCopy tương ứng.

------------------------------------------------------------------------

## Rule 5 --- Return phải trả đúng BookCopy

Nếu:

``` text
BORROW-001
bookId = BOOK-001
bookCopyId = COPY-003
```

thì khi trả phải release:

``` text
COPY-003
```

Không được lấy ngẫu nhiên một copy khác của `BOOK-001`.

------------------------------------------------------------------------

# 6. Các trạng thái của Borrowing

Đề xuất:

``` text
PENDING
CONFIRMED
RETURNED
FAILED
CANCELLED
```

## PENDING

Borrowing đã bắt đầu nhưng chưa giữ được BookCopy.

``` text
PENDING
```

## CONFIRMED

Đã giữ được BookCopy và borrowing được xác nhận.

``` text
CONFIRMED
```

## RETURNED

Người dùng đã trả sách.

``` text
RETURNED
```

## FAILED

Không thể hoàn tất quá trình mượn.

Ví dụ:

``` text
Không còn BookCopy AVAILABLE
```

## CANCELLED

Quá trình mượn bị hủy.

------------------------------------------------------------------------

# 7. Use case 1 --- Tạo Book

Admin tạo một đầu sách:

``` text
Create Book
```

Ví dụ:

``` json
{
  "title": "Clean Code",
  "author": "Robert C. Martin"
}
```

Sau đó tạo các BookCopy:

``` text
BOOK-001
 ├── COPY-001
 ├── COPY-002
 └── COPY-003
```

Trạng thái ban đầu:

``` text
COPY-001 → AVAILABLE
COPY-002 → AVAILABLE
COPY-003 → AVAILABLE
```

Kết quả:

``` text
totalCopies = 3
availableCopies = 3
```

------------------------------------------------------------------------

# 8. Use case 2 --- Thêm một bản sao

Admin thêm:

``` text
COPY-004
```

Kết quả:

``` text
totalCopies = 4
availableCopies = 4
```

Event có thể là:

``` text
BookCopyAddedEvent
```

------------------------------------------------------------------------

# 9. Use case 3 --- Mượn sách

Đây là nghiệp vụ quan trọng nhất.

User:

``` text
EMP-001
```

muốn mượn:

``` text
BOOK-001
```

Request:

``` http
POST /borrowings
```

``` json
{
  "employeeId": "EMP-001",
  "bookId": "BOOK-001"
}
```

------------------------------------------------------------------------

# 10. Luồng mượn tổng quát

``` text
Client
  |
  v
Borrowing Controller
  |
  v
CreateBorrowingCommand
  |
  v
Borrowing Aggregate
  |
  v
BorrowingCreatedEvent
  |
  v
Borrowing Saga
  |
  v
ReserveBookCopyCommand
  |
  v
Book Aggregate
  |
  v
BookCopyReservedEvent
  |
  v
Borrowing Saga
  |
  v
ConfirmBorrowingCommand
  |
  v
Borrowing Aggregate
  |
  v
BorrowingConfirmedEvent
```

------------------------------------------------------------------------

# 11. Bước 1 --- Create Borrowing

Controller nhận:

``` text
employeeId = EMP-001
bookId     = BOOK-001
```

Tạo:

``` text
CreateBorrowingCommand
```

Borrowing Aggregate tạo borrowing:

``` text
BORROW-001
status = PENDING
```

Phát:

``` text
BorrowingCreatedEvent
```

------------------------------------------------------------------------

# 12. Bước 2 --- Saga bắt đầu reserve

Saga nhận:

``` text
BorrowingCreatedEvent
```

Sau đó gửi:

``` text
ReserveBookCopyCommand
```

Thông tin nên có:

``` text
reservationId
borrowingId
bookId
```

Ví dụ:

``` text
reservationId = RESERVE-001
borrowingId   = BORROW-001
bookId        = BOOK-001
```

------------------------------------------------------------------------

# 13. Bước 3 --- Book Service tìm BookCopy

Book Aggregate xử lý:

``` text
ReserveBookCopyCommand
```

Giả sử:

``` text
COPY-001 → BORROWED
COPY-002 → AVAILABLE
COPY-003 → AVAILABLE
```

Aggregate chọn:

``` text
COPY-002
```

và chuyển:

``` text
COPY-002
AVAILABLE → RESERVED
```

Phát:

``` text
BookCopyReservedEvent
```

Event chứa:

``` text
reservationId
borrowingId
bookId
bookCopyId
```

Ví dụ:

``` text
RESERVE-001
BORROW-001
BOOK-001
COPY-002
```

------------------------------------------------------------------------

# 14. Bước 4 --- Saga xác nhận Borrowing

Saga nhận:

``` text
BookCopyReservedEvent
```

Sau đó gửi:

``` text
ConfirmBorrowingCommand
```

Borrowing Aggregate chuyển:

``` text
PENDING → CONFIRMED
```

và lưu:

``` text
bookId     = BOOK-001
bookCopyId = COPY-002
employeeId = EMP-001
```

Phát:

``` text
BorrowingConfirmedEvent
```

------------------------------------------------------------------------

# 15. Bước 5 --- BookCopy chuyển sang BORROWED

Có hai cách thiết kế.

### Cách A

Book Aggregate nhận command xác nhận:

``` text
ConfirmBookCopyBorrowedCommand
```

sau đó:

``` text
RESERVED → BORROWED
```

### Cách B

Một Event Handler/Projection cập nhật trạng thái read model.

Đối với domain aggregate, nên xác định rõ ai có quyền thay đổi trạng
thái domain. Không nên để một projection chỉ vì cập nhật DB mà vô tình
trở thành nơi quyết định business rule.

------------------------------------------------------------------------

# 16. Luồng thành công hoàn chỉnh

``` text
CreateBorrowingCommand
        |
        v
BorrowingAggregate
        |
        v
BorrowingCreatedEvent
        |
        v
Saga
        |
        v
ReserveBookCopyCommand
        |
        v
BookAggregate
        |
        v
AVAILABLE → RESERVED
        |
        v
BookCopyReservedEvent
        |
        v
Saga
        |
        v
ConfirmBorrowingCommand
        |
        v
BorrowingAggregate
        |
        v
PENDING → CONFIRMED
        |
        v
BorrowingConfirmedEvent
```

------------------------------------------------------------------------

# 17. Use case 4 --- Không còn sách

Giả sử:

``` text
COPY-001 → BORROWED
COPY-002 → BORROWED
COPY-003 → BORROWED
```

Không còn:

``` text
AVAILABLE
```

User gửi:

``` text
CreateBorrowingCommand
```

Saga vẫn có thể bắt đầu:

``` text
Borrowing = PENDING
```

Sau đó:

``` text
ReserveBookCopyCommand
```

Book Aggregate kiểm tra:

``` text
Không có AVAILABLE Copy
```

Không phát:

``` text
BookCopyReservedEvent
```

mà phát:

``` text
BookCopyReservationFailedEvent
```

Saga nhận failure và gửi:

``` text
FailBorrowingCommand
```

Borrowing:

``` text
PENDING → FAILED
```

Kết quả:

``` text
Không có sách để mượn.
```

------------------------------------------------------------------------

# 18. Use case 5 --- Trả sách

User trả:

``` text
BORROW-001
```

Borrowing biết:

``` text
bookId     = BOOK-001
bookCopyId = COPY-002
```

Client:

``` http
POST /borrowings/BORROW-001/return
```

Command:

``` text
ReturnBorrowingCommand
```

Borrowing Aggregate kiểm tra:

``` text
status == CONFIRMED
```

sau đó:

``` text
CONFIRMED → RETURNED
```

phát:

``` text
BorrowingReturnedEvent
```

------------------------------------------------------------------------

# 19. Saga xử lý trả sách

Saga nhận:

``` text
BorrowingReturnedEvent
```

và gửi:

``` text
ReleaseBookCopyCommand
```

với:

``` text
bookId     = BOOK-001
bookCopyId = COPY-002
borrowingId = BORROW-001
```

Book Aggregate kiểm tra:

``` text
COPY-002 == BORROWED
```

Sau đó:

``` text
BORROWED → AVAILABLE
```

phát:

``` text
BookCopyReleasedEvent
```

------------------------------------------------------------------------

# 20. Luồng trả sách

``` text
ReturnBorrowingCommand
        |
        v
BorrowingAggregate
        |
        v
BorrowingReturnedEvent
        |
        v
Saga
        |
        v
ReleaseBookCopyCommand
        |
        v
BookAggregate
        |
        v
BookCopyReleasedEvent
```

------------------------------------------------------------------------

# 21. Concurrency --- bài toán quan trọng nhất

Giả sử:

``` text
BOOK-001

COPY-001 → BORROWED
COPY-002 → AVAILABLE
```

Chỉ còn **1 bản**.

Hai nhân viên đồng thời muốn mượn:

``` text
EMP-001
EMP-002
```

Hai command:

``` text
ReserveBookCopyCommand(A)
ReserveBookCopyCommand(B)
```

Nếu xử lý sai:

``` text
A → thấy COPY-002 AVAILABLE
B → thấy COPY-002 AVAILABLE

A → reserve COPY-002
B → reserve COPY-002
```

Kết quả sai:

``` text
COPY-002
   ↓
BORROW-001
BORROW-002
```

Một bản sách bị mượn cho hai người.

------------------------------------------------------------------------

# 22. Quy tắc chống double booking

Cần có một consistency boundary rõ ràng.

Một lựa chọn đơn giản:

``` text
BookAggregate
```

quản lý việc phân bổ các BookCopy của một Book.

Command:

``` text
ReserveBookCopyCommand(bookId)
```

được route đến cùng Book Aggregate.

Aggregate xử lý tuần tự các command nhắm tới cùng aggregate theo cơ chế
concurrency của Axon.

State:

``` text
availableCopies = 1
```

Request A:

``` text
Reserve
1 → 0
```

Request B sau đó:

``` text
Reserve
0 → ERROR
```

Chỉ một request thành công.

------------------------------------------------------------------------

# 23. Optimistic Concurrency

Nếu sử dụng persistence/versioning phù hợp, Aggregate có thể có version:

``` text
BookAggregate
version = 10
```

Hai transaction cùng đọc version:

``` text
A → version 10
B → version 10
```

A commit:

``` text
version 10 → 11
```

B cố commit với state cũ:

``` text
version 10
```

Có thể gặp:

``` text
ConcurrencyException
```

Ứng dụng cần có chiến lược retry/failure phù hợp.

> Không nên chỉ dựa vào `availableCopies--` trong một query rồi nghĩ
> rằng đã giải quyết concurrency. Invariant "một copy không thể được giữ
> bởi hai borrowing" phải được bảo vệ ở đúng consistency boundary và
> persistence layer.

------------------------------------------------------------------------

# 24. Nếu dùng totalCopies + availableCopies

Nếu không cần theo dõi từng bản vật lý, có thể đơn giản hóa.

Book:

``` text
totalCopies
availableCopies
```

Reserve:

``` text
availableCopies > 0
```

sau đó:

``` text
availableCopies--
```

Return:

``` text
availableCopies++
```

Ví dụ:

``` text
totalCopies = 5
availableCopies = 1
```

Reserve:

``` text
5 / 1
   ↓
5 / 0
```

Return:

``` text
5 / 0
   ↓
5 / 1
```

Tuy nhiên vẫn phải giải quyết concurrency để không xảy ra:

``` text
1 available

Request A → reserve
Request B → reserve

=> available = -1
```

hoặc tệ hơn là hai borrowing đều được xác nhận.

------------------------------------------------------------------------

# 25. Projection và Database

Domain Event có thể được dùng để cập nhật read model.

Ví dụ:

``` text
BookCopyReservedEvent
        |
        v
BookProjection
        |
        v
book_copies
```

Database:

``` text
book_copies

id        book_id     status
--------------------------------
COPY-001  BOOK-001    BORROWED
COPY-002  BOOK-001    RESERVED
COPY-003  BOOK-001    AVAILABLE
```

Borrowing Projection:

``` text
BorrowingConfirmedEvent
        |
        v
BorrowingProjection
        |
        v
borrowings
```

Database:

``` text
borrowings

id          employee_id   book_id    book_copy_id   status
----------------------------------------------------------------
BORROW-001  EMP-001       BOOK-001   COPY-002       CONFIRMED
```

------------------------------------------------------------------------

# 26. Query --- xem sách còn bao nhiêu bản

API:

``` http
GET /books/BOOK-001
```

Query:

``` text
GetBookDetailQuery
```

Query Handler đọc projection:

``` text
BookProjection
        |
        v
Book DB
```

Response:

``` json
{
  "id": "BOOK-001",
  "title": "Clean Code",
  "totalCopies": 3,
  "availableCopies": 1
}
```

Nếu dùng BookCopy:

``` json
{
  "id": "BOOK-001",
  "title": "Clean Code",
  "copies": [
    {
      "id": "COPY-001",
      "status": "BORROWED"
    },
    {
      "id": "COPY-002",
      "status": "AVAILABLE"
    },
    {
      "id": "COPY-003",
      "status": "BORROWED"
    }
  ]
}
```

------------------------------------------------------------------------

# 27. Compensation khi Saga thất bại

Đây là phần rất đáng chú ý.

Giả sử:

``` text
BookCopy
AVAILABLE
    ↓
RESERVED
```

Sau đó Saga gửi:

``` text
ConfirmBorrowingCommand
```

nhưng Borrowing Service gặp lỗi.

Lúc này không được để mãi:

``` text
COPY-001 → RESERVED
```

vì bản sách đã bị giữ nhưng không có borrowing thành công.

Saga cần có compensation:

``` text
ReleaseBookCopyCommand
```

để:

``` text
RESERVED → AVAILABLE
```

Luồng:

``` text
Reserve copy
     ↓
Success
     ↓
Confirm borrowing
     ↓
FAIL
     ↓
Compensation
     ↓
Release copy
     ↓
AVAILABLE
```

------------------------------------------------------------------------

# 28. Timeout

Một Saga phân tán cũng có thể gặp:

``` text
BorrowingCreatedEvent
       ↓
Saga
       ↓
ReserveBookCopyCommand
       ↓
Book Service
       ↓
???
```

Nếu Book Service không phản hồi hoặc event không tới, borrowing có thể
treo ở:

``` text
PENDING
```

Do đó nên có timeout/timeout policy:

``` text
PENDING
   |
   | quá thời gian
   v
CANCELLED / FAILED
```

và nếu đã reserve copy thì phải release.

------------------------------------------------------------------------

# 29. Các Command đề xuất

## Borrowing Service

``` text
CreateBorrowingCommand
ConfirmBorrowingCommand
FailBorrowingCommand
ReturnBorrowingCommand
CancelBorrowingCommand
```

## Book Service

``` text
CreateBookCommand
AddBookCopyCommand
ReserveBookCopyCommand
ConfirmBookCopyBorrowedCommand
ReleaseBookCopyCommand
MarkBookCopyLostCommand
MarkBookCopyDamagedCommand
```

------------------------------------------------------------------------

# 30. Các Event đề xuất

## Borrowing

``` text
BorrowingCreatedEvent
BorrowingConfirmedEvent
BorrowingFailedEvent
BorrowingReturnedEvent
BorrowingCancelledEvent
```

## Book

``` text
BookCreatedEvent
BookCopyAddedEvent
BookCopyReservedEvent
BookCopyBorrowedEvent
BookCopyReleasedEvent
BookCopyReservationFailedEvent
BookCopyMarkedLostEvent
BookCopyMarkedDamagedEvent
```

------------------------------------------------------------------------

# 31. Aggregate boundary đề xuất

Một phương án dễ triển khai và dễ hiểu:

``` text
BookAggregate
    |
    +-- bookId
    +-- copies / inventory state
```

và:

``` text
BorrowingAggregate
    |
    +-- borrowingId
    +-- employeeId
    +-- bookId
    +-- bookCopyId
    +-- status
```

Saga:

``` text
BorrowingSaga
```

chịu trách nhiệm điều phối:

``` text
Borrowing
    ↕
Book
```

Không để:

``` text
BorrowingAggregate
      ↓
trực tiếp update
Book DB
```

và cũng không để:

``` text
BookAggregate
      ↓
trực tiếp update
Borrowing DB
```

Mỗi service giữ database của mình.

------------------------------------------------------------------------

# 32. Database đề xuất

## Book DB

### books

``` text
id
title
author
created_at
updated_at
```

### book_copies

``` text
id
book_id
barcode
status
location
condition
created_at
updated_at
```

Quan hệ:

``` text
books 1 ---- N book_copies
```

------------------------------------------------------------------------

## Borrowing DB

### borrowings

``` text
id
employee_id
book_id
book_copy_id
status
borrowing_date
return_date
created_at
updated_at
```

Có thể lưu:

``` text
reservation_id
```

nếu cần trace Saga.

------------------------------------------------------------------------

# 33. Một số invariant cần test

Đây là các rule nên viết integration/concurrency test.

### Test 1

Một copy AVAILABLE.

``` text
1 request
→ SUCCESS
```

------------------------------------------------------------------------

### Test 2

Một copy BORROWED.

``` text
1 request
→ FAILED
```

------------------------------------------------------------------------

### Test 3

Hai request đồng thời, một copy.

``` text
2 requests
→ 1 SUCCESS
→ 1 FAILED
```

Không được:

``` text
2 SUCCESS
```

------------------------------------------------------------------------

### Test 4

10 copies, 100 requests đồng thời.

Kỳ vọng:

``` text
10 SUCCESS
90 FAILED
```

------------------------------------------------------------------------

### Test 5

Reserve thành công nhưng confirm borrowing thất bại.

Kỳ vọng:

``` text
BookCopy
RESERVED
    ↓
AVAILABLE
```

------------------------------------------------------------------------

### Test 6

Borrowing returned.

Kỳ vọng:

``` text
Borrowing
CONFIRMED → RETURNED

BookCopy
BORROWED → AVAILABLE
```

------------------------------------------------------------------------

### Test 7

Không được return hai lần.

``` text
RETURNED
    ↓
ReturnBorrowingCommand
    ↓
ERROR
```

------------------------------------------------------------------------

# 34. Luồng nghiệp vụ cuối cùng

Tóm lại, nghiệp vụ nên được hình dung như sau:

``` text
                         MƯỢN SÁCH

Client
  |
  v
Create Borrowing
  |
  v
Borrowing = PENDING
  |
  v
Saga
  |
  v
Reserve 1 BookCopy
  |
  +------ Không còn copy ------> Borrowing = FAILED
  |
  |
  +------ Có copy
             |
             v
       Copy = RESERVED
             |
             v
       Confirm Borrowing
             |
             +---- FAIL ----> Release Copy
             |
             v
       Borrowing = CONFIRMED
             |
             v
       Copy = BORROWED
```

Trả sách:

``` text
                         TRẢ SÁCH

Return Borrowing
       |
       v
Borrowing = RETURNED
       |
       v
Saga
       |
       v
Release BookCopy
       |
       v
BookCopy = AVAILABLE
```

------------------------------------------------------------------------

# 35. Mục tiêu của feature

Sau khi hoàn thành feature này, hệ thống phải đảm bảo:

``` text
✓ Một Book có nhiều BookCopy

✓ Mỗi BookCopy có trạng thái riêng

✓ Chỉ AVAILABLE mới được reserve

✓ RESERVED không thể bị người khác reserve

✓ Một BookCopy không thể thuộc hai borrowing cùng lúc

✓ Borrowing phải biết chính xác BookCopy nào được mượn

✓ Reserve thành công nhưng Borrowing fail phải có compensation

✓ Return phải release đúng BookCopy

✓ Không còn copy → borrowing thất bại

✓ Hai request đồng thời tranh bản cuối → chỉ một request thành công

✓ Query có thể xem totalCopies / availableCopies

✓ Mỗi service chỉ quản lý database của chính mình
```

------------------------------------------------------------------------

# 36. Thứ tự triển khai khuyến nghị

Đừng triển khai tất cả cùng lúc. Có thể chia thành các phase:

### Phase 1 --- Domain model

``` text
Book
BookCopy
Borrowing
```

và enum:

``` text
BookCopyStatus
BorrowingStatus
```

### Phase 2 --- CRUD BookCopy

``` text
Add Copy
Remove Copy
Mark Lost
Mark Damaged
```

### Phase 3 --- Reserve

``` text
ReserveBookCopyCommand
BookCopyReservedEvent
```

### Phase 4 --- Borrowing Saga

``` text
Create Borrowing
    ↓
Reserve Copy
    ↓
Confirm Borrowing
```

### Phase 5 --- Return

``` text
Return Borrowing
    ↓
Release Copy
```

### Phase 6 --- Compensation

``` text
Reserve success
    ↓
Confirm failed
    ↓
Release Copy
```

### Phase 7 --- Concurrency

Viết test:

``` text
2 users
1 copy
```

sau đó:

``` text
100 users
10 copies
```

Đây là phase rất đáng làm vì nó giúp bạn hiểu **Axon Aggregate
concurrency thực sự hoạt động như thế nào**, thay vì chỉ sử dụng
`@CommandHandler` và `@EventHandler` theo CRUD thông thường.
