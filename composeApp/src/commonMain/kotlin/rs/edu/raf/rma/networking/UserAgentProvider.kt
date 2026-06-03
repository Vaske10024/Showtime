package rs.edu.raf.rma.networking


object UserAgentProvider {
    val USER_AGENT: String = "RMA-App ${getUserAgentPlatformName()}"
}

expect fun getUserAgentPlatformName(): String
