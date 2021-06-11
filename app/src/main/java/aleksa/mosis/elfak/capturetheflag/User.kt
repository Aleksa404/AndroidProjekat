package aleksa.mosis.elfak.capturetheflag



data class User(var id : String,
                var username: String,
                var email : String,
                var name: String,
                var surname: String,
                var phone: String
                )
{
    var matches : Int = 0
    var won : Int = 0
    var flags : Int = 0
    var friends : List<User>? = null

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
