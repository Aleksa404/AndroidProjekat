package aleksa.mosis.elfak.capturetheflag.data

import java.time.LocalTime
import kotlin.time.ExperimentalTime

data class Game @ExperimentalTime constructor(
            var owner : String = "",
            var start : LocalTime ?= null,
            var duration : Int = 0,
            var password : String = ""
){
//    var id : String = ""
}
