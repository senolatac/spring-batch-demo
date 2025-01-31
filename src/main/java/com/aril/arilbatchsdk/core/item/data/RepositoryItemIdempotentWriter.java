package com.aril.arilbatchsdk.core.item.data;

import com.aril.arilbatchsdk.core.item.AbstractItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.kafka.KafkaItemIdempotentWriter;
import com.aril.arilbatchsdk.util.MethodInvokerUtils;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Setter
public class RepositoryItemIdempotentWriter<T extends IdempotentBatchItem> extends AbstractItemIdempotentWriter<T, Void> implements InitializingBean {

    private CrudRepository<T, ?> repository;
    private String methodName;
    private List<?> arguments;

    public RepositoryItemIdempotentWriter() {
        setName(ClassUtils.getShortName(KafkaItemIdempotentWriter.class));
    }

    /**
     * Check mandatory properties - there must be a repository.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initIdempotencyOptions();
        if (idempotencyOptions.isEnable()) {
            Assert.notNull(idempotentWriter, "The idempotent-writer must be set");
        }
        Assert.notNull(name, "The name must be set");
        Assert.state(repository != null, "A CrudRepository implementation is required");
        if (this.methodName != null) {
            Assert.state(StringUtils.hasText(this.methodName), "methodName must not be empty.");
        } else {
            log.debug("No method name provided, CrudRepository.save will be used.");
        }
    }

    /**
     * Performs the actual write to the repository. This can be overridden by a subclass
     * if necessary.
     *
     * @param item the item to be persisted.
     * @throws Exception thrown if error occurs during writing.
     */
    protected Void doWrite(T item) throws Exception {
        if (this.methodName == null) {
            this.repository.save(item);
            return null;
        }

        MethodInvoker invoker = MethodInvokerUtils.createMethodInvoker(repository, methodName);
        invoker.setArguments(MethodInvokerUtils.generateMethodArguments(item, arguments));
        MethodInvokerUtils.doInvoke(invoker);
        return null;
    }
}
