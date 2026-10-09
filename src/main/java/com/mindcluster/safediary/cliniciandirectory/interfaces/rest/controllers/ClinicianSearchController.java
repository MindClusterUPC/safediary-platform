package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.controllers;

import com.mindcluster.safediary.cliniciandirectory.application.commandservices.DirectoryCommandService;
import com.mindcluster.safediary.cliniciandirectory.application.queryservices.DirectoryQueryService;
import com.mindcluster.safediary.cliniciandirectory.application.internal.outboundservices.iam.IamRoleClient;
import com.mindcluster.safediary.cliniciandirectory.domain.model.commands.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.queries.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources.*;
import com.mindcluster.safediary.cliniciandirectory.interfaces.rest.transform.DirectoryResourceAssembler;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
@RestController @RequiredArgsConstructor
@RequestMapping(value="/api/v1/clinicians", produces=MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Clinician Directory - Search")
public class ClinicianSearchController {
    private final DirectoryQueryService queries;
    @GetMapping @Operation(summary="Search approved, published clinicians by name, specialty and price")
    public ResponseEntity<?> search(@RequestParam(required=false) String text,
            @RequestParam(required=false) String specialty, @RequestParam(required=false) BigDecimal maxAmount,
            @RequestParam(required=false) String currency, @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(queries.handle(new SearchCliniciansQuery(text, specialty, maxAmount, currency, page, size))
                .stream().map(DirectoryResourceAssembler::profile).toList());
    }
    @GetMapping("/{id}") @Operation(summary="Read a verified, published public profile")
    public ResponseEntity<ProfessionalProfileResource> profile(@PathVariable Long id) {
        return queries.handle(new GetClinicianProfileQuery(id)).map(DirectoryResourceAssembler::profile)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/rating") @Operation(summary="Read aggregate rating; average is null when no reviews exist")
    public ResponseEntity<?> rating(@PathVariable Long id) {
        return ResponseEntity.ok(DirectoryResourceAssembler.rating(queries.handle(new GetRatingSummaryQuery(id))));
    }
    @GetMapping("/{id}/reviews") @Operation(summary="List anonymous published reviews and helpful counts")
    public ResponseEntity<?> reviews(@PathVariable Long id) {
        return ResponseEntity.ok(queries.handle(new GetClinicianReviewsQuery(id)).stream()
                .map(r -> DirectoryResourceAssembler.review(r, queries.helpfulCount(r.getId()))).toList());
    }
}
