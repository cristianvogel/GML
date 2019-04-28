package com.neverEngineLabs.GML2019;

import javafx.scene.media.AudioClip;
import rita.support.RiTimer;

import static processing.core.PApplet.println;

public class AudioStreamer extends Thread {


    private String _url;
        private double _volume;
        private AudioClip soundClip;
        private boolean _looping;
        private int _cycles;

        //Timed Event

          private boolean _offsetTimerComplete = false;
          private float _startTime;
          private RiTimer _offsetTimer;

    AudioStreamer (String url, float startTime) {
            _url = url;
            _volume = 0.75;
            _looping = false;
            _cycles = -1;
            _startTime = startTime;
            soundClip = new AudioClip(_url);

            if(startTime != 0) {
                _offsetTimer = new RiTimer(this, _startTime, "timerDone");
            } else _offsetTimerComplete = true;
        }

        public void run()
        {

           if (_offsetTimerComplete && !soundClip.isPlaying()) {
                println("Timer complete, starting playback");
                soundClip.play(_volume);
            }


        }



    /**
     * Callback method for RiTimer
     */
    private void timerDone () {

            _offsetTimerComplete = true;
            _offsetTimer.stop();
        }


        public void setVolume (double volume)
        {
            _volume = volume;
            soundClip.setVolume(volume);
        }

        public boolean getStatus() {
            return soundClip.isPlaying();
        }


}

