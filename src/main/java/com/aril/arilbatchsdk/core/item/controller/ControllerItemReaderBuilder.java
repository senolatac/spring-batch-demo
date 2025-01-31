package com.aril.arilbatchsdk.core.item.controller;

import com.aril.valhala.application.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ControllerItemReaderBuilder<T> {
    private Controller controller;
    private List<?> arguments;
    private int pageSize = 10;
    private Method method;
    private boolean saveState = true;
    private String name = "controllerItemReader";
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
    public ControllerItemReaderBuilder<T> saveState(boolean saveState) {
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
    public ControllerItemReaderBuilder<T> name(String name) {
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
    public ControllerItemReaderBuilder<T> maxItemCount(int maxItemCount) {
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
    public ControllerItemReaderBuilder<T> currentItemCount(int currentItemCount) {
        this.currentItemCount = currentItemCount;

        return this;
    }

    /**
     * Arguments to be passed to the data providing method.
     *
     * @param arguments list of method arguments to be passed to the repository.
     * @return The current instance of the builder.
     * @see ControllerItemReader#setArguments(List)
     */
    public ControllerItemReaderBuilder<T> arguments(List<?> arguments) {
        this.arguments = arguments;

        return this;
    }

    /**
     * Arguments to be passed to the data providing method.
     *
     * @param arguments the method arguments to be passed to the repository.
     * @return The current instance of the builder.
     * @see ControllerItemReader#setArguments(List)
     */
    public ControllerItemReaderBuilder<T> arguments(Object... arguments) {
        return arguments(Arrays.asList(arguments));
    }

    /**
     * Establish the pageSize for the generated RepositoryItemReader.
     *
     * @param pageSize The number of items to retrieve per page. Must be greater than 0.
     * @return The current instance of the builder.
     * @see ControllerItemReader#setPageSize(int)
     */
    public ControllerItemReaderBuilder<T> pageSize(int pageSize) {
        this.pageSize = pageSize;

        return this;
    }

    /**
     * The {@link com.aril.valhala.batch.ControllerBatchItem}
     * implementation used to read input from.
     *
     * @param controller underlying controller for input to be read from.
     * @return The current instance of the builder.
     * @see ControllerItemReader#setController(Controller)
     */
    public ControllerItemReaderBuilder<T> controller(Controller controller) {
        this.controller = controller;

        return this;
    }

    /**
     * Specifies what method on the repository to call. This method must take
     * {@link org.springframework.data.domain.Pageable} as the <em>last</em> argument.
     *
     * @param method name of the method to invoke.
     * @return The current instance of the builder.
     * @see ControllerItemReader#setMethod(Method)
     */
    public ControllerItemReaderBuilder<T> method(Method method) {
        this.method = method;

        return this;
    }

    /**
     * Builds the {@link ControllerItemReader}.
     *
     * @return a {@link ControllerItemReader}
     */
    public ControllerItemReader<T> build() {
        Assert.notNull(this.controller, "controller is required.");
        Assert.isTrue(this.pageSize > 0, "Page size must be greater than 0");
        Assert.notNull(this.method, "method is required.");
        if (this.saveState) {
            Assert.state(StringUtils.hasText(this.name), "A name is required when saveState is set to true.");
        }

        ControllerItemReader<T> reader = new ControllerItemReader<>();
        reader.setArguments(this.arguments);
        reader.setController(this.controller);
        reader.setMethod(this.method);
        reader.setPageSize(this.pageSize);
        reader.setCurrentItemCount(this.currentItemCount);
        reader.setMaxItemCount(this.maxItemCount);
        reader.setSaveState(this.saveState);
        reader.setName(this.name);
        return reader;
    }
}
