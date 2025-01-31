package com.aril.arilbatchsdk.core.item.service.writer;

import com.aril.arilbatchsdk.core.item.AbstractItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.service.IdempotentService;
import com.aril.arilbatchsdk.util.MethodInvokerUtils;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.util.List;

@Setter
public class ServiceItemIdempotentWriter<T extends IdempotentBatchItem> extends AbstractItemIdempotentWriter<T, Void> implements InitializingBean {
    private IdempotentService<T> service;
    private String methodName;
    private List<?> arguments;

    public ServiceItemIdempotentWriter() {
        setName(ClassUtils.getShortName(ServiceItemIdempotentWriter.class));
    }

    /**
     * Check mandatory properties - there must be a service.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initIdempotencyOptions();
        if (idempotencyOptions.isEnable()) {
            Assert.notNull(idempotentWriter, "The idempotent-writer must be set");
        }
        Assert.notNull(name, "The name must be set");
        Assert.state(service != null, "A IdempotentService implementation is required");
        Assert.state(StringUtils.hasText(this.methodName), "methodName must not be empty.");
    }

    /**
     * Performs the actual write to the service. This can be overridden by a subclass
     * if necessary.
     *
     * @param item the item to be persisted.
     * @throws Exception thrown if error occurs during writing.
     */
    @Override
    protected Void doWrite(T item) throws Exception {
        MethodInvoker invoker = MethodInvokerUtils.createMethodInvoker(service, methodName);
        invoker.setArguments(MethodInvokerUtils.generateMethodArguments(item, arguments));
        MethodInvokerUtils.doInvoke(invoker);
        return null;
    }
}
