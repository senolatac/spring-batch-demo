package com.aril.arilbatchsdk.core.item.service.reader;

import com.aril.arilbatchsdk.core.item.AbstractPagingServiceAndControllerReader;
import com.aril.arilbatchsdk.util.MethodInvokerUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Setter
public class ServiceItemReader<T> extends AbstractPagingServiceAndControllerReader<T> implements InitializingBean {

    private PagingReaderService<?> service;
    private List<?> arguments;
    private String methodName;

    public ServiceItemReader() {
        this.setName(ClassUtils.getShortName(ServiceItemReader.class));
    }

    /**
     * Performs the actual reading of a page via the service. Available for overriding
     * as needed.
     *
     * @return the list of items that make up the page
     * @throws Exception Based on what the underlying method throws or related to the
     *                   calling of the method
     */
    @SuppressWarnings("unchecked")
    protected List<T> doPageRead() throws Exception {
        Pageable pageRequest = PageRequest.of(page.get(), pageSize);

        MethodInvoker invoker = MethodInvokerUtils.createMethodInvoker(service, methodName);
        invoker.setArguments(MethodInvokerUtils.generateMethodArguments(pageRequest, arguments));

        Slice<T> curPage = (Slice<T>) MethodInvokerUtils.doInvoke(invoker);

        return curPage.getContent();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(this.service != null, "A PagingService is required");
        Assert.state(this.pageSize > 0, "Page size must be greater than 0");
        Assert.state(this.methodName != null && !this.methodName.isEmpty(), "methodName is required.");
        if (this.isSaveState()) {
            Assert.state(StringUtils.hasText(this.getName()), "A name is required when saveState is set to true.");
        }
    }
}
