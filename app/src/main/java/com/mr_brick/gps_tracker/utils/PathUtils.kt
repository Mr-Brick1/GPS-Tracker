package com.mr_brick.gps_tracker.utils

import org.osmdroid.util.GeoPoint
import kotlin.math.abs
import kotlin.math.sqrt

object PathUtils {

    /**
     * Simplifies a list of points using the Douglas-Peucker algorithm.
     * @param points The original list of GeoPoints.
     * @param tolerance The maximum distance (in meters approximately) a point can be from the line.
     */
    fun simplifyPath(points: List<GeoPoint>, tolerance: Double): List<GeoPoint> {
        if (points.size < 3) return points

        var maxDist = 0.0
        var index = 0
        val end = points.size - 1

        for (i in 1 until end) {
            val dist = perpendicularDistance(points[i], points[0], points[end])
            if (dist > maxDist) {
                index = i
                maxDist = dist
            }
        }

        return if (maxDist > tolerance) {
            val res1 = simplifyPath(points.subList(0, index + 1), tolerance)
            val res2 = simplifyPath(points.subList(index, points.size), tolerance)
            res1.dropLast(1) + res2
        } else {
            listOf(points[0], points[end])
        }
    }

    private fun perpendicularDistance(pt: GeoPoint, start: GeoPoint, end: GeoPoint): Double {
        val dx = end.latitude - start.latitude
        val dy = end.longitude - start.longitude

        val mag = sqrt(dx * dx + dy * dy)
        if (mag == 0.0) return sqrt(
            (pt.latitude - start.latitude) * (pt.latitude - start.latitude) +
                    (pt.longitude - start.longitude) * (pt.longitude - start.longitude)
        )

        val u = ((pt.latitude - start.latitude) * dx + (pt.longitude - start.longitude) * dy) / (mag * mag)

        val closestLat: Double
        val closestLon: Double

        if (u < 0) {
            closestLat = start.latitude
            closestLon = start.longitude
        } else if (u > 1) {
            closestLat = end.latitude
            closestLon = end.longitude
        } else {
            closestLat = start.latitude + u * dx
            closestLon = start.longitude + u * dy
        }

        // Обратное расстояние в градусах, измеренное примерно в метрах (очень приблизительно, но для упрощения подходит)
        // 1 градус равен примерно 111 км
        return sqrt((pt.latitude - closestLat) * (pt.latitude - closestLat) +
                (pt.longitude - closestLon) * (pt.longitude - closestLon)) * 111000.0
    }

    /**
     * Creates a smooth spline through the points using Catmull-Rom interpolation.
     * @param points Original points
     * @param subdivisions How many points to generate between each pair of original points
     */
    fun createSmoothPath(points: List<GeoPoint>, subdivisions: Int = 4): List<GeoPoint> {
        if (points.size < 3) return points

        val smoothedPoints = mutableListOf<GeoPoint>()
        
        // Add virtual control points at start and end to handle boundaries
        val workingPoints = mutableListOf<GeoPoint>()
        workingPoints.add(calculateVirtualPoint(points[0], points[1]))
        workingPoints.addAll(points)
        workingPoints.add(calculateVirtualPoint(points[points.size - 1], points[points.size - 2]))

        for (i in 1 until workingPoints.size - 2) {
            val p0 = workingPoints[i - 1]
            val p1 = workingPoints[i]
            val p2 = workingPoints[i + 1]
            val p3 = workingPoints[i + 2]

            for (j in 0 until subdivisions) {
                val t = j.toDouble() / subdivisions
                smoothedPoints.add(catmullRomInterpolate(p0, p1, p2, p3, t))
            }
        }
        smoothedPoints.add(points.last())
        return smoothedPoints
    }

    private fun calculateVirtualPoint(p1: GeoPoint, p2: GeoPoint): GeoPoint {
        return GeoPoint(p1.latitude + (p1.latitude - p2.latitude), p1.longitude + (p1.longitude - p2.longitude))
    }

    private fun catmullRomInterpolate(p0: GeoPoint, p1: GeoPoint, p2: GeoPoint, p3: GeoPoint, t: Double): GeoPoint {
        val t2 = t * t
        val t3 = t2 * t

        val lat = 0.5 * ((2.0 * p1.latitude) +
                (-p0.latitude + p2.latitude) * t +
                (2.0 * p0.latitude - 5.0 * p1.latitude + 4.0 * p2.latitude - p3.latitude) * t2 +
                (-p0.latitude + 3.0 * p1.latitude - 3.0 * p2.latitude + p3.latitude) * t3)

        val lon = 0.5 * ((2.0 * p1.longitude) +
                (-p0.longitude + p2.longitude) * t +
                (2.0 * p0.longitude - 5.0 * p1.longitude + 4.0 * p2.longitude - p3.longitude) * t2 +
                (-p0.longitude + 3.0 * p1.longitude - 3.0 * p2.longitude + p3.longitude) * t3)

        return GeoPoint(lat, lon)
    }
}
