package com.mindcluster.safediary.iam.interfaces.acl.dto;

/** Published identity of an account: no credentials, only id, e-mail, role and status. */
public record IamAccountDto(Long accountId, String email, String role, String status) {}
