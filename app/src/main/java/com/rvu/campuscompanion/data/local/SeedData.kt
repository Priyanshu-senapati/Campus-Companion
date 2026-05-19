package com.rvu.campuscompanion.data.local

object SeedData {

    suspend fun seedTimetable(dao: TimetableDao) {
        if (dao.count() > 0) return
        val branch = "CSE"
        val sem = 5
        val entries = listOf(
            // Monday
            TimetableEntry(0, "Operating Systems", "Prof. Ramesh K", "Room 301", "Monday", "09:00", "10:00", "Lecture", sem, branch),
            TimetableEntry(0, "Database Management", "Prof. Priya S", "Room 302", "Monday", "10:00", "11:00", "Lecture", sem, branch),
            TimetableEntry(0, "OS Lab", "Prof. Ramesh K", "Lab 105", "Monday", "11:15", "13:15", "Lab", sem, branch),
            TimetableEntry(0, "Computer Networks", "Prof. Anil M", "Room 301", "Monday", "14:00", "15:00", "Lecture", sem, branch),
            TimetableEntry(0, "Theory of Computation", "Prof. Sunitha R", "Room 305", "Monday", "15:00", "16:00", "Lecture", sem, branch),

            // Tuesday
            TimetableEntry(0, "Database Management", "Prof. Priya S", "Room 302", "Tuesday", "09:00", "10:00", "Lecture", sem, branch),
            TimetableEntry(0, "Operating Systems", "Prof. Ramesh K", "Room 301", "Tuesday", "10:00", "11:00", "Lecture", sem, branch),
            TimetableEntry(0, "DBMS Lab", "Prof. Priya S", "Lab 106", "Tuesday", "11:15", "13:15", "Lab", sem, branch),
            TimetableEntry(0, "Software Engineering", "Prof. Karthik N", "Room 303", "Tuesday", "14:00", "15:00", "Lecture", sem, branch),
            TimetableEntry(0, "Math Tutorial", "Prof. Geetha V", "Room 304", "Tuesday", "15:00", "16:00", "Tutorial", sem, branch),

            // Wednesday
            TimetableEntry(0, "Computer Networks", "Prof. Anil M", "Room 301", "Wednesday", "09:00", "10:00", "Lecture", sem, branch),
            TimetableEntry(0, "Software Engineering", "Prof. Karthik N", "Room 303", "Wednesday", "10:00", "11:00", "Lecture", sem, branch),
            TimetableEntry(0, "Theory of Computation", "Prof. Sunitha R", "Room 305", "Wednesday", "11:15", "12:15", "Lecture", sem, branch),
            TimetableEntry(0, "CN Lab", "Prof. Anil M", "Lab 107", "Wednesday", "14:00", "16:00", "Lab", sem, branch),

            // Thursday
            TimetableEntry(0, "Operating Systems", "Prof. Ramesh K", "Room 301", "Thursday", "09:00", "10:00", "Lecture", sem, branch),
            TimetableEntry(0, "Computer Networks", "Prof. Anil M", "Room 301", "Thursday", "10:00", "11:00", "Lecture", sem, branch),
            TimetableEntry(0, "Software Engineering", "Prof. Karthik N", "Room 303", "Thursday", "11:15", "12:15", "Lecture", sem, branch),
            TimetableEntry(0, "DB Tutorial", "Prof. Priya S", "Room 302", "Thursday", "14:00", "15:00", "Tutorial", sem, branch),
            TimetableEntry(0, "Theory of Computation", "Prof. Sunitha R", "Room 305", "Thursday", "15:00", "16:00", "Lecture", sem, branch),

            // Friday
            TimetableEntry(0, "Database Management", "Prof. Priya S", "Room 302", "Friday", "09:00", "10:00", "Lecture", sem, branch),
            TimetableEntry(0, "Software Engineering", "Prof. Karthik N", "Room 303", "Friday", "10:00", "11:00", "Lecture", sem, branch),
            TimetableEntry(0, "SE Lab", "Prof. Karthik N", "Lab 108", "Friday", "11:15", "13:15", "Lab", sem, branch),
            TimetableEntry(0, "Open Elective", "Prof. Visiting", "Room 306", "Friday", "14:00", "15:00", "Lecture", sem, branch),

            // Saturday
            TimetableEntry(0, "Project Work", "Prof. Karthik N", "Lab 110", "Saturday", "09:00", "12:00", "Lab", sem, branch),
            TimetableEntry(0, "Seminar", "Various", "Auditorium", "Saturday", "13:00", "14:00", "Tutorial", sem, branch)
        )
        dao.insertAll(entries)
    }

