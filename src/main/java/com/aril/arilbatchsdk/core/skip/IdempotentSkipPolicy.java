package com.aril.arilbatchsdk.core.skip;

import com.aril.arilidempotentsdk.exception.ArilIdempotentException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class IdempotentSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        return t instanceof ArilIdempotentException;
    }
}
