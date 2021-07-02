package aleksa.mosis.elfak.capturetheflag.data

import com.google.android.gms.maps.model.Marker

data class Flag(
                var longitude: Double = 0.0,
                var latitude : Double = 0.0,
                var value: Int = 0,
                var marker: Marker? = null
)
{

}