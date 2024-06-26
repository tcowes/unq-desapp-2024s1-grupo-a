package ar.edu.unq.desapp.grupoa.backenddesappapi.webservice.dtos

import ar.edu.unq.desapp.grupoa.backenddesappapi.model.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserDTO(
    @field:Size(min = 3, max = 30)
    val name: String,

    @field:Size(min = 3, max = 30)
    val surname: String,

    @field:Email
    val email: String,

    @field:Size(min = 10, max = 30)
    val address: String,

    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{6,}\$")
    val password: String,

    @field:Size(min = 22, max = 22)
    val cvu: String,

    @field:Size(min = 8, max = 8)
    val walletAddress: String
) {
    fun toModel(): User {
        return User(this.name, this.surname, this.email, this.address, this.password, this.cvu, this.walletAddress)
    }
}

data class ExposedUserDTO(
    val id: Long?,
    val name: String,
    val surname: String,
    val email: String,
    val address: String,
    val cvu: String,
    val walletAddress: String
) {
    companion object {
        fun fromModel(user: User): ExposedUserDTO {
            return ExposedUserDTO(
                user.id,
                user.name,
                user.surname,
                user.email,
                user.address,
                user.cvu,
                user.walletAddress
            )
        }
    }
}

data class LoginUserDTO(
    val email: String,
    val password: String,
)
