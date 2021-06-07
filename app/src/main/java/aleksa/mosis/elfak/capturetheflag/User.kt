package aleksa.mosis.elfak.capturetheflag

data class User(var id : String,
                var username: String,
                var email : String
)
{
    var wins : Int = 0
    var loses : Int = 0
    var score : Int = 0
    var friends : List<User>? = null

    fun addWin() {
        wins++
    }
    fun addLose(){
        loses++
    }
}
