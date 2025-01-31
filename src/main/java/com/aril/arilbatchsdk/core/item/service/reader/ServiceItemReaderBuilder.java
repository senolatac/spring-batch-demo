package com.aril.arilbatchsdk.core.item.service.reader;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class ServiceItemReaderBuilder<T> {

    private PagingReaderService<?> service;
    private List<?> arguments;
    private int pageSize = 10;
    private String methodName;
    private boolean saveState = true;
    private String name = "serviceItemReader";
    private int maxItemCount = Integer.MAX_VALUE;
    private int currentItemCount;

    /**
     * Configure if the state of the
     * {@link org.springframework.batch.item.ItemStreamSupport} should be persisted within
     * the {@link org.springframework.batch.item.ExecutionContext} for restart purposes.
     *
     * @param saveState defaults to true
     * @return The current instance of the builder.
     */
    public ServiceItemReaderBuilder<T> saveState(boolean saveState) {
        this.saveState = saveState;

        return this;
    }

    /**
     * The name used to calculate the key within the
     * {@link org.springframework.batch.item.ExecutionContext}. Required if
     * {@link #saveState(boolean)} is set to true.
     *
     * @param name name of the reader instance
     * @return The current instance of the builder.
     * @see org.springframework.batch.item.ItemStreamSupport#setName(String)
     */
    public ServiceItemReaderBuilder<T> name(String name) {
        this.name = name;

        return this;
    }

    /**
     * Configure the max number of items to be read.
     *
     * @param maxItemCount the max items to be read
     * @return The current instance of the builder.
     * @see org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader#setMaxItemCount(int)
     */
    public ServiceItemReaderBuilder<T> maxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;

        return this;
    }

    /**
     * Index for the current item. Used on restarts to indicate where to start from.
     *
     * @param currentItemCount current index
     * @return this instance for method chaining
     * @see org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader#setCurrentItemCount(int)
     */
    public ServiceItemReaderBuilder<T> currentItemCount(int currentItemCount) {
        this.currentItemCount = currentItemCount;

        return this;
    }

    /**
     * Arguments to be passed to the data providing method.
     *
     * @param arguments list of method arguments to be passed to the repository.
     * @return The current instance of the builder.
     * @see ServiceItemReader#setArguments(List)
     */
    public ServiceItemReaderBuilder<T> arguments(List<?> arguments) {
        this.arguments = arguments;

        return this;
    }

    /**
     * Arguments to be passed to the data providing method.
     *
     * @param arguments the method arguments to be passed to the repository.
     * @return The current instance of the builder.
     * @see ServiceItemReader#setArguments(List)
     */
    public ServiceItemReaderBuilder<T> arguments(Object... arguments) {
        return arguments(Arrays.asList(arguments));
    }

    /**
     * Establish the pageSize for the generated RepositoryItemReader.
     *
     * @param pageSize The number of items to retrieve per page. Must be greater than 0.
     * @return The current instance of the builder.
     * @see ServiceItemReader#setPageSize(int)
     */
    public ServiceItemReaderBuilder<T> pageSize(int pageSize) {
        this.pageSize = pageSize;

        return this;
    }

    /**
     * The {@link PagingReaderService}
     * implementation used to read input from.
     *
     * @param service underlying service for input to be read from.
     * @return The current instance of the builder.
     * @see ServiceItemReader#setService(PagingReaderService)
     */
    public ServiceItemReaderBuilder<T> service(PagingReaderService<?> service) {
        this.service = service;

        return this;
    }

    /**
     * Specifies what method on the repository to call. This method must take
     * {@link org.springframework.data.domain.Pageable} as the <em>last</em> argument.
     *
     * @param methodName name of the method to invoke.
     * @return The current instance of the builder.
     * @see ServiceItemReader#setMethodName(String)
     */
    public ServiceItemReaderBuilder<T> methodName(String methodName) {
        this.methodName = methodName;

        return this;
    }

    /**
     * Builds the {@link ServiceItemReader}.
     *
     * @return a {@link ServiceItemReader}
     */
    public ServiceItemReader<T> build() {
        Assert.notNull(this.service, "service is required.");
        Assert.isTrue(this.pageSize > 0, "Page size must be greater than 0");
        Assert.hasText(this.methodName, "methodName is required.");
        if (this.saveState) {
            Assert.state(StringUtils.hasText(this.name), "A name is required when saveState is set to true.");
        }

        ServiceItemReader<T> reader = new ServiceItemReader<>();
        reader.setArguments(this.arguments);
        reader.setService(this.service);
        reader.setMethodName(this.methodName);
        reader.setPageSize(this.pageSize);
        reader.setCurrentItemCount(this.currentItemCount);
        reader.setMaxItemCount(this.maxItemCount);
        reader.setSaveState(this.saveState);
        reader.setName(this.name);
        return reader;
    }
}
