package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.CrisisHotlineResource;

/**
 * Assembler from crisis hotline value object to resource.
 */
public final class CrisisHotlineResourceFromValueObjectAssembler {

    private CrisisHotlineResourceFromValueObjectAssembler() {
    }

    public static CrisisHotlineResource toResource(CrisisHotline hotline) {
        return new CrisisHotlineResource(hotline.name(), hotline.phone(), hotline.description());
    }
}
