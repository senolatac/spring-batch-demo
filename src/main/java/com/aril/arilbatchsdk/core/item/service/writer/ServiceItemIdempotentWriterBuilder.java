package com.aril.arilbatchsdk.core.item.service.writer;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.service.IdempotentService;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.valhala.batch.IdempotentBatchItem;
import org.springframework.util.Assert;

import java.util.List;

public class ServiceItemIdempotentWriterBuilder<T extends IdempotentBatchItem> {
    private String name = "serviceItemWriter";
    private IdempotencyOptions idempotencyOptions = IdempotencyOptions.builder().build();
    private IdempotentService<T> service;
    private IdempotentWriter idempotentWriter;
    private String methodName;
    private List<?> arguments;

    /**
     * The name used to calculate the key within the
     *
     * @param name name of the writer instance
     * @return The current instance of the builder.
     */
    public ServiceItemIdempotentWriterBuilder<T> name(String name) {
        this.name = name;

        return this;
    }

    /**
     * idempotency in reusable steps and exceptional cases.
     *
     * @param idempotencyOptions item-idempotency-options
     * @return The current instance of the builder.
     * @see ServiceItemIdempotentWriter#setIdempotencyOptions(IdempotencyOptions)
     */
    public ServiceItemIdempotentWriterBuilder<T> idempotencyOptions(IdempotencyOptions idempotencyOptions) {
        this.idempotencyOptions = idempotencyOptions;

        return this;
    }

    /**
     * @param idempotentWriter idempotent-writer
     * @return The current instance of the builder.
     * @see ServiceItemIdempotentWriter#setIdempotentWriter(IdempotentWriter)
     */
    public ServiceItemIdempotentWriterBuilder<T> idempotentWriter(IdempotentWriter idempotentWriter) {
        this.idempotentWriter = idempotentWriter;

        return this;
    }

    /**
     * The {@link IdempotentService}
     * implementation used to read input from.
     *
     * @param service underlying service for input to be write from.
     * @return The current instance of the builder.
     * @see ServiceItemIdempotentWriter#setService(IdempotentService)
     */
    public ServiceItemIdempotentWriterBuilder<T> service(IdempotentService<T> service) {
        this.service = service;

        return this;
    }

    /**
     * Specifies what method on the repository to call. This method must have the type of
     * object passed to this writer as the <em>sole</em> argument.
     *
     * @param methodName the name of the method to be used for saving the item.
     * @return The current instance of the builder.
     * @see ServiceItemIdempotentWriter#setMethodName(String)
     */
    public ServiceItemIdempotentWriterBuilder<T> methodName(String methodName) {
        this.methodName = methodName;

        return this;
    }

    /**
     * Arguments to be passed to the data providing method.
     *
     * @param arguments list of method arguments to be passed to the repository.
     * @return The current instance of the builder.
     * @see ServiceItemIdempotentWriter#setArguments(List)
     */
    public ServiceItemIdempotentWriterBuilder<T> arguments(List<?> arguments) {
        this.arguments = arguments;

        return this;
    }

    /**
     * Builds the {@link ServiceItemIdempotentWriter}.
     *
     * @return a {@link ServiceItemIdempotentWriter}
     */
    public ServiceItemIdempotentWriter<T> build() {
        Assert.notNull(this.service, "service is required.");
        Assert.hasText(this.methodName, "methodName must not be empty.");

        ServiceItemIdempotentWriter<T> writer = new ServiceItemIdempotentWriter<>();
        writer.setName(name);
        writer.setIdempotencyOptions(this.idempotencyOptions);
        writer.setService(this.service);
        writer.setIdempotentWriter(idempotentWriter);
        writer.setMethodName(this.methodName);
        writer.setArguments(this.arguments);
        return writer;
    }
}
