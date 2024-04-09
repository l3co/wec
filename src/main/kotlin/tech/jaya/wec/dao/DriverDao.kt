package tech.jaya.wec.dao

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import tech.jaya.wec.dao.exception.EntityNotFoundException
import tech.jaya.wec.model.Car
import tech.jaya.wec.model.Driver
import java.util.ResourceBundle

/**
 * This class is responsible for performing CRUD operations on the Driver entity.
 *
 * @property jdbcTemplate used to interact with the database.
 */
@Repository
class DriverDao(private val jdbcTemplate: JdbcTemplate) : Dao<Driver> {

    private val queries = ResourceBundle.getBundle("sql-queries")

    private val simpleJdbcInsert: SimpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
        .withTableName("drivers")
        .usingGeneratedKeyColumns("id")

    private val rowMapper = RowMapper { rs, _ ->
        val carId = rs.getObject("car_id")
        val car = carId?.let {
            Car(
                id = rs.getLong("car_id"),
                licensePlate = rs.getString("license_plate") ?: "",
                model = rs.getString("model") ?: "",
                color = rs.getString("color") ?: ""
            )
        }

        Driver(
            id = rs.getLong("id"),
            name = rs.getString("name") ?: "",
            available = rs.getBoolean("available"),
            car = car
        )
    }

    /**
     * Retrieves all drivers from the database.
     *
     * @return a list of all drivers.
     */
    override fun findAll(): List<Driver> {
        val sql = queries.getString("DriverDao.findAll")
        return jdbcTemplate.query(sql, rowMapper)
    }

    /**
     * Retrieves a driver by its ID.
     *
     * @param id the ID of the driver to retrieve.
     * @return the driver if found, null otherwise.
     */
    override fun findById(id: Long): Driver? {
        return try {
            val sql = queries.getString("DriverDao.findById")
            jdbcTemplate.queryForObject(sql, rowMapper, id)
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    /**
     * Saves a driver to the database. If the driver already exists, it is updated.
     *
     * @param entity the driver to save.
     * @return the saved driver.
     */
    override fun save(entity: Driver): Driver = entity.id?.let {
        update(entity)
    } ?: run {
        insert(entity)
    }

    /**
     * Updates an existing driver in the database.
     *
     * @param driver the driver to update.
     * @return the updated driver.
     * @throws EntityNotFoundException if the driver does not exist.
     */
    private fun update(driver: Driver): Driver {
        val existingId = driver.id!!
        findById(existingId) ?: throw EntityNotFoundException("Driver with id $existingId not found")
        val sql = queries.getString("DriverDao.save.update")
        jdbcTemplate.update(
            sql,
            driver.name,
            driver.available,
            driver.car?.id,
            existingId
        )
        return driver
    }

    /**
     * Inserts a new driver into the database.
     *
     * @param driver the driver to insert.
     * @return the inserted driver with its new ID.
     */
    private fun insert(driver: Driver): Driver {
        val parameters = HashMap<String, Any>(3)
        parameters["name"] = driver.name
        parameters["available"] = driver.available

        driver.car?.run {
            parameters["car_id"] = this.id!!
        }

        val newId = simpleJdbcInsert.executeAndReturnKey(parameters).toLong()

        return driver.copy(id = newId)
    }

    /**
     * Deletes a driver by its ID.
     *
     * @param id the ID of the driver to delete.
     */
    override fun deleteById(id: Long) {
        findById(id) ?: throw EntityNotFoundException("Driver with id $id not found")
        val sql = queries.getString("DriverDao.deleteById")
        jdbcTemplate.update(sql, id)
    }
}