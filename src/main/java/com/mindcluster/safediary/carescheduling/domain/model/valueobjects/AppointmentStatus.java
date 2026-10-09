package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

/** REQUESTED is a schedule proposal; HELD and CONFIRMED occupy the clinician slot. */
public enum AppointmentStatus { REQUESTED, HELD, CONFIRMED, CANCELLED, EXPIRED, COMPLETED }
