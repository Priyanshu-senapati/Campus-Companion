package com.rvu.campuscompanion.data.repository

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.rvu.campuscompanion.data.model.CampusLocation

object MapRepository {
    val locations = listOf(
        CampusLocation("gate", "Main Gate", 12.9141, 77.4966,
            "Primary entry to RV University campus. Security check-in.",
            "Open 24/7", "Entry", BitmapDescriptorFactory.HUE_RED),
        CampusLocation("academic", "Academic Block", 12.9145, 77.4970,
            "Main academic building — lecture halls, departments, admin offices.",
            "8:00 AM – 6:00 PM", "Academics", BitmapDescriptorFactory.HUE_AZURE),
        CampusLocation("library", "Central Library", 12.9143, 77.4968,
            "Books, journals, digital resources and silent study zones.",
            "8:00 AM – 8:00 PM", "Library", BitmapDescriptorFactory.HUE_BLUE),
        CampusLocation("mingos", "Mingo's Food Court", 12.9139, 77.4965,
            "Multi-cuisine canteen — breakfast, lunch, snacks and beverages.",
            "7:30 AM – 6:00 PM", "Food", BitmapDescriptorFactory.HUE_ORANGE),
        CampusLocation("sports", "Sports Complex", 12.9150, 77.4975,
            "Gym, indoor courts, athletics ground, swimming pool.",
            "6:00 AM – 8:00 PM", "Sports", BitmapDescriptorFactory.HUE_GREEN),
        CampusLocation("boys_hostel", "Boys Hostel", 12.9155, 77.4972,
            "Residential block for male students.",
            "Resident access only", "Residence", BitmapDescriptorFactory.HUE_CYAN),
        CampusLocation("girls_hostel", "Girls Hostel", 12.9157, 77.4968,
            "Residential block for female students.",
            "Resident access only", "Residence", BitmapDescriptorFactory.HUE_MAGENTA),
        CampusLocation("medical", "Medical Center", 12.9142, 77.4962,
            "On-campus clinic with doctor consultations and first aid.",
            "9:00 AM – 5:00 PM", "Health", BitmapDescriptorFactory.HUE_RED),
        CampusLocation("auditorium", "Auditorium", 12.9148, 77.4971,
            "800-seat auditorium for events, seminars and convocations.",
            "Event-based", "Events", BitmapDescriptorFactory.HUE_YELLOW),
        CampusLocation("atm", "SBI ATM", 12.9140, 77.4963,
            "24/7 ATM kiosk for cash withdrawals.",
            "Open 24/7", "Services", BitmapDescriptorFactory.HUE_VIOLET),
        CampusLocation("wifi1", "WiFi Zone — Quad", 12.9144, 77.4969,
            "High-speed Wi-Fi hotspot. SSID: RVU_Student.",
            "Always active", "WiFi", BitmapDescriptorFactory.HUE_ROSE),
        CampusLocation("parking", "Parking Area", 12.9138, 77.4964,
            "200-vehicle capacity. Two-wheelers and cars.",
            "Open 24/7", "Parking", BitmapDescriptorFactory.HUE_AZURE)
    )
}
