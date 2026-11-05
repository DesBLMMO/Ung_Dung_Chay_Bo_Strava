package com.example.ung_dung_chay_bo

import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsUtilsTest {

    @Test
    fun haversineDistance_betweenTwoKnownCoordinates_isAccurate() {
        // Hà Nội (Hồ Gươm) -> (Hồ Tây)
        val lat1 = 21.0285
        val lon1 = 105.8542
        val lat2 = 21.0543
        val lon2 = 105.8245

        val distanceMeters = GpsUtils.haversineDistance(lat1, lon1, lat2, lon2)

        // Khoảng cách thực tế xấp xỉ ~4.2 km (4200m)
        assertTrue(distanceMeters > 3800.0 && distanceMeters < 4600.0)
    }

    @Test
    fun haversineDistance_samePoint_isZero() {
        val lat = 21.0285
        val lon = 105.8542

        val distance = GpsUtils.haversineDistance(lat, lon, lat, lon)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun calculatePace_normalRunningSpeed_returnsCorrectFormat() {
        // Tốc độ 3.33 m/s (~12 km/h) -> 1000 / 3.333 = 300s -> 05:00 /km
        val pace = GpsUtils.calculatePace(3.3333f)
        assertEquals("05:00 /km", pace)
    }

    @Test
    fun calculatePace_zeroOrVeryLowSpeed_returnsPlaceholder() {
        val paceZero = GpsUtils.calculatePace(0f)
        val paceLow = GpsUtils.calculatePace(0.05f)

        assertEquals("--:-- /km", paceZero)
        assertEquals("--:-- /km", paceLow)
    }

    @Test
    fun formatDuration_underOneHour_returnsMinutesAndSeconds() {
        val formatted = GpsUtils.formatDuration(125L) // 2 min 5 sec
        assertEquals("02:05", formatted)
    }

    @Test
    fun formatDuration_overOneHour_returnsHoursMinutesAndSeconds() {
        val formatted = GpsUtils.formatDuration(3665L) // 1 hr 1 min 5 sec
        assertEquals("01:01:05", formatted)
    }

    @Test
    fun formatDistance_formatsToTwoDecimalPlaces() {
        assertEquals("5.25 km", GpsUtils.formatDistance(5250.0))
        assertEquals("0.50 km", GpsUtils.formatDistance(500.0))
    }
}
