// Version management for HydroTracker
object AppVersion {
    const val major = 1
    const val minor = 0
    const val patch = 0
    
    // Build number from CI or local development
    private val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0
    
    val versionCode = major * 10000 + minor * 100 + patch + buildNumber
    val versionName = if (buildNumber > 0) {
        "$major.$minor.$patch-build.$buildNumber"
    } else {
        "$major.$minor.$patch-dev"
    }
    
    fun getVersionInfo(): String {
        return """
            Version Name: $versionName
            Version Code: $versionCode
            Major: $major
            Minor: $minor  
            Patch: $patch
            Build: $buildNumber
        """.trimIndent()
    }
}