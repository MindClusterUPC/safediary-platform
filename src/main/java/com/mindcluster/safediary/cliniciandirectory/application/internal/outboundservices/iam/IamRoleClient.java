package com.mindcluster.safediary.cliniciandirectory.application.internal.outboundservices.iam;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.DirectoryActor;
/** Replace this port with trusted IAM authentication when that context is available. */
public interface IamRoleClient { DirectoryActor currentActor(); }
