package ee.ut.cs.HEALTH.domain.model

import ee.ut.cs.HEALTH.R
import java.time.LocalDate

object Profile {
    var profilePicture: Int = R.drawable.default_profile_pic
    var nameOfUser: String = "John Doe"
    var emailOfUser: String = "example@email.com"
    var phoneNumber: String = "+372 5555 5555"
    var dateOfBirth: String = "01.01.2000"
    var description: String =
        "Lorem ipsum dolor sit amet consectetur adipiscing elit. Quisque faucibus ex sapien vitae pellentesque sem placerat. In id cursus mi pretium tellus duis convallis. Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere. Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia nostra inceptos himenaeos."

    var userHasSetTheirInfo: Boolean = false
}