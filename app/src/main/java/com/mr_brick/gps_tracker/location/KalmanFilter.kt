package com.mr_brick.gps_tracker.location

class KalmanFilter {
    private var minAccuracy = 1f
    private var lastTime: Long = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var variance: Float = -1f // Дисперсия - это степень неопределенности

    /**
     * Filters a location point.
     * @param lat Raw latitude
     * @param lon Raw longitude
     * @param accuracy Raw accuracy in meters
     * @param time Timestamp in milliseconds
     * @return Array containing [filteredLat, filteredLon]
     */
    fun filter(lat: Double, lon: Double, accuracy: Float, time: Long): DoubleArray {
        var acc = accuracy
        if (acc < minAccuracy) acc = minAccuracy

        if (variance < 0) {
            // Инициализация фильтра
            lastTime = time
            latitude = lat
            longitude = lon
            variance = acc * acc
        } else {
            val duration = time - lastTime
            if (duration > 0) {
                // С течением времени мы менее уверены в своем местоположении
                // Добавляем некоторый "технологический шум" (неопределенность скорости).
                variance += duration.toFloat() * PROCESS_NOISE / 1000f
                lastTime = time
            }
            // Коэффициент Калмана (насколько мы доверяем новому измерению по сравнению с текущей оценкой)
            val k = variance / (variance + acc * acc)
            latitude += k * (lat - latitude)
            longitude += k * (lon - longitude)
            variance *= (1 - k)
        }
        return doubleArrayOf(latitude, longitude)
    }

    fun reset() {
        variance = -1f
    }

    companion object {
        private const val PROCESS_NOISE = 3.0f // Измените это значение, чтобы изменить "отзывчивость".
    }
}
