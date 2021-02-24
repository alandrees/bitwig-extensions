package com.bitwig.extensions.controllers.nativeinstruments.maschine.modes;

import com.bitwig.extension.controller.api.DrumPad;

public interface NoteFocusHandler {
	public void notifyDrumPadSelected(DrumPad pad, int padOffset, int note);

	public void notifyNoteSelected(int note);

}
