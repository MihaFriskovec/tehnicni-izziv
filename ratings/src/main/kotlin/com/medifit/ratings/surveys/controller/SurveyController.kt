package com.medifit.ratings.surveys.controller

import com.medifit.ratings.surveys.dto.SubmitSurveyDto
import com.medifit.ratings.surveys.dto.SurveyDto
import com.medifit.ratings.surveys.model.Survey
import com.medifit.ratings.surveys.service.SurveyService
import com.medifit.sharedlib.IdTransformer
import com.medifit.sharedlib.dto.ListResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.validation.Valid

@RestController
@RequestMapping("/surveys")
class SurveyController(private val surveyService: SurveyService) {

    @GetMapping("/{case}")
    fun survey(principal: Principal, @PathVariable case: String): ResponseEntity<SurveyDto> {
        return ResponseEntity.ok(
            surveyService.getSurveyByCaseAndPatient(
                patient = principal.name.toLong(),
                caseId = IdTransformer.toInternal(case)
            ).toDto()
        )
    }

    @PostMapping("/submit")
    fun updateSurvey(@RequestBody @Valid submitSurveyDto: SubmitSurveyDto): ResponseEntity<SurveyDto> {
        val survey = surveyService.submitSurvey(
            case = IdTransformer.toInternal(submitSurveyDto.caseId!!),
            doctor = IdTransformer.toInternal(submitSurveyDto.doctorId!!),
            patient = IdTransformer.toInternal(submitSurveyDto.patientId!!),
            caseDateTime = submitSurveyDto.caseDateTime!!,
            rating = submitSurveyDto.rating!!
        )

        return ResponseEntity.ok(survey.toDto())
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    fun surveys(): ResponseEntity<ListResponseDto<SurveyDto>> {
        return ResponseEntity.ok(ListResponseDto(surveyService.getSurveys().map { it.toDto() }))
    }

    fun Survey.toDto() = SurveyDto(
        id = IdTransformer.toExternal(id),
        case = IdTransformer.toExternal(appointment),
        doctor = IdTransformer.toExternal(doctor),
        patient = IdTransformer.toExternal(patient),
        rating = rating.toString()
    )
}
