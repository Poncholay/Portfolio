package me.guillaumewilmot.api.controllers

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import me.guillaumewilmot.api.HTTP_200_MSG
import me.guillaumewilmot.api.HTTP_404_MSG
import me.guillaumewilmot.api.models.ErrorResponseModel
import me.guillaumewilmot.api.models.LiftModel
import me.guillaumewilmot.api.models.ResponseModel
import me.guillaumewilmot.api.services.LiftService
import me.guillaumewilmot.api.to

@KtorExperimentalLocationsAPI
@Suppress("FunctionName")
fun Route.LiftController(liftService: LiftService) {
    route("/lift") {
        /**
         * @returns all lifts
         */
        get("/") {
            val lifts = liftService.all()
            call.respond(ResponseModel(HTTP_200_MSG, lifts))
        }

        /**
         * @returns one lift or null
         */
        post("/") {
            val requestBody = call.receiveText()
            call.respond(ResponseModel(HTTP_200_MSG, requestBody.to<LiftModel>()))
        }

        /**
         * @returns one lift or null
         * @param id Id of the lift
         */
        @Location("/{id}")
        data class LiftId(val id: Long)
        get<LiftId> {
            liftService.one(it.id)?.let { lift ->
                return@get call.respond(ResponseModel(HTTP_200_MSG, lift))
            }
            call.respond(HttpStatusCode.NotFound, ErrorResponseModel(HTTP_404_MSG))
        }
    }
}