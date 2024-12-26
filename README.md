# Local-Database-Manager
Java local database manager and data editor that uses CSV files for storing and creating data
---
![console preview](image.png)
---
## Database Schema Design
### Tables
- `Products (Cart) Table (temporary table)`
   - ProductID (Primary Key)
   - ItemName
   - Price
   - GroceryType
   - CategoryType
   - ItemNo
   - SupplierID (Foreign Key)

- `Suppliers Table`
    - SupplierID (Primary Key)
    - SupplierName
    - Contact

- `Payment (Debit/Credit) Table`
   - PaymentMethod
   - PaymentID (Primary Key)
   - ProductID (Primary Key)
   - ItemName
   - Price
   - GroceryType
   - CategoryType
   - ItemNo
   - SupplierID (Foreign Key)
   - DatePayed

- `OrderID (Primary Key)` **`Work-in-progress`**
    - CustomerName
    - CustomerContact
    - OrderDate
    - OrderDetailID (Foreign Key)

- `OrderDetailID (Primary Key)` **`Work-in-progress`**
    - ProductID (Foreign Key)
    - PaymentID (Foreign)