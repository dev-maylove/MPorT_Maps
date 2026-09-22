package id.mport.maps.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import id.mport.maps.domain.measurement.MeasurementPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationManager(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun current(): MeasurementPoint? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }

        fun resumeOnce(value: MeasurementPoint?) {
            if (cont.isActive) cont.resume(value)
        }

        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    resumeOnce(
                        MeasurementPoint(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracy = loc.accuracy,
                            altitude = loc.altitude,
                            bearing = loc.bearing
                        )
                    )
                } else {
                    // Fallback: last known location
                    client.lastLocation
                        .addOnSuccessListener { last ->
                            resumeOnce(
                                last?.let {
                                    MeasurementPoint(
                                        latitude = it.latitude,
                                        longitude = it.longitude,
                                        accuracy = it.accuracy,
                                        altitude = it.altitude,
                                        bearing = it.bearing
                                    )
                                }
                            )
                        }
                        .addOnFailureListener { resumeOnce(null) }
                }
            }
            .addOnFailureListener {
                client.lastLocation
                    .addOnSuccessListener { last ->
                        resumeOnce(
                            last?.let {
                                MeasurementPoint(
                                    latitude = it.latitude,
                                    longitude = it.longitude,
                                    accuracy = it.accuracy,
                                    altitude = it.altitude,
                                    bearing = it.bearing
                                )
                            }
                        )
                    }
                    .addOnFailureListener { resumeOnce(null) }
            }
    }
}
