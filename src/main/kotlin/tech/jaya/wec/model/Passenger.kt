/**
 * A data class representing a Passenger in the system.
 *
 * @property id The unique identifier of the passenger. It's nullable because it can be absent when a Passenger object is created before being saved to the database.
 * @property name The name of the passenger. It's a non-nullable String.
 */
package tech.jaya.wec.model

data class Passenger(
    val id: Long? = null,
    val name: String
)