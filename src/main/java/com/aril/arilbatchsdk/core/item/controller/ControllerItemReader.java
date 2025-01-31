package com.aril.arilbatchsdk.core.item.controller;

import com.aril.arilbatchsdk.core.item.AbstractPagingServiceAndControllerReader;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.arilbatchsdk.util.GsonUtils;
import com.aril.arilbatchsdk.util.MethodInvokerUtils;
import com.aril.valhala.application.Controller;
import com.aril.valhala.application.Pager;
import com.aril.valhala.batch.ControllerBatchItem;
import com.aril.valhala.dto.SearchResultDTO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Setter
public class ControllerItemReader<T> extends AbstractPagingServiceAndControllerReader<T> implements InitializingBean {

    private Controller controller;
    private List<?> arguments;
    private Method method;

    public ControllerItemReader() {
        this.setName(ClassUtils.getShortName(ServiceItemReader.class));
        //pageNumber starts with 1 on FE so start from 1
        page.set(1);
    }

    /**
     * Performs the actual reading of a page via the controller. Available for overriding
     * as needed.
     *
     * @return the list of items that make up the page
     * @throws Exception Based on what the underlying method throws or related to the
     *                   calling of the method
     */
    @SuppressWarnings("unchecked")
    protected List<T> doPageRead() throws Exception {
        Pager pager = Pager.builder()
                .pageSize(pageSize)
                .pageNumber(page.get())
                .build();

        MethodInvoker invoker = MethodInvokerUtils.createMethodInvoker(controller, method.getName());
        invoker.setArguments(setParameters(pager).toArray());

        ResponseEntity<SearchResultDTO<T>> searchResult = (ResponseEntity<SearchResultDTO<T>>) MethodInvokerUtils.doInvoke(invoker);

        if (searchResult == null || searchResult.getBody() == null) {
            return Collections.emptyList();
        }

        SearchResultDTO<T> body = searchResult.getBody();

        checkPageSize(body);

        return body != null && body.getResult() != null ? body.getResult() : Collections.emptyList();
    }

    private void checkPageSize(SearchResultDTO<T> result) {
        if (result == null) {
            return;
        }

        if (result.getResult().size() > pageSize || result.getResultInfo().getTotalPage() == 1) { //means single page
            //added +1 due to don't let to go next page
            pageSize = Math.max(pageSize, result.getResult().size()) + 1;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(this.controller != null, "A PagerController is required");
        Assert.state(this.pageSize > 0, "Page size must be greater than 0");
        Assert.state(this.method != null, "method is required.");
        if (this.isSaveState()) {
            Assert.state(StringUtils.hasText(this.getName()), "A name is required when saveState is set to true.");
        }
    }

    private List<Object> setParameters(Pager pager) {
        List<Object> parameters = new ArrayList<>();
        for (int i = 0; i < method.getParameters().length; i++) {
            Class<?> type = method.getParameters()[i].getType();
            Object item = arguments.get(i);

            if (type.isInstance(item)) {
                item = type.cast(arguments.get(i));
            } else if (item instanceof String) {
                item = GsonUtils.GSON.fromJson(String.valueOf(item), type);
            } else if (item instanceof Map<?, ?>) {
                item = GsonUtils.GSON.fromJson(GsonUtils.GSON.toJson(item, Map.class), type);
            } else {
                item = type.cast(arguments.get(i));
            }

            parameters.add(setPagerOfParameter(item, pager));
        }
        return parameters;
    }

    private Object setPagerOfParameter(Object param, Pager pager) {
        if (param instanceof ControllerBatchItem item) {
            item.setPager(pager);
            return item;
        }
        return param;
    }
}
