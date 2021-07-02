package aleksa.mosis.elfak.capturetheflag.data

import android.graphics.Bitmap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserLocation(var uid: String, var latitude: Double, var longitude: Double, var marker: Marker?) {
    @Exclude @set:Exclude @get:Exclude  var photoUri: String? = ""
    @Exclude @set:Exclude @get:Exclude  var username: String = ""


}