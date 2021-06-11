import aleksa.mosis.elfak.capturetheflag.User
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

//
//class UserLocation : Parcelable {
//    private var user: User? = null
//    var geo_point: GeoPoint? = null
//
//    @ServerTimestamp
//    private var timestamp: Date? = null
//
//    constructor(user: User?, geo_point: GeoPoint?, timestamp: Date?) {
//        this.user = user
//        this.geo_point = geo_point
//        this.timestamp = timestamp
//    }
//
////    constructor() {}
////    protected constructor(`in`: Parcel) {
////        user = `in`.readParcelable(User::class.java.getClassLoader())
////    }
//
//    fun getUser(): User? {
//        return user
//    }
//
//    fun setUser(user: User?) {
//        this.user = user
//    }
//
//    fun getTimestamp(): Date? {
//        return timestamp
//    }
//
//    fun setTimestamp(timestamp: Date?) {
//        this.timestamp = timestamp
//    }
//
////    override fun toString(): String {
////        return "UserLocation{" +
////                "user=" + user +
////                ", geo_point=" + geo_point +
////                ", timestamp=" + timestamp +
////                '}'
////    }
////
////    override fun describeContents(): Int {
////        return 0
////    }
////
////    override fun writeToParcel(dest: Parcel, flags: Int) {
////        dest.writeParcelable(user, flags)
////    }
//////
////    companion object {
////        val CREATOR: Creator<UserLocation> = object : Creator<UserLocation?> {
////            override fun createFromParcel(`in`: Parcel): UserLocation? {
////                return UserLocation(`in`)
////            }
////
////            override fun newArray(size: Int): Array<UserLocation?> {
////                return arrayOfNulls(size)
////            }
////        }
////    }
//}