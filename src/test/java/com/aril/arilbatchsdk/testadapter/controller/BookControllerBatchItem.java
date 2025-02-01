package com.aril.arilbatchsdk.testadapter.controller;

import com.aril.valhala.application.Pager;
import com.aril.valhala.batch.ControllerBatchItem;

public class BookControllerBatchItem implements ControllerBatchItem {

    private Pager pager;

    @Override
    public Pager getPager() {
        return pager;
    }

    @Override
    public void setPager(Pager pager) {
        this.pager = pager;
    }
}
