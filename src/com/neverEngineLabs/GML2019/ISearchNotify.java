package com.neverEngineLabs.GML2019;

import java.util.concurrent.ConcurrentLinkedDeque;

public interface ISearchNotify {
    void taskComplete(Thread tm, int m_id);
}
