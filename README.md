# Hotel Management System
### OSDL Lab Project | Java + JavaFX | Maven Build

## 📁 Project Structure

```
HotelManagementSystem/
├── pom.xml                          # Maven build file (JavaFX + Shade plugins)
├── data/                            # Persistent storage (auto-created)
│   ├── rooms.dat                    # Serialized room objects 
│   └── bills_log.txt                # Bill receipts log 
│   └── roomtypes.dat                # Hotel room types
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/hotel/
    │       ├── MainApp.java         # JavaFX Application, Stage/Scene setup
    │       ├── model/
    │       │   ├── Room.java        # Serializable model, Enum RoomType
    │       │   └── Bill.java        # Invoice model with GST
    │       ├── service/
    │       │   ├── HotelService.java      # Business logic, Collections
    │       │   └── FileStorageService.java # File I/O, Serialization
    │       └── ui/
    │           ├── DashboardView.java     # Overview stats
    │           ├── RoomManagementView.java # Room CRUD + TableView
    │           ├── BookingView.java        # Book/Checkout + Bill
    │           ├── BillingView.java        # Revenue + file log
    │           └── RoomCleaningView.java       # Room cleaning using thread
    └── resources/
        └── com/hotel/styles/
            └── hotel.css           
```

