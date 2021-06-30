package aleksa.mosis.elfak.capturetheflag.data


data class User(var id : String = "",
                var username: String = "",
                var email : String = "",
                var name: String = "",
                var surname: String = "",
                var phone : String = ""
                )
{



    var latitude: Double = 0.0
    var longitude: Double = 0.0

    var photoUri: String = ""
    var matches : Int = 0
    var won : Int = 0
    var flags : Int = 0
    var friends : ArrayList<User>? = ArrayList()

    fun addWin() {
        won++
    }
    fun addMatch() {
        matches++
    }
    fun addFlag(){
        flags++
    }




}
