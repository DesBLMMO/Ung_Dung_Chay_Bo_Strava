package com.example.ung_dung_chay_bo.core.utils

import android.util.Log
import com.example.ung_dung_chay_bo.domain.model.SportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object RoadSnapService {
    private const val TAG = "RoadSnapService"

    private val routePointsCache = ConcurrentHashMap<String, List<Pair<Double, Double>>>()

    suspend fun snapToRealRoads(
        routeId: String,
        waypoints: List<Pair<Double, Double>>,
        sportType: SportType = SportType.RUN
    ): List<Pair<Double, Double>> = withContext(Dispatchers.IO) {
        if (waypoints.size < 2) return@withContext waypoints

        routePointsCache[routeId]?.let { cached ->
            if (cached.size > waypoints.size) {
                return@withContext cached
            }
        }

        val profile = if (sportType == SportType.RIDE) "bicycle" else "foot"

        val fullSnapped = fetchRouteGeometry(waypoints, profile)
        if (fullSnapped.size >= waypoints.size * 2) {
            Log.d(TAG, "Lộ trình $routeId đã được snap toàn bộ: ${fullSnapped.size} điểm")
            routePointsCache[routeId] = fullSnapped
            return@withContext fullSnapped
        }

        try {
            val segmentResult = ArrayList<Pair<Double, Double>>()
            for (i in 0 until waypoints.size - 1) {
                val pair = listOf(waypoints[i], waypoints[i + 1])
                val segmentPoints = fetchRouteGeometry(pair, profile)
                if (segmentPoints.size >= 2) {
                    if (segmentResult.isNotEmpty()) {
                        segmentResult.addAll(segmentPoints.drop(1))
                    } else {
                        segmentResult.addAll(segmentPoints)
                    }
                } else {
                    if (segmentResult.isEmpty()) segmentResult.add(waypoints[i])
                    segmentResult.add(waypoints[i + 1])
                }
            }

            if (segmentResult.size > waypoints.size) {
                Log.d(TAG, "Lộ trình $routeId đã được snap theo từng chặng: ${segmentResult.size} điểm")
                routePointsCache[routeId] = segmentResult
                return@withContext segmentResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi snap từng chặng cho $routeId: ${e.message}")
        }

        waypoints
    }

    private fun fetchRouteGeometry(
        points: List<Pair<Double, Double>>,
        profile: String
    ): List<Pair<Double, Double>> {
        if (points.size < 2) return emptyList()

        try {
            val coordinatesParam = points.joinToString(";") { (lat, lng) ->
                String.format(Locale.US, "%.6f,%.6f", lng, lat)
            }

            val urlString = "https://router.project-osrm.org/route/v1/$profile/$coordinatesParam?overview=full&geometries=geojson"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "UngDungChayBo/1.0 (Android)")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val code = root.optString("code")
                if (code == "Ok") {
                    val routesArray = root.optJSONArray("routes")
                    if (routesArray != null && routesArray.length() > 0) {
                        val firstRoute = routesArray.getJSONObject(0)
                        val geometry = firstRoute.optJSONObject("geometry")
                        val coordsArray = geometry?.optJSONArray("coordinates")

                        if (coordsArray != null && coordsArray.length() > 0) {
                            val result = ArrayList<Pair<Double, Double>>(coordsArray.length())
                            for (i in 0 until coordsArray.length()) {
                                val coord = coordsArray.getJSONArray(i)
                                val lng = coord.getDouble(0)
                                val lat = coord.getDouble(1)
                                result.add(lat to lng)
                            }
                            return result
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchRouteGeometry thất bại: ${e.message}")
        }
        return emptyList()
    }
}
