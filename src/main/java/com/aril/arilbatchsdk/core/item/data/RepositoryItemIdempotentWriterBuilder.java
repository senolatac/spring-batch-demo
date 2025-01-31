package com.aril.arilbatchsdk.core.item.data;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
public class RepositoryItemIdempotentWriterBuilder<T extends IdempotentBatchItem> {

    private String name = "repositoryItemWriter";
    private IdempotencyOptions idempotencyOptions = IdempotencyOptions.builder().build();
    private CrudRepository<T, ?> repository;
    private IdempotentWriter idempotentWriter;
    private String methodName;
    private List<?> arguments;

    /**
     * The name used to calculate the key within the
     *
     * @param name name of the writer instance
     * @return The current instance of the builder.
     */
    public RepositoryItemIdempotentWriterBuilder<T> name(String name) {
        this.name = name;

        return this;
    }

    /**
     * idempotency in reusable steps and exceptional cases.
     *
     * @param idempotencyOptions item-idempotency-options
     * @return The current instance of the builder.
     * @see RepositoryItemIdempotentWriter#setIdempotencyOptions(IdempotencyOptions)
     */
    public RepositoryItemIdempotentWriterBuilder<T> idempotencyOptions(IdempotencyOptions idempotencyOptions) {
        this.idempotencyOptions = idempotencyOptions;

        return this;
    }

    /**
     * @param idempotentWriter idempotent-writer
     * @return The current instance of the builder.
     * @see RepositoryItemIdempotentWriter#setIdempotentWriter(IdempotentWriter)
     */
    public RepositoryItemIdempotentWriterBuilder<T> idempotentWriter(IdempotentWriter idempotentWriter) {
        this.idempotentWriter = idempotentWriter;

        return this;
    }

    /**
     * Specifies what method on the repository to call. This method must have the type of
     * object passed to this writer as the <em>sole</em> argument.
     *
     * @param methodName the name of the method to be used for saving the item.
     * @return The current instance of the builder.
     * @see RepositoryItemIdempotentWriter#setMethodName(String)
     */
    public RepositoryItemIdempotentWriterBuilder<T> methodName(String methodName) {
        this.methodName = methodName;

        return this;
    }

    /**
     * Arguments to be passed to the data providing method.
     *
     * @param arguments list of method arguments to be passed to the repository.
     * @return The current instance of the builder.
     * @see RepositoryItemIdempotentWriter#setArguments(List)
     */
    public RepositoryItemIdempotentWriterBuilder<T> arguments(List<?> arguments) {
        this.arguments = arguments;

        return this;
    }

    /**
     * Set the {@link org.springframework.data.repository.CrudRepository} implementation
     * for persistence
     *
     * @param repository the Spring Data repository to be set
     * @return The current instance of the builder.
     * @see RepositoryItemIdempotentWriter#setRepository(CrudRepository)
     */
    public RepositoryItemIdempotentWriterBuilder<T> repository(CrudRepository<T, ?> repository) {
        this.repository = repository;

        return this;
    }

    /**
     * Builds the {@link RepositoryItemIdempotentWriter}.
     *
     * @return a {@link RepositoryItemIdempotentWriter}
     */
    public RepositoryItemIdempotentWriter<T> build() {
        Assert.notNull(this.repository, "repository is required.");

        RepositoryItemIdempotentWriter<T> writer = new RepositoryItemIdempotentWriter<>();
        writer.setName(name);
        writer.setIdempotencyOptions(this.idempotencyOptions);
        writer.setRepository(this.repository);
        writer.setArguments(this.arguments);
        writer.setIdempotentWriter(idempotentWriter);
        if (this.methodName != null) {
            Assert.hasText(this.methodName, "methodName must not be empty.");
            writer.setMethodName(this.methodName);
        } else {
            log.debug("No method name provided, CrudRepository.saveAll will be used.");
        }
        return writer;
    }

}
