package aleksa.mosis.elfak.capturetheflag.data

import java.time.LocalTime
import kotlin.time.ExperimentalTime

data class Game @ExperimentalTime constructor(
            var owner : String = "",
            var duration : Int = 0,
            var password : String = "",
            var flags : ArrayList<Flag>? = ArrayList(),
            var players: ArrayList<User> = ArrayList(),
            var started: Boolean = false

){
      var start : HashMap<String, Int> = HashMap()
}
