# Local-Database-Manager
Java local database manager and data editor that uses CSV files for storing and creating data
---
![Console Preview](image.png)
---
## Database Schema Design
### Tables
- `Products (Cart) Table`
   - ProductID (Primary Key)
   - ItemName
   - Price
   - ItemType
   - CategoryType
   - ItemNo
   - SupplierID (Foreign Key)
   - isPaid

- `Suppliers Table`
    - SupplierID (Primary Key)
    - SupplierName
    - Contact

- `Payment (Debit/Credit) Table`
   - PaymentMethod
   - OrderID (Foreign Key)
   - OrderDetailID (Foreign Key)

- `OrderID`
    - OrderID (Primary Key)
    - CustomerName
    - CustomerContact
    - OrderDate
    - OrderDetailID (Foreign Key)

- `OrderDetailID`
    - OrderDetailID (Primary Key)
    - ProductID (Foreign Key)
    - PaymentID (Primary Key)