package es.instituto.practicapermisos


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class MainActivity : AppCompatActivity() {


    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestCamera: ActivityResultLauncher<Void?>


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adaptador: AdaptadorEntrada

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private var lastlocation: Location? = null

    // pone los permisos en un array para
    // despues poder pedir multiples permisos
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Metodo que se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        this.configLocation()
        this.configRequests()

        adaptador = AdaptadorEntrada(this)
        findViewById<ListView>(R.id.list_view).adapter = adaptador

        findViewById<Button>(R.id.b_peticion).setOnClickListener {
            // Verifica si tiene permiso de ubicación
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Inicia las actualizaciones de ubicación
                startLocationUpdates()

                // Verifica si tiene el permiso de cámara
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Lanza la camara
                    requestCamera.launch(null)
                } else {
                    // Lanza la solicitud de permisos
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
                }
            } else {
                // Lanza la solicitud de permisos si no tiene el permiso de ubicación
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
        }
    }

    // ya que antes de iniciar actualizaciones de ubicacion
    // compruebo si tengo permisos, no lo pregunto otra vez aqui
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Verifica si la solicitud de ubicación y el callback no son nulos
        locationRequest?.let { request ->
            locationCallback?.let { callback ->
                // Si no son nulos, solicita actualizaciones
                // de ubicación al cliente de ubicación fusionada
                fusedLocationClient.requestLocationUpdates(request, callback, null)
            }
        }
    }

    private fun configLocation() {
        //el servidor
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //la configuración de la actualización, se asocia a posteriori
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000)
            .build()
        //que se ejecuta cuando se actualiza
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                //se obtiene la última coordenada
                lastlocation = p0.lastLocation
            }
        }
        //se asocia el servicio al tratamiento de la actualización
        fusedLocationClient.removeLocationUpdates(this.locationCallback as LocationCallback)

    }

    private fun handleImage(bitmap: Bitmap) {
        // coje las coordenadas
        val currentLocation = lastlocation
        // crea una nueva entrada
        val newEntrada = Entrada(bitmap, currentLocation)
        // mete la nueva entrada en el lisview y lo actualiza
        adaptador.add(newEntrada)
        adaptador.notifyDataSetChanged()
    }

    private fun configRequests() {
        //los dos launcher para permisos y fotografía
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(
                ), {
                    //no es necesario el tratamiento de esta actividad
                }
            )
        //se abre la cámara
        requestCamera = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) {
            it?.let { bitmap ->
                handleImage(bitmap)
            }
        }
    }
}