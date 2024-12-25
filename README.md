# Local-Database-Manager
Java local database manager and data editor that uses CSV files for storing and creating data
---
![console preview](image.png)
---
## Database Schema Design
### Tables
- `Products (Cart) Table`
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
   - ProductID (Foreign Key)

- `OrderID (Primary Key)`
    - CustomerName
    - CustomerContact
    - OrderDate
    - OrderDetailID (Foreign Key)

- `OrderDetailID (Primary Key)`
    - OrderID (Foreign Key)
    - ProductID (Foreign Key)
    - Quantity