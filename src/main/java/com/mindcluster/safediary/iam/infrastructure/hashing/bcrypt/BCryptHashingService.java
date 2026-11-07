package com.mindcluster.safediary.iam.infrastructure.hashing.bcrypt;

import com.mindcluster.safediary.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptHashingService implements HashingService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String raw) { return encoder.encode(raw); }

    public boolean matches(String raw, String hash) { return raw != null && hash != null && encoder.matches(raw, hash); }
}
