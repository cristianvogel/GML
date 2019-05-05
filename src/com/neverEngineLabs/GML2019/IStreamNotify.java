package com.neverEngineLabs.GML2019;

public interface IStreamNotify {
    void playbackStart(String url, String token);
    void playbackStatus(String thread);
}