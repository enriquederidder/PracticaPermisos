package es.instituto.practicapermisos

import android.graphics.Bitmap
import android.location.Location

class Entrada {

    var imagen: Bitmap
    var location: Location?

    constructor(
        imagen: Bitmap,
        location: Location?,

        ) {
        this.location = location
        this.imagen = imagen

    }
}