package com.mindcluster.safediary.iam.application.internal.outboundservices.hashing;

/** One-way hashing for passwords, vault PINs and recovery codes. */
public interface HashingService {
    String encode(String raw);
    boolean matches(String raw, String hash);
}
