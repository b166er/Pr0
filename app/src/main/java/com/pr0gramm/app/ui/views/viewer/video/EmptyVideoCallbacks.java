package com.pr0gramm.app.ui.views.viewer.video;

/**
 * Empty implementation of {@link VideoPlayer.Callbacks}
 */
public final class EmptyVideoCallbacks implements VideoPlayer.Callbacks {
    @Override
    public void onVideoBufferingStarts() {

    }

    @Override
    public void onVideoBufferingEnds() {

    }

    @Override
    public void onVideoRenderingStarts() {

    }

    @Override
    public void onVideoSizeChanged(int width, int height) {

    }

    @Override
    public void onVideoError(String message, VideoPlayer.ErrorKind kind) {

    }

    @Override
    public void onDroppedFrames(int count) {

    }
}
