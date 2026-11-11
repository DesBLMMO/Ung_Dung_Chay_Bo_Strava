package com.example.ung_dung_chay_bo.core.utils

import com.example.ung_dung_chay_bo.domain.model.RouteDifficulty
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import com.example.ung_dung_chay_bo.domain.model.SurfaceType

object RouteCatalog {
    const val DEFAULT_USER_LAT = 21.0025
    const val DEFAULT_USER_LNG = 105.8185

    fun getDefaultRoutes(): List<SuggestedRoute> {
        val userLat = DEFAULT_USER_LAT
        val userLng = DEFAULT_USER_LNG

        return listOf(
            SuggestedRoute(
                id = "ride_1",
                title = "Cung đường Cầu Nhật Tân - Sông Hồng đón gió",
                sportType = SportType.RIDE,
                distanceKm = 18.5,
                durationMinutes = 55,
                elevationGainM = 35,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Chân cầu Nhật Tân (Phú Thượng)",
                areaName = "Tây Hồ - Đông Anh",
                isPersonalized = true,
                isSaved = true,
                startPoint = Pair(21.0880, 105.8180),
                routesInAreaCount = 7,
                runnersCount = 2350,
                imageUrl = "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400",
                routePoints = listOf(
                    21.0880 to 105.8180,
                    21.0965 to 105.8200,
                    21.1070 to 105.8220,
                    21.1180 to 105.8240,
                    21.1270 to 105.8260,
                    21.1250 to 105.8350,
                    21.0965 to 105.8200,
                    21.0880 to 105.8180
                )
            ),
            SuggestedRoute(
                id = "ride_2",
                title = "Đại lộ Thăng Long - Chinh phục Hòa Lạc",
                sportType = SportType.RIDE,
                distanceKm = 30.0,
                durationMinutes = 75,
                elevationGainM = 60,
                difficulty = RouteDifficulty.HARD,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Trung tâm Hội nghị Quốc gia",
                areaName = "Mễ Trì - Hòa Lạc",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(21.0050, 105.7850),
                routesInAreaCount = 12,
                runnersCount = 1180,
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=400",
                routePoints = listOf(
                    21.0050 to 105.7850,
                    21.0030 to 105.7650,
                    21.0010 to 105.7350,
                    20.9990 to 105.7000,
                    20.9980 to 105.6600,
                    20.9970 to 105.6000
                )
            ),
            SuggestedRoute(
                id = "ride_3",
                title = "Đê Bát Tràng - Cung đường làng gốm ven sông",
                sportType = SportType.RIDE,
                distanceKm = 14.2,
                durationMinutes = 45,
                elevationGainM = 15,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Chân cầu Vĩnh Tuy (Long Biên)",
                areaName = "Long Biên - Gia Lâm",
                isPersonalized = false,
                isSaved = false,
                startPoint = Pair(21.0055, 105.8760),
                routesInAreaCount = 6,
                runnersCount = 1620,
                imageUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=400",
                routePoints = listOf(
                    21.0055 to 105.8760,
                    20.9960 to 105.8880,
                    20.9850 to 105.8990,
                    20.9750 to 105.9080,
                    20.9690 to 105.9120,
                    20.9850 to 105.8990,
                    21.0055 to 105.8760
                )
            ),

            SuggestedRoute(
                id = "run_1",
                title = "Vòng quanh Bờ Hồ Hoàn Kiếm - Trái tim Thủ Đô",
                sportType = SportType.RUN,
                distanceKm = 1.75,
                durationMinutes = 12,
                elevationGainM = 5,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Quảng trường Đông Kinh Nghĩa Thục",
                areaName = "Hoàn Kiếm",
                isPersonalized = true,
                isSaved = true,
                startPoint = Pair(21.0315, 105.8525),
                routesInAreaCount = 14,
                runnersCount = 4720,
                imageUrl = "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=400",
                routePoints = listOf(
                    21.0315 to 105.8525,
                    21.0305 to 105.8540,
                    21.0265 to 105.8535,
                    21.0250 to 105.8520,
                    21.0270 to 105.8495,
                    21.0300 to 105.8510,
                    21.0315 to 105.8525
                )
            ),
            SuggestedRoute(
                id = "run_2",
                title = "Vòng Hồ Tây lộng gió - Cung đường huyền thoại 15K",
                sportType = SportType.RUN,
                distanceKm = 14.80,
                durationMinutes = 95,
                elevationGainM = 25,
                difficulty = RouteDifficulty.HARD,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Đường Thanh Niên (chùa Trấn Quốc)",
                areaName = "Tây Hồ",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0475, 105.8365),
                routesInAreaCount = 8,
                runnersCount = 3850,
                imageUrl = "https://images.unsplash.com/photo-1530549387789-4c1017266635?w=400",
                routePoints = listOf(
                    21.0475 to 105.8365,
                    21.0560 to 105.8290,
                    21.0630 to 105.8220,
                    21.0680 to 105.8150,
                    21.0600 to 105.8080,
                    21.0490 to 105.8120,
                    21.0430 to 105.8240,
                    21.0475 to 105.8365
                )
            ),

            SuggestedRoute(
                id = "walk_1",
                title = "Đi bộ ngắm phố cổ & Cầu Long Biên lịch sử",
                sportType = SportType.WALK,
                distanceKm = 4.20,
                durationMinutes = 55,
                elevationGainM = 12,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.PAVED,
                startLocationName = "Ô Quan Chưởng",
                areaName = "Hoàn Kiếm - Long Biên",
                isPersonalized = true,
                isSaved = false,
                startPoint = Pair(21.0360, 105.8540),
                routesInAreaCount = 4,
                runnersCount = 3120,
                imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=400",
                routePoints = listOf(
                    21.0360 to 105.8540,
                    21.0390 to 105.8560,
                    21.0420 to 105.8600,
                    21.0435 to 105.8670,
                    21.0420 to 105.8600,
                    21.0360 to 105.8540
                )
            ),

            SuggestedRoute(
                id = "trail_1",
                title = "Phố Khương Trung - Ngách 29/78 Khương Hạ",
                sportType = SportType.TRAIL_RUN,
                distanceKm = 6.88,
                durationMinutes = 50,
                elevationGainM = 45,
                difficulty = RouteDifficulty.EASY,
                surfaceType = SurfaceType.MIXED,
                startLocationName = "Vị trí hiện tại",
                areaName = "Khương Trung",
                isPersonalized = true,
                isSaved = true,
                startPoint = Pair(userLat, userLng),
                routesInAreaCount = 1,
                runnersCount = 380,
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=400",
                routePoints = listOf(
                    21.0028 to 105.8188,
                    21.0000 to 105.8175,
                    20.9960 to 105.8178,
                    20.9895 to 105.8162,
                    20.9930 to 105.8190,
                    20.9985 to 105.8218,
                    21.0020 to 105.8210,
                    21.0028 to 105.8188
                )
            )
        )
    }
}