    suspend fun seedMenu(dao: MenuItemDao) {
        if (dao.count() > 0) return
        val items = listOf(
            // Breakfast
            MenuItemEntity(0, "Idli Sambar", 30, "Breakfast", true, true, "Soft steamed rice cakes served with sambar and chutney", 180, "None"),
            MenuItemEntity(0, "Masala Dosa", 45, "Breakfast", true, true, "Crispy dosa stuffed with spiced potato masala", 320, "None"),
            MenuItemEntity(0, "Medu Vada", 20, "Breakfast", true, true, "Crispy lentil donuts with sambar", 200, "None"),
            MenuItemEntity(0, "Poha", 25, "Breakfast", true, true, "Flattened rice with onions and peanuts", 220, "Peanuts"),
            MenuItemEntity(0, "Tea", 10, "Breakfast", true, true, "Hot Indian milk tea", 60, "Dairy"),
            MenuItemEntity(0, "Coffee", 15, "Breakfast", true, true, "Filter coffee", 70, "Dairy"),

            // Lunch
            MenuItemEntity(0, "Rice + Dal + Sabzi", 60, "Lunch", true, true, "Full meal — rice, dal, sabzi, curd", 520, "Dairy"),
            MenuItemEntity(0, "Chapati (2) + Curry", 50, "Lunch", true, true, "Two chapatis with seasonal curry", 450, "Wheat"),
            MenuItemEntity(0, "Chicken Biryani", 80, "Lunch", false, true, "Aromatic chicken biryani with raita", 640, "Dairy"),
            MenuItemEntity(0, "Veg Biryani", 65, "Lunch", true, true, "Vegetable biryani with raita", 560, "Dairy"),
            MenuItemEntity(0, "Curd Rice", 40, "Lunch", true, true, "Cooling curd rice with tempering", 380, "Dairy"),

            // Snacks
            MenuItemEntity(0, "Samosa", 15, "Snacks", true, true, "Crispy potato samosa", 240, "Wheat"),
            MenuItemEntity(0, "Bread Omelette", 35, "Snacks", false, true, "Toasted bread with masala omelette", 320, "Eggs, Wheat"),
            MenuItemEntity(0, "Maggi", 30, "Snacks", true, true, "Instant noodles with vegetables", 290, "Wheat"),
            MenuItemEntity(0, "French Fries", 50, "Snacks", true, true, "Crispy potato fries with seasoning", 360, "None"),
            MenuItemEntity(0, "Corn Chat", 40, "Snacks", true, true, "Spicy sweet corn chat", 220, "None"),

            // Beverages
            MenuItemEntity(0, "Lassi", 30, "Beverages", true, true, "Sweet yogurt drink", 180, "Dairy"),
            MenuItemEntity(0, "Fresh Lime Soda", 25, "Beverages", true, true, "Refreshing lime soda — salt or sweet", 80, "None"),
            MenuItemEntity(0, "Buttermilk", 20, "Beverages", true, true, "Spiced buttermilk", 60, "Dairy"),
            MenuItemEntity(0, "Cold Coffee", 45, "Beverages", true, true, "Iced milk coffee", 220, "Dairy")
        )
        dao.insertAll(items)
    }
}
