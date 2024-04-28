package uk.ac.tees.mad.d3656925.domain

data class UserResponse(
    val item: CurrentUser?,
    val key: String?
) {
    data class CurrentUser(
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val profileImage: String = "",
        val lastTrips: String = "",
        val carNumber: String = "",
        val isActive: Boolean = false
    )
}